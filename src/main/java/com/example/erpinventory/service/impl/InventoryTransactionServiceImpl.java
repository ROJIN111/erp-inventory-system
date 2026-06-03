package com.example.erpinventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erpinventory.entity.InventoryTransaction;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.mapper.InventoryTransactionMapper;
import com.example.erpinventory.service.InventoryTransactionService;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.util.CurrentOperatorResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class InventoryTransactionServiceImpl extends ServiceImpl<InventoryTransactionMapper, InventoryTransaction>
        implements InventoryTransactionService {

    private static final DateTimeFormatter TRANSACTION_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final DateTimeFormatter TREND_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Autowired
    private ProductService productService;

    @Autowired
    private CurrentOperatorResolver currentOperatorResolver;

    @Override
    public Page<InventoryTransaction> pageTransactions(Integer pageNum, Integer pageSize,
                                                       Long productId, Integer transactionType,
                                                       String sku, String category,
                                                       String relatedNo, String operatorName,
                                                       String keyword,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        validateTimeRange(startTime, endTime);
        Page<InventoryTransaction> page = new Page<>(pageNum, pageSize);
        return page(page, buildFilteredQuery(productId, transactionType, sku, category, relatedNo, operatorName, keyword, startTime, endTime));
    }

    @Override
    public List<InventoryTransaction> listTransactions(Long productId, Integer transactionType,
                                                       String sku, String category,
                                                       String relatedNo, String operatorName,
                                                       String keyword,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        validateTimeRange(startTime, endTime);
        return list(buildFilteredQuery(productId, transactionType, sku, category, relatedNo, operatorName, keyword, startTime, endTime));
    }

    @Override
    public void recordTransaction(Long productId, Integer transactionType, Integer quantity,
                                  Integer beforeStock, Integer afterStock,
                                  Long relatedId, String relatedNo, String remark,
                                  LocalDateTime operationTime) {
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在，无法记录库存流水");
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setProductId(productId);
        transaction.setProductName(product.getName());
        transaction.setSku(product.getSku());
        transaction.setTransactionType(transactionType);
        transaction.setQuantity(Math.abs(quantity == null ? 0 : quantity));
        transaction.setBeforeStock(beforeStock);
        transaction.setAfterStock(afterStock);
        transaction.setRelatedId(relatedId);
        transaction.setRelatedNo(relatedNo);
        CurrentOperatorResolver.CurrentOperator operator = currentOperatorResolver.resolve();
        transaction.setOperatorId(operator.userId());
        transaction.setOperatorName(operator.username());
        transaction.setRemark(buildRemark(operator.username(), remark));
        transaction.setCreateTime(operationTime == null ? LocalDateTime.now() : operationTime);
        save(transaction);
    }

    @Override
    public List<InventoryTransaction> listRecentTransactions(int limit, List<Integer> transactionTypes) {
        LambdaQueryWrapper<InventoryTransaction> queryWrapper = new LambdaQueryWrapper<>();
        if (transactionTypes != null && !transactionTypes.isEmpty()) {
            queryWrapper.in(InventoryTransaction::getTransactionType, transactionTypes);
        }
        queryWrapper.orderByDesc(InventoryTransaction::getCreateTime)
                .last("LIMIT " + limit);
        return list(queryWrapper);
    }

    @Override
    public List<InventoryTransaction> listTransactionsByRelated(Long relatedId, String relatedNo) {
        LambdaQueryWrapper<InventoryTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryTransaction::getRelatedId, relatedId);
        if (relatedNo != null && !relatedNo.isBlank()) {
            queryWrapper.eq(InventoryTransaction::getRelatedNo, relatedNo);
        }
        queryWrapper.orderByAsc(InventoryTransaction::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> buildTrend(int days) {
        LocalDateTime startTime = LocalDate.now().minusDays(days - 1L).atStartOfDay();
        LambdaQueryWrapper<InventoryTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(InventoryTransaction::getCreateTime, startTime)
                .in(InventoryTransaction::getTransactionType, List.of(1, 2, 3, 4))
                .orderByAsc(InventoryTransaction::getCreateTime);

        List<InventoryTransaction> transactions = list(queryWrapper);
        Map<String, Map<String, Object>> dailyMap = new LinkedHashMap<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startTime.toLocalDate().plusDays(i);
            String key = date.toString();
            Map<String, Object> bucket = new LinkedHashMap<>();
            bucket.put("date", key);
            bucket.put("label", date.format(TREND_LABEL_FORMATTER));
            bucket.put("inbound", 0);
            bucket.put("outbound", 0);
            bucket.put("checkGain", 0);
            bucket.put("checkLoss", 0);
            bucket.put("netChange", 0);
            dailyMap.put(key, bucket);
        }

        for (InventoryTransaction transaction : transactions) {
            String key = transaction.getCreateTime().toLocalDate().toString();
            Map<String, Object> bucket = dailyMap.get(key);
            if (bucket == null) {
                continue;
            }

            int quantity = transaction.getQuantity() == null ? 0 : transaction.getQuantity();
            int inbound = (int) bucket.get("inbound");
            int outbound = (int) bucket.get("outbound");
            int checkGain = (int) bucket.get("checkGain");
            int checkLoss = (int) bucket.get("checkLoss");

            switch (transaction.getTransactionType()) {
                case 1 -> inbound += quantity;
                case 2 -> outbound += quantity;
                case 3 -> checkGain += quantity;
                case 4 -> checkLoss += quantity;
                default -> {
                }
            }

            bucket.put("inbound", inbound);
            bucket.put("outbound", outbound);
            bucket.put("checkGain", checkGain);
            bucket.put("checkLoss", checkLoss);
            bucket.put("netChange", inbound - outbound + checkGain - checkLoss);
        }

        return new ArrayList<>(dailyMap.values());
    }

    private String generateTransactionNo() {
        return "IT"
                + LocalDateTime.now().format(TRANSACTION_NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private LambdaQueryWrapper<InventoryTransaction> buildFilteredQuery(Long productId, Integer transactionType,
                                                                        String sku, String category,
                                                                        String relatedNo, String operatorName,
                                                                        String keyword,
                                                                        LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<InventoryTransaction> queryWrapper = new LambdaQueryWrapper<>();
        List<Long> categoryProductIds = resolveCategoryProductIds(category);
        if (categoryProductIds != null && categoryProductIds.isEmpty()) {
            queryWrapper.eq(InventoryTransaction::getId, -1L);
            return queryWrapper;
        }
        queryWrapper.eq(productId != null, InventoryTransaction::getProductId, productId)
                .eq(transactionType != null, InventoryTransaction::getTransactionType, transactionType)
                .like(sku != null && !sku.isBlank(), InventoryTransaction::getSku, sku == null ? null : sku.trim())
                .like(relatedNo != null && !relatedNo.isBlank(), InventoryTransaction::getRelatedNo, relatedNo == null ? null : relatedNo.trim())
                .like(operatorName != null && !operatorName.isBlank(), InventoryTransaction::getOperatorName, operatorName == null ? null : operatorName.trim())
                .ge(startTime != null, InventoryTransaction::getCreateTime, startTime)
                .le(endTime != null, InventoryTransaction::getCreateTime, endTime)
                .in(categoryProductIds != null, InventoryTransaction::getProductId, categoryProductIds)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(InventoryTransaction::getTransactionNo, keyword.trim())
                        .or().like(InventoryTransaction::getProductName, keyword.trim())
                        .or().like(InventoryTransaction::getSku, keyword.trim())
                        .or().like(InventoryTransaction::getRelatedNo, keyword.trim())
                        .or().like(InventoryTransaction::getOperatorName, keyword.trim())
                        .or().like(InventoryTransaction::getRemark, keyword.trim()))
                .orderByDesc(InventoryTransaction::getCreateTime)
                .orderByDesc(InventoryTransaction::getId);
        return queryWrapper;
    }

    private List<Long> resolveCategoryProductIds(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }

        return productService.list(new LambdaQueryWrapper<Product>()
                        .like(Product::getCategory, category.trim()))
                .stream()
                .map(Product::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
    }

    private String buildRemark(String operatorName, String remark) {
        if (operatorName == null || operatorName.isBlank()) {
            return remark;
        }
        if (remark == null || remark.isBlank()) {
            return "经手人：" + operatorName;
        }
        return "经手人：" + operatorName + "；" + remark;
    }
}
