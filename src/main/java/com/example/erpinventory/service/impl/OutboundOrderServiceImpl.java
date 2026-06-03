package com.example.erpinventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erpinventory.entity.OutboundOrder;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.mapper.OutboundOrderMapper;
import com.example.erpinventory.service.InventoryTransactionService;
import com.example.erpinventory.service.OutboundOrderService;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.service.ProductWarningMessageService;
import com.example.erpinventory.util.CurrentOperatorResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundOrderService {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Autowired
    private ProductWarningMessageService productWarningMessageService;

    @Autowired
    private CurrentOperatorResolver currentOperatorResolver;

    @Override
    public Page<OutboundOrder> pageOutboundOrders(Integer pageNum, Integer pageSize, String keyword) {
        Page<OutboundOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OutboundOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(OutboundOrder::getOrderNo, keyword)
                    .or()
                    .like(OutboundOrder::getCustomer, keyword);
        }
        return baseMapper.selectPageWithProductName(page, queryWrapper);
    }

    @Override
    public List<OutboundOrder> listOutboundOrders() {
        return baseMapper.selectListWithProductName();
    }

    @Override
    @Transactional
    public OutboundOrder createOutboundOrder(OutboundOrder outboundOrder) {
        Product product = productService.getById(outboundOrder.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        String orderNo = "OUT" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        CurrentOperatorResolver.CurrentOperator operator = currentOperatorResolver.resolve();
        outboundOrder.setOrderNo(orderNo);
        outboundOrder.setStatus(0);
        outboundOrder.setCreateBy(operator.userId());
        outboundOrder.setCreateByName(operator.username());
        outboundOrder.setAuditBy(null);
        outboundOrder.setAuditByName(null);
        outboundOrder.setAuditTime(null);
        outboundOrder.setCreateTime(now);
        outboundOrder.setUpdateTime(now);

        if (outboundOrder.getPrice() != null && outboundOrder.getQuantity() != null) {
            outboundOrder.setTotalAmount(
                    outboundOrder.getPrice().multiply(java.math.BigDecimal.valueOf(outboundOrder.getQuantity()))
            );
        }

        save(outboundOrder);
        return outboundOrder;
    }

    @Override
    @Transactional
    public boolean auditOutboundOrder(Long id) {
        OutboundOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() == 1) {
            throw new BusinessException("该出库单已审核");
        }

        CurrentOperatorResolver.CurrentOperator operator = currentOperatorResolver.resolve();
        if (order.getCreateBy() != null && order.getCreateBy().equals(operator.userId())) {
            throw new BusinessException("制单人不能审核自己的出库单");
        }

        Product product = productService.getById(order.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        int beforeStock = product.getStock() == null ? 0 : product.getStock();
        int quantity = order.getQuantity() == null ? 0 : order.getQuantity();
        if (beforeStock < quantity) {
            throw new BusinessException("库存不足");
        }

        int afterStock = beforeStock - quantity;
        LocalDateTime now = LocalDateTime.now();
        if (!productService.updateStockIfMatches(product.getId(), beforeStock, afterStock, now)) {
            throw new BusinessException("商品库存已发生变化，请刷新后重试");
        }

        product.setStock(afterStock);
        product.setUpdateTime(now);
        productWarningMessageService.syncWarningsForProduct(product);

        inventoryTransactionService.recordTransaction(
                product.getId(),
                2,
                quantity,
                beforeStock,
                afterStock,
                order.getId(),
                order.getOrderNo(),
                "出库审核通过" + (order.getCustomer() == null || order.getCustomer().isBlank()
                        ? ""
                        : "，客户：" + order.getCustomer().trim()),
                now
        );

        order.setStatus(1);
        order.setAuditBy(operator.userId());
        order.setAuditByName(operator.username());
        order.setAuditTime(now);
        order.setUpdateTime(now);
        return updateById(order);
    }
}
