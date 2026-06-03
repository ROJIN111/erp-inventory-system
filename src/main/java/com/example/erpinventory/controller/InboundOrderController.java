package com.example.erpinventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.annotation.Log;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.entity.InboundOrder;
import com.example.erpinventory.service.InboundOrderService;
import com.example.erpinventory.util.PermissionGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound")
public class InboundOrderController {

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private PermissionGuard permissionGuard;

    @PreAuthorize("hasAuthority('inbound:view')")
    @GetMapping("/list")
    public Result<List<InboundOrder>> list() {
        permissionGuard.require("inbound:view");
        List<InboundOrder> list = inboundOrderService.listInboundOrders();
        return Result.success(list);
    }

    @PreAuthorize("hasAuthority('inbound:view')")
    @GetMapping("/page")
    public Result<Page<InboundOrder>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        permissionGuard.require("inbound:view");
        Page<InboundOrder> page = inboundOrderService.pageInboundOrders(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PreAuthorize("hasAuthority('inbound:view')")
    @GetMapping("/{id}")
    public Result<InboundOrder> getById(@PathVariable Long id) {
        permissionGuard.require("inbound:view");
        InboundOrder order = inboundOrderService.getById(id);
        return Result.success(order);
    }

    @PreAuthorize("hasAuthority('inbound:create')")
    @Log("新增入库单")
    @PostMapping
    public Result<InboundOrder> create(@RequestBody InboundOrder inboundOrder) {
        permissionGuard.require("inbound:create");
        InboundOrder result = inboundOrderService.createInboundOrder(inboundOrder);
        return Result.success(result);
    }

    @PreAuthorize("hasAuthority('inbound:audit')")
    @Log("审核入库单")
    @PutMapping("/{id}/audit")
    public Result<Boolean> audit(@PathVariable Long id) {
        permissionGuard.require("inbound:audit");
        boolean result = inboundOrderService.auditInboundOrder(id);
        return Result.success(result);
    }

    @PreAuthorize("hasAuthority('inbound:delete')")
    @Log("删除入库单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        permissionGuard.require("inbound:delete");
        boolean result = inboundOrderService.removeById(id);
        return Result.success(result);
    }
}
