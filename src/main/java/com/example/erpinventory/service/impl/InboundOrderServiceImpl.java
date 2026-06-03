package com.example.erpinventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erpinventory.entity.InboundOrder;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.mapper.InboundOrderMapper;
import com.example.erpinventory.service.InboundOrderService;
import com.example.erpinventory.service.InventoryTransactionService;
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
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder> implements InboundOrderService {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Autowired
    private ProductWarningMessageService productWarningMessageService;

    @Autowired
    private CurrentOperatorResolver currentOperatorResolver;

    @Override
    public Page<InboundOrder> pageInboundOrders(Integer pageNum, Integer pageSize, String keyword) {
        Page<InboundOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InboundOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(InboundOrder::getOrderNo, keyword)
                    .or()
                    .like(InboundOrder::getSupplier, keyword);
        }
        return baseMapper.selectPageWithProductName(page, queryWrapper);
    }

    @Override
    public List<InboundOrder> listInboundOrders() {
        return baseMapper.selectListWithProductName();
    }

    @Override
    @Transactional
    public InboundOrder createInboundOrder(InboundOrder inboundOrder) {
        Product product = productService.getById(inboundOrder.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        String orderNo = "IN" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        CurrentOperatorResolver.CurrentOperator operator = currentOperatorResolver.resolve();
        inboundOrder.setOrderNo(orderNo);
        inboundOrder.setStatus(0);
        inboundOrder.setCreateBy(operator.userId());
        inboundOrder.setCreateByName(operator.username());
        inboundOrder.setAuditBy(null);
        inboundOrder.setAuditByName(null);
        inboundOrder.setAuditTime(null);
        inboundOrder.setCreateTime(now);
        inboundOrder.setUpdateTime(now);

        if (inboundOrder.getPrice() != null && inboundOrder.getQuantity() != null) {
            inboundOrder.setTotalAmount(
                    inboundOrder.getPrice().multiply(java.math.BigDecimal.valueOf(inboundOrder.getQuantity()))
            );
        }

        save(inboundOrder);
        return inboundOrder;
    }

    @Override
    @Transactional
    public boolean auditInboundOrder(Long id) {
        InboundOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() == 1) {
            throw new BusinessException("该入库单已审核");
        }

        CurrentOperatorResolver.CurrentOperator operator = currentOperatorResolver.resolve();
        if (order.getCreateBy() != null && order.getCreateBy().equals(operator.userId())) {
            throw new BusinessException("制单人不能审核自己的入库单");
        }

        Product product = productService.getById(order.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        int beforeStock = product.getStock() == null ? 0 : product.getStock();
        int quantity = order.getQuantity() == null ? 0 : order.getQuantity();
        int afterStock = beforeStock + quantity;
        LocalDateTime now = LocalDateTime.now();

        if (!productService.updateStockIfMatches(product.getId(), beforeStock, afterStock, now)) {
            throw new BusinessException("商品库存已发生变化，请刷新后重试");
        }

        product.setStock(afterStock);
        product.setUpdateTime(now);
        productWarningMessageService.syncWarningsForProduct(product);

        inventoryTransactionService.recordTransaction(
                product.getId(),
                1,
                quantity,
                beforeStock,
                afterStock,
                order.getId(),
                order.getOrderNo(),
                "入库审核通过" + (order.getSupplier() == null || order.getSupplier().isBlank()
                        ? ""
                        : "，供应商：" + order.getSupplier().trim()),
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
