package com.example.erpinventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.annotation.Log;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.entity.OutboundOrder;
import com.example.erpinventory.service.OutboundOrderService;
import com.example.erpinventory.util.PermissionGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbound")
public class OutboundOrderController {

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private PermissionGuard permissionGuard;

    @PreAuthorize("hasAuthority('outbound:view')")
    @GetMapping("/list")
    public Result<List<OutboundOrder>> list() {
        permissionGuard.require("outbound:view");
        List<OutboundOrder> list = outboundOrderService.listOutboundOrders();
        return Result.success(list);
    }

    @PreAuthorize("hasAuthority('outbound:view')")
    @GetMapping("/page")
    public Result<Page<OutboundOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        permissionGuard.require("outbound:view");
        Page<OutboundOrder> page = outboundOrderService.pageOutboundOrders(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PreAuthorize("hasAuthority('outbound:view')")
    @GetMapping("/{id}")
    public Result<OutboundOrder> getById(@PathVariable Long id) {
        permissionGuard.require("outbound:view");
        OutboundOrder order = outboundOrderService.getById(id);
        return Result.success(order);
    }

    @PreAuthorize("hasAuthority('outbound:create')")
    @Log("新增出库单")
    @PostMapping
    public Result<OutboundOrder> create(@RequestBody OutboundOrder outboundOrder) {
        permissionGuard.require("outbound:create");
        OutboundOrder result = outboundOrderService.createOutboundOrder(outboundOrder);
        return Result.success(result);
    }

    @PreAuthorize("hasAuthority('outbound:audit')")
    @Log("审核出库单")
    @PutMapping("/{id}/audit")
    public Result<Boolean> audit(@PathVariable Long id) {
        permissionGuard.require("outbound:audit");
        boolean result = outboundOrderService.auditOutboundOrder(id);
        return Result.success(result);
    }

    @PreAuthorize("hasAuthority('outbound:delete')")
    @Log("删除出库单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        permissionGuard.require("outbound:delete");
        boolean result = outboundOrderService.removeById(id);
        return Result.success(result);
    }
}
