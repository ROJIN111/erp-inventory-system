package com.example.erpinventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.entity.InboundOrder;
import com.example.erpinventory.entity.OutboundOrder;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.service.InboundOrderService;
import com.example.erpinventory.service.OutboundOrderService;
import com.example.erpinventory.service.ProductService;
import com.example.erpinventory.util.PermissionGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private PermissionGuard permissionGuard;

    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping("/product/stats")
    public Result<Map<String, Object>> getProductStats() {
        permissionGuard.require("dashboard:view");
        Map<String, Object> stats = new HashMap<>();

        List<Product> products = productService.list();
        long totalProducts = products.size();
        long lowStockCount = products.stream()
                .filter(product -> product.getStock() != null && product.getStock() < 10)
                .count();

        Map<String, Long> categoryCount = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        stats.put("totalProducts", totalProducts);
        stats.put("lowStockCount", lowStockCount);
        stats.put("categoryDistribution", categoryCount);
        return Result.success(stats);
    }

    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping("/inventory/stats")
    public Result<Map<String, Object>> getInventoryStats() {
        permissionGuard.require("dashboard:view");
        Map<String, Object> stats = new HashMap<>();

        List<Product> products = productService.list();
        int totalStock = products.stream()
                .mapToInt(product -> product.getStock() == null ? 0 : product.getStock())
                .sum();

        BigDecimal totalValue = products.stream()
                .filter(product -> product.getPrice() != null && product.getStock() != null)
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> lowStockProducts = products.stream()
                .filter(product -> product.getStock() != null && product.getStock() < 10)
                .map(product -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", product.getName());
                    item.put("stock", product.getStock());
                    return item;
                })
                .collect(Collectors.toList());

        stats.put("totalStock", totalStock);
        stats.put("totalValue", totalValue);
        stats.put("lowStockProducts", lowStockProducts);
        return Result.success(stats);
    }

    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping("/inbound/stats")
    public Result<Map<String, Object>> getInboundStats() {
        permissionGuard.require("dashboard:view");
        Map<String, Object> stats = new HashMap<>();

        QueryWrapper<InboundOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        List<InboundOrder> completedOrders = inboundOrderService.list(queryWrapper);

        long totalInbound = completedOrders.size();
        int totalQuantity = completedOrders.stream().mapToInt(InboundOrder::getQuantity).sum();
        BigDecimal totalAmount = completedOrders.stream()
                .map(InboundOrder::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalInbound", totalInbound);
        stats.put("totalQuantity", totalQuantity);
        stats.put("totalAmount", totalAmount);
        return Result.success(stats);
    }

    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping("/outbound/stats")
    public Result<Map<String, Object>> getOutboundStats() {
        permissionGuard.require("dashboard:view");
        Map<String, Object> stats = new HashMap<>();

        QueryWrapper<OutboundOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        List<OutboundOrder> completedOrders = outboundOrderService.list(queryWrapper);

        long totalOutbound = completedOrders.size();
        int totalQuantity = completedOrders.stream().mapToInt(OutboundOrder::getQuantity).sum();
        BigDecimal totalAmount = completedOrders.stream()
                .map(OutboundOrder::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalOutbound", totalOutbound);
        stats.put("totalQuantity", totalQuantity);
        stats.put("totalAmount", totalAmount);
        return Result.success(stats);
    }
}
