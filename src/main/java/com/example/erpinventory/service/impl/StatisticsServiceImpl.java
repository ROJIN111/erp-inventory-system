package com.example.erpinventory.service.impl;

import com.example.erpinventory.mapper.InboundOrderMapper;
import com.example.erpinventory.mapper.OutboundOrderMapper;
import com.example.erpinventory.mapper.ProductMapper;
import com.example.erpinventory.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InboundOrderMapper inboundOrderMapper;

    @Autowired
    private OutboundOrderMapper outboundOrderMapper;

    @Override
    public Map<String, Object> getProductStats() {
        Map<String, Object> summary = productMapper.selectProductAndInventorySummary(LOW_STOCK_THRESHOLD);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProducts", toLong(summary.get("totalProducts")));
        stats.put("lowStockCount", toLong(summary.get("lowStockCount")));
        stats.put("categoryDistribution", buildCategoryDistribution(productMapper.selectCategoryStats()));
        return stats;
    }

    @Override
    public Map<String, Object> getInventoryStats() {
        Map<String, Object> summary = productMapper.selectProductAndInventorySummary(LOW_STOCK_THRESHOLD);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalStock", toInt(summary.get("totalStock")));
        stats.put("totalValue", toBigDecimal(summary.get("totalValue")));
        stats.put("lowStockProducts", buildLowStockProducts(productMapper.selectLowStockProducts(LOW_STOCK_THRESHOLD)));
        return stats;
    }

    @Override
    public Map<String, Object> getInboundStats() {
        Map<String, Object> summary = inboundOrderMapper.selectCompletedInboundStats();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalInbound", toLong(summary.get("totalInbound")));
        stats.put("totalQuantity", toInt(summary.get("totalQuantity")));
        stats.put("totalAmount", toBigDecimal(summary.get("totalAmount")));
        return stats;
    }

    @Override
    public Map<String, Object> getOutboundStats() {
        Map<String, Object> summary = outboundOrderMapper.selectCompletedOutboundStats();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalOutbound", toLong(summary.get("totalOutbound")));
        stats.put("totalQuantity", toInt(summary.get("totalQuantity")));
        stats.put("totalAmount", toBigDecimal(summary.get("totalAmount")));
        return stats;
    }

    private Map<String, Long> buildCategoryDistribution(List<Map<String, Object>> rows) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String categoryName = Objects.toString(row.get("categoryName"), "未分类");
            distribution.put(categoryName, toLong(row.get("categoryCount")));
        }
        return distribution;
    }

    private List<Map<String, Object>> buildLowStockProducts(List<Map<String, Object>> rows) {
        List<Map<String, Object>> products = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", Objects.toString(row.get("name"), "--"));
            item.put("stock", toInt(row.get("stock")));
            products.add(item);
        }
        return products;
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}
