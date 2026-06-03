package com.example.erpinventory.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.dto.response.InventoryTransactionExportRow;
import com.example.erpinventory.entity.InventoryTransaction;
import com.example.erpinventory.service.InventoryTransactionService;
import com.example.erpinventory.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory-transaction")
public class InventoryTransactionController {

    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasAuthority('inventory_flow:view')")
    @GetMapping("/page")
    public Result<Page<InventoryTransaction>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer transactionType,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String relatedNo,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(
                inventoryTransactionService.pageTransactions(
                        pageNum, pageSize, productId, transactionType,
                        sku, category, relatedNo, operatorName, keyword,
                        startTime, endTime
                )
        );
    }

    @PreAuthorize("hasAuthority('inventory_flow:view')")
    @GetMapping("/{id}")
    public Result<InventoryTransaction> getById(@PathVariable Long id) {
        return Result.success(inventoryTransactionService.getById(id));
    }

    @PreAuthorize("hasAuthority('inventory_flow:export')")
    @GetMapping("/export")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer transactionType,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String relatedNo,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        String fileName = URLEncoder.encode("库存流水账", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<InventoryTransaction> transactions = inventoryTransactionService
                .listTransactions(productId, transactionType, sku, category, relatedNo, operatorName, keyword, startTime, endTime);
        List<InventoryTransactionExportRow> exportRows = buildExportRows(transactions);

        EasyExcel.write(response.getOutputStream(), InventoryTransactionExportRow.class)
                .sheet("库存流水明细")
                .doWrite(exportRows);
    }

    private List<InventoryTransactionExportRow> buildExportRows(List<InventoryTransaction> transactions) {
        Map<Long, String> categoryMap = loadCategoryMap(transactions);
        return transactions.stream().map(item -> {
            InventoryTransactionExportRow row = new InventoryTransactionExportRow();
            row.setTransactionNo(item.getTransactionNo());
            row.setProductName(item.getProductName());
            row.setSku(item.getSku());
            row.setCategory(categoryMap.getOrDefault(item.getProductId(), "--"));
            row.setTransactionTypeText(getTransactionTypeText(item.getTransactionType()));
            row.setQuantity(item.getQuantity());
            row.setBeforeStock(item.getBeforeStock());
            row.setAfterStock(item.getAfterStock());
            row.setRelatedNo(item.getRelatedNo());
            row.setOperatorName(item.getOperatorName());
            row.setRemark(item.getRemark());
            row.setCreateTime(item.getCreateTime());
            return row;
        }).toList();
    }

    private Map<Long, String> loadCategoryMap(List<InventoryTransaction> transactions) {
        Collection<Long> productIds = transactions.stream()
                .map(InventoryTransaction::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (productIds.isEmpty()) {
            return Map.of();
        }

        return productService.listByIds(productIds).stream()
                .filter(product -> product.getId() != null)
                .collect(Collectors.toMap(product -> product.getId(), product -> {
                    if (product.getCategory() == null || product.getCategory().isBlank()) {
                        return "--";
                    }
                    return product.getCategory();
                }));
    }

    private String getTransactionTypeText(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "入库";
            case 2 -> "出库";
            case 3 -> "盘盈调整";
            case 4 -> "盘亏调整";
            default -> "未知";
        };
    }
}
