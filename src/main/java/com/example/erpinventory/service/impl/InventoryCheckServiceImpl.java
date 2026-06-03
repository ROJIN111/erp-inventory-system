package com.example.erpinventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erpinventory.entity.InventoryCheck;
import com.example.erpinventory.entity.InventoryCheckItem;
import com.example.erpinventory.entity.InventoryTransaction;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.mapper.InventoryCheckMapper;
import com.example.erpinventory.service.InventoryCheckItemService;
import com.example.erpinventory.service.InventoryCheckService;
import com.example.erpinventory.service.InventoryTransactionService;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.service.ProductWarningMessageService;
import com.example.erpinventory.util.CurrentOperatorResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class InventoryCheckServiceImpl extends ServiceImpl<InventoryCheckMapper, InventoryCheck>
        implements InventoryCheckService {

    private static final List<Integer> VALID_CHECK_MODES = List.of(1, 2, 3, 4);
    private static final List<Integer> VALID_REASON_TYPES = List.of(1, 2, 3, 4);
    private static final List<Integer> TRACKED_TRANSACTION_TYPES = List.of(1, 2, 3, 4);

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryCheckItemService inventoryCheckItemService;

    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Autowired
    private ProductWarningMessageService productWarningMessageService;

    @Autowired
    private CurrentOperatorResolver currentOperatorResolver;

    @Override
    public Page<InventoryCheck> pageInventoryChecks(Integer pageNum, Integer pageSize, String keyword) {
        Page<InventoryCheck> pageData = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryCheck> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(wrapper -> wrapper.like(InventoryCheck::getCheckNo, keyword)
                    .or()
                    .like(InventoryCheck::getScopeName, keyword)
                    .or()
                    .like(InventoryCheck::getRemark, keyword));
        }
        queryWrapper.orderByDesc(InventoryCheck::getCreateTime);

        Page<InventoryCheck> pageResult = page(pageData, queryWrapper);
        fillCheckSummaries(pageResult.getRecords());
        return pageResult;
    }

    @Override
    public InventoryCheck getDetailById(Long id) {
        InventoryCheck inventoryCheck = getById(id);
        if (inventoryCheck == null) {
            throw new BusinessException("盘点单不存在");
        }

        List<InventoryCheckItem> items = listItems(id);
        inventoryCheck.setItems(items);
        fillCheckSummary(inventoryCheck, items);
        return inventoryCheck;
    }

    @Override
    @Transactional
    public InventoryCheck createInventoryCheck(InventoryCheck inventoryCheck) {
        validateInventoryCheck(inventoryCheck);

        inventoryCheck.setId(null);
        inventoryCheck.setCheckNo(generateCheckNo());
        inventoryCheck.setStatus(0);
        inventoryCheck.setBlindCheck(normalizeBlindCheck(inventoryCheck.getBlindCheck()));
        inventoryCheck.setSnapshotTime(null);
        inventoryCheck.setReviewTime(null);
        CurrentOperatorResolver.CurrentOperator operator = currentOperatorResolver.resolve();
        inventoryCheck.setCreateBy(operator.userId());
        inventoryCheck.setCreateByName(operator.username());
        inventoryCheck.setAuditBy(null);
        inventoryCheck.setAuditByName(null);
        inventoryCheck.setCreateTime(LocalDateTime.now());
        inventoryCheck.setUpdateTime(LocalDateTime.now());
        save(inventoryCheck);

        saveItems(inventoryCheck.getId(), inventoryCheck.getItems());
        return getDetailById(inventoryCheck.getId());
    }

    @Override
    @Transactional
    public InventoryCheck updateInventoryCheck(InventoryCheck inventoryCheck) {
        if (inventoryCheck.getId() == null) {
            throw new BusinessException("盘点单ID不能为空");
        }
        validateInventoryCheck(inventoryCheck);

        InventoryCheck existing = getById(inventoryCheck.getId());
        if (existing == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (!Integer.valueOf(0).equals(existing.getStatus())) {
            throw new BusinessException("只有草稿状态的盘点单才可以修改");
        }

        InventoryCheck updateEntity = new InventoryCheck();
        updateEntity.setId(existing.getId());
        updateEntity.setCheckDate(inventoryCheck.getCheckDate());
        updateEntity.setCheckMode(inventoryCheck.getCheckMode());
        updateEntity.setScopeName(inventoryCheck.getScopeName().trim());
        updateEntity.setBlindCheck(normalizeBlindCheck(inventoryCheck.getBlindCheck()));
        updateEntity.setRemark(inventoryCheck.getRemark());
        updateEntity.setSnapshotTime(null);
        updateEntity.setUpdateTime(LocalDateTime.now());
        updateById(updateEntity);

        LambdaQueryWrapper<InventoryCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCheckItem::getCheckId, existing.getId());
        inventoryCheckItemService.remove(wrapper);
        saveItems(existing.getId(), inventoryCheck.getItems());

        return getDetailById(existing.getId());
    }

    @Override
    @Transactional
    public boolean submitInventoryCheck(Long id) {
        InventoryCheck inventoryCheck = getDetailById(id);
        if (!Integer.valueOf(0).equals(inventoryCheck.getStatus())) {
            throw new BusinessException("只有草稿状态的盘点单才可以提交审核");
        }

        LocalDateTime snapshotTime = LocalDateTime.now();
        List<InventoryCheckItem> refreshedItems = refreshSnapshotItems(id, inventoryCheck.getItems());
        inventoryCheck.setItems(refreshedItems);
        fillCheckSummary(inventoryCheck, refreshedItems);
        validateBeforeSubmit(refreshedItems);

        InventoryCheck updateEntity = new InventoryCheck();
        updateEntity.setId(id);
        updateEntity.setStatus(1);
        updateEntity.setSnapshotTime(snapshotTime);
        updateEntity.setUpdateTime(snapshotTime);
        return updateById(updateEntity);
    }

    @Override
    @Transactional
    public boolean auditInventoryCheck(Long id) {
        InventoryCheck inventoryCheck = getDetailById(id);
        if (!Integer.valueOf(1).equals(inventoryCheck.getStatus())) {
            throw new BusinessException("只有待审核状态的盘点单才可以审核");
        }
        if (inventoryCheck.getSnapshotTime() == null) {
            throw new BusinessException("盘点单尚未冻结账面快照，请重新提交后再审核");
        }

        CurrentOperatorResolver.CurrentOperator operator = currentOperatorResolver.resolve();
        if (inventoryCheck.getCreateBy() != null && inventoryCheck.getCreateBy().equals(operator.userId())) {
            throw new BusinessException("制单人不能审核自己的盘点单");
        }
        validateBeforeSubmit(inventoryCheck.getItems());

        LocalDateTime now = LocalDateTime.now();
        Map<Long, Product> productMap = loadProductMap(
                inventoryCheck.getItems().stream().map(InventoryCheckItem::getProductId).collect(Collectors.toList())
        );

        for (InventoryCheckItem item : inventoryCheck.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BusinessException("存在已删除商品，无法完成盘点审核");
            }

            int currentStock = safeInt(product.getStock());
            int snapshotStock = safeInt(item.getSystemStock());
            if (currentStock != snapshotStock) {
                throw new BusinessException("商品【" + product.getName() + "】在提交后库存已发生变化（快照"
                        + snapshotStock + "，当前" + currentStock + "），请重新盘点后再审核");
            }

            int difference = safeInt(item.getDifference());
            if (difference == 0) {
                continue;
            }

            int afterStock = snapshotStock + difference;
            if (afterStock < 0) {
                throw new BusinessException("商品【" + product.getName() + "】调整后库存不能小于 0");
            }
            if (!productService.updateStockIfMatches(product.getId(), currentStock, afterStock, now)) {
                throw new BusinessException("商品【" + product.getName() + "】在审核时库存已发生变化，请重新盘点后再审核");
            }

            product.setStock(afterStock);
            product.setUpdateTime(now);
            productWarningMessageService.syncWarningsForProduct(product);

            inventoryTransactionService.recordTransaction(
                    product.getId(),
                    difference > 0 ? 3 : 4,
                    Math.abs(difference),
                    currentStock,
                    afterStock,
                    inventoryCheck.getId(),
                    inventoryCheck.getCheckNo(),
                    buildAdjustmentRemark(inventoryCheck, item),
                    now
            );
        }

        InventoryCheck updateEntity = new InventoryCheck();
        updateEntity.setId(id);
        updateEntity.setStatus(2);
        updateEntity.setAuditBy(operator.userId());
        updateEntity.setAuditByName(operator.username());
        updateEntity.setReviewTime(now);
        updateEntity.setUpdateTime(now);
        return updateById(updateEntity);
    }

    @Override
    @Transactional
    public boolean cancelInventoryCheck(Long id) {
        InventoryCheck inventoryCheck = getById(id);
        if (inventoryCheck == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (Integer.valueOf(2).equals(inventoryCheck.getStatus())) {
            throw new BusinessException("已审核的盘点单不能作废");
        }
        if (Integer.valueOf(3).equals(inventoryCheck.getStatus())) {
            throw new BusinessException("该盘点单已作废");
        }

        InventoryCheck updateEntity = new InventoryCheck();
        updateEntity.setId(id);
        updateEntity.setStatus(3);
        updateEntity.setUpdateTime(LocalDateTime.now());
        return updateById(updateEntity);
    }

    @Override
    @Transactional
    public boolean removeInventoryCheck(Long id) {
        InventoryCheck inventoryCheck = getById(id);
        if (inventoryCheck == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (!Integer.valueOf(0).equals(inventoryCheck.getStatus())) {
            throw new BusinessException("只有草稿状态的盘点单才可以删除");
        }

        LambdaQueryWrapper<InventoryCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCheckItem::getCheckId, id);
        inventoryCheckItemService.remove(wrapper);
        return removeById(id);
    }

    @Override
    public Map<String, Object> getInventoryCheckDashboard() {
        LambdaQueryWrapper<InventoryCheck> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(InventoryCheck::getCreateTime);
        List<InventoryCheck> checks = list(queryWrapper);
        Map<Long, List<InventoryCheckItem>> itemMap = fillCheckSummaries(checks);

        List<InventoryCheckItem> allItems = itemMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        long totalChecks = checks.size();
        long pendingChecks = checks.stream().filter(item -> Integer.valueOf(1).equals(item.getStatus())).count();
        long approvedChecks = checks.stream().filter(item -> Integer.valueOf(2).equals(item.getStatus())).count();
        long differenceChecks = checks.stream().filter(item -> safeInt(item.getDifferenceItems()) > 0).count();
        int totalItems = checks.stream().mapToInt(item -> safeInt(item.getTotalItems())).sum();
        int totalDifferenceItems = checks.stream().mapToInt(item -> safeInt(item.getDifferenceItems())).sum();
        int pendingDifferenceItems = checks.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getStatus()))
                .mapToInt(item -> safeInt(item.getDifferenceItems()))
                .sum();
        int confirmedProfitQuantity = checks.stream()
                .filter(item -> Integer.valueOf(2).equals(item.getStatus()))
                .mapToInt(item -> safeInt(item.getProfitQuantity()))
                .sum();
        int confirmedLossQuantity = checks.stream()
                .filter(item -> Integer.valueOf(2).equals(item.getStatus()))
                .mapToInt(item -> safeInt(item.getLossQuantity()))
                .sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalChecks", totalChecks);
        summary.put("pendingChecks", pendingChecks);
        summary.put("approvedChecks", approvedChecks);
        summary.put("differenceChecks", differenceChecks);
        summary.put("totalItems", totalItems);
        summary.put("differenceItems", totalDifferenceItems);
        summary.put("pendingDifferenceItems", pendingDifferenceItems);
        summary.put("confirmedProfitQuantity", confirmedProfitQuantity);
        summary.put("confirmedLossQuantity", confirmedLossQuantity);
        summary.put("confirmedNetChangeQuantity", confirmedProfitQuantity - confirmedLossQuantity);
        summary.put("differenceRate", calculateRate(totalDifferenceItems, totalItems));

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("summary", summary);
        dashboard.put("modeDistribution", buildModeDistribution(checks));
        dashboard.put("reasonDistribution", buildReasonDistribution(allItems));
        dashboard.put("trend", inventoryTransactionService.buildTrend(14));
        dashboard.put("recentTransactions", inventoryTransactionService.listRecentTransactions(10, TRACKED_TRANSACTION_TYPES));
        return dashboard;
    }

    @Override
    public Map<String, Object> getInventoryCheckAnalysis(Long id) {
        InventoryCheck inventoryCheck = getDetailById(id);
        List<InventoryCheckItem> items = inventoryCheck.getItems() == null ? List.of() : inventoryCheck.getItems();
        List<InventoryTransaction> relatedTransactions = inventoryTransactionService
                .listTransactionsByRelated(inventoryCheck.getId(), inventoryCheck.getCheckNo());

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("checkNo", inventoryCheck.getCheckNo());
        overview.put("checkDate", inventoryCheck.getCheckDate());
        overview.put("status", inventoryCheck.getStatus());
        overview.put("checkMode", inventoryCheck.getCheckMode());
        overview.put("scopeName", inventoryCheck.getScopeName());
        overview.put("blindCheck", inventoryCheck.getBlindCheck());
        overview.put("snapshotTime", inventoryCheck.getSnapshotTime());
        overview.put("reviewTime", inventoryCheck.getReviewTime());
        overview.put("totalItems", safeInt(inventoryCheck.getTotalItems()));
        overview.put("differenceItems", safeInt(inventoryCheck.getDifferenceItems()));
        overview.put("profitQuantity", safeInt(inventoryCheck.getProfitQuantity()));
        overview.put("lossQuantity", safeInt(inventoryCheck.getLossQuantity()));
        overview.put("netChangeQuantity", safeInt(inventoryCheck.getNetChangeQuantity()));
        overview.put("differenceRate", calculateRate(inventoryCheck.getDifferenceItems(), inventoryCheck.getTotalItems()));

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("check", inventoryCheck);
        analysis.put("overview", overview);
        analysis.put("reasonDistribution", buildReasonDistribution(items));
        analysis.put("differenceRanking", buildDifferenceRanking(items));
        analysis.put("relatedTransactions", relatedTransactions);
        return analysis;
    }

    private void validateInventoryCheck(InventoryCheck inventoryCheck) {
        if (inventoryCheck.getCheckDate() == null) {
            throw new BusinessException("盘点日期不能为空");
        }
        if (inventoryCheck.getCheckMode() == null || !VALID_CHECK_MODES.contains(inventoryCheck.getCheckMode())) {
            throw new BusinessException("请选择有效的盘点方式");
        }
        if (inventoryCheck.getScopeName() == null || inventoryCheck.getScopeName().isBlank()) {
            throw new BusinessException("盘点范围不能为空");
        }
        inventoryCheck.setBlindCheck(normalizeBlindCheck(inventoryCheck.getBlindCheck()));

        if (inventoryCheck.getItems() == null || inventoryCheck.getItems().isEmpty()) {
            throw new BusinessException("请至少添加一条盘点明细");
        }

        Set<Long> productIds = new HashSet<>();
        int rowNumber = 1;
        for (InventoryCheckItem item : inventoryCheck.getItems()) {
            if (item.getProductId() == null) {
                throw new BusinessException("第" + rowNumber + " 行未选择商品");
            }
            if (item.getActualStock() == null || item.getActualStock() < 0) {
                throw new BusinessException("第" + rowNumber + " 行实盘数量必须大于等于 0");
            }
            if (item.getReasonType() != null && !VALID_REASON_TYPES.contains(item.getReasonType())) {
                throw new BusinessException("第" + rowNumber + " 行差异原因类型不正确");
            }
            if (!productIds.add(item.getProductId())) {
                throw new BusinessException("同一商品不能重复盘点");
            }
            rowNumber++;
        }
    }

    private void validateBeforeSubmit(List<InventoryCheckItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("盘点明细不能为空");
        }

        int rowNumber = 1;
        for (InventoryCheckItem item : items) {
            if (item.getActualStock() == null || item.getActualStock() < 0) {
                throw new BusinessException("第" + rowNumber + " 行实盘数量必须大于等于 0");
            }
            if (safeInt(item.getDifference()) != 0) {
                if (item.getReasonType() == null || !VALID_REASON_TYPES.contains(item.getReasonType())) {
                    throw new BusinessException("第" + rowNumber + " 行存在差异，请补充差异原因");
                }
                if (item.getRemark() == null || item.getRemark().isBlank()) {
                    throw new BusinessException("第" + rowNumber + " 行存在差异，请填写差异说明");
                }
            }
            rowNumber++;
        }
    }

    private void saveItems(Long checkId, List<InventoryCheckItem> items) {
        List<InventoryCheckItem> saveList = new ArrayList<>(items.size());
        LocalDateTime now = LocalDateTime.now();
        Map<Long, Product> productMap = loadProductMap(items.stream().map(InventoryCheckItem::getProductId).collect(Collectors.toList()));

        for (InventoryCheckItem item : items) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在，无法保存盘点明细");
            }

            int systemStock = safeInt(product.getStock());
            int actualStock = safeInt(item.getActualStock());
            int difference = actualStock - systemStock;

            InventoryCheckItem saveItem = new InventoryCheckItem();
            saveItem.setCheckId(checkId);
            saveItem.setProductId(product.getId());
            saveItem.setProductName(product.getName());
            saveItem.setSku(product.getSku());
            saveItem.setSystemStock(systemStock);
            saveItem.setActualStock(actualStock);
            saveItem.setDifference(difference);
            saveItem.setReasonType(difference == 0 ? null : item.getReasonType());
            saveItem.setRemark(item.getRemark());
            saveItem.setCreateTime(now);
            saveList.add(saveItem);
        }

        inventoryCheckItemService.saveBatch(saveList);
    }

    private List<InventoryCheckItem> refreshSnapshotItems(Long checkId, List<InventoryCheckItem> items) {
        LambdaQueryWrapper<InventoryCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCheckItem::getCheckId, checkId);
        inventoryCheckItemService.remove(wrapper);
        saveItems(checkId, items);
        return listItems(checkId);
    }

    private List<InventoryCheckItem> listItems(Long checkId) {
        LambdaQueryWrapper<InventoryCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCheckItem::getCheckId, checkId)
                .orderByAsc(InventoryCheckItem::getId);
        return inventoryCheckItemService.list(wrapper);
    }

    private Map<Long, List<InventoryCheckItem>> fillCheckSummaries(List<InventoryCheck> checks) {
        Map<Long, List<InventoryCheckItem>> itemMap = loadItemsByCheckIds(
                checks.stream().map(InventoryCheck::getId).collect(Collectors.toList())
        );

        for (InventoryCheck inventoryCheck : checks) {
            fillCheckSummary(inventoryCheck, itemMap.getOrDefault(inventoryCheck.getId(), List.of()));
        }
        return itemMap;
    }

    private Map<Long, Product> loadProductMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productService.mapByIds(productIds);
    }

    private Map<Long, List<InventoryCheckItem>> loadItemsByCheckIds(List<Long> checkIds) {
        if (checkIds == null || checkIds.isEmpty()) {
            return Map.of();
        }

        LambdaQueryWrapper<InventoryCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(InventoryCheckItem::getCheckId, checkIds)
                .orderByAsc(InventoryCheckItem::getCheckId)
                .orderByAsc(InventoryCheckItem::getId);
        return inventoryCheckItemService.list(wrapper).stream()
                .collect(Collectors.groupingBy(InventoryCheckItem::getCheckId, LinkedHashMap::new, Collectors.toList()));
    }

    private void fillCheckSummary(InventoryCheck inventoryCheck, List<InventoryCheckItem> items) {
        int totalItems = items.size();
        int differenceItems = 0;
        int profitQuantity = 0;
        int lossQuantity = 0;

        for (InventoryCheckItem item : items) {
            int difference = safeInt(item.getDifference());
            if (difference != 0) {
                differenceItems++;
            }
            if (difference > 0) {
                profitQuantity += difference;
            } else if (difference < 0) {
                lossQuantity += Math.abs(difference);
            }
        }

        inventoryCheck.setTotalItems(totalItems);
        inventoryCheck.setDifferenceItems(differenceItems);
        inventoryCheck.setProfitQuantity(profitQuantity);
        inventoryCheck.setLossQuantity(lossQuantity);
        inventoryCheck.setNetChangeQuantity(profitQuantity - lossQuantity);
    }

    private List<Map<String, Object>> buildModeDistribution(List<InventoryCheck> checks) {
        Map<Integer, Long> counter = checks.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getCheckMode() == null ? 0 : item.getCheckMode(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<Map<String, Object>> distribution = new ArrayList<>();
        counter.forEach((mode, value) -> distribution.add(buildDistributionItem(mode, getCheckModeText(mode), value.intValue())));
        return distribution;
    }

    private List<Map<String, Object>> buildReasonDistribution(List<InventoryCheckItem> items) {
        Map<Integer, Long> counter = items.stream()
                .filter(item -> safeInt(item.getDifference()) != 0)
                .collect(Collectors.groupingBy(
                        item -> item.getReasonType() == null ? 0 : item.getReasonType(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<Map<String, Object>> distribution = new ArrayList<>();
        counter.forEach((reasonType, value) -> distribution.add(
                buildDistributionItem(reasonType, getReasonTypeText(reasonType), value.intValue())
        ));
        return distribution;
    }

    private List<Map<String, Object>> buildDifferenceRanking(List<InventoryCheckItem> items) {
        return items.stream()
                .filter(item -> safeInt(item.getDifference()) != 0)
                .sorted(Comparator.comparingInt((InventoryCheckItem item) -> Math.abs(safeInt(item.getDifference()))).reversed())
                .limit(10)
                .map(item -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("productId", item.getProductId());
                    row.put("productName", item.getProductName());
                    row.put("sku", item.getSku());
                    row.put("systemStock", safeInt(item.getSystemStock()));
                    row.put("actualStock", safeInt(item.getActualStock()));
                    row.put("difference", safeInt(item.getDifference()));
                    row.put("reasonType", item.getReasonType());
                    row.put("reasonText", getReasonTypeText(item.getReasonType() == null ? 0 : item.getReasonType()));
                    row.put("remark", item.getRemark());
                    return row;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildDistributionItem(Integer key, String label, Integer value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    private double calculateRate(Integer numerator, Integer denominator) {
        if (denominator == null || denominator <= 0 || numerator == null || numerator <= 0) {
            return 0D;
        }
        return BigDecimal.valueOf((double) numerator * 100 / denominator)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int normalizeBlindCheck(Integer blindCheck) {
        if (blindCheck == null) {
            return 0;
        }
        if (blindCheck != 0 && blindCheck != 1) {
            throw new BusinessException("盲盘标记只能是 0 或 1");
        }
        return blindCheck;
    }

    private String buildAdjustmentRemark(InventoryCheck inventoryCheck, InventoryCheckItem item) {
        StringBuilder remark = new StringBuilder();
        remark.append(safeInt(item.getDifference()) > 0 ? "盘盈调整" : "盘亏调整");
        remark.append("，盘点方式：").append(getCheckModeText(inventoryCheck.getCheckMode()));
        remark.append("，盘点范围：").append(inventoryCheck.getScopeName());
        remark.append("，差异原因：").append(getReasonTypeText(item.getReasonType() == null ? 0 : item.getReasonType()));
        if (item.getRemark() != null && !item.getRemark().isBlank()) {
            remark.append("，说明：").append(item.getRemark().trim());
        }
        return remark.toString();
    }

    private String getCheckModeText(Integer checkMode) {
        if (checkMode == null) {
            return "未设置";
        }
        return switch (checkMode) {
            case 1 -> "静态盘点";
            case 2 -> "动态盘点";
            case 3 -> "循环盘点";
            case 4 -> "抽盘";
            default -> "未设置";
        };
    }

    private String getReasonTypeText(Integer reasonType) {
        if (reasonType == null || reasonType == 0) {
            return "待归因";
        }
        return switch (reasonType) {
            case 1 -> "流程执行问题";
            case 2 -> "记录与操作问题";
            case 3 -> "客观损耗";
            case 4 -> "其他";
            default -> "待归因";
        };
    }

    private String generateCheckNo() {
        return "IC"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }
}
