package com.example.erpinventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.annotation.Log;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.entity.InventoryCheck;
import com.example.erpinventory.service.InventoryCheckService;
import com.example.erpinventory.util.PermissionGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory-check")
public class InventoryCheckController {

    @Autowired
    private InventoryCheckService inventoryCheckService;

    @Autowired
    private PermissionGuard permissionGuard;

    @PreAuthorize("hasAuthority('inventory_check:view')")
    @GetMapping("/page")
    public Result<Page<InventoryCheck>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        permissionGuard.require("inventory_check:view");
        return Result.success(inventoryCheckService.pageInventoryChecks(pageNum, pageSize, keyword));
    }

    @PreAuthorize("hasAuthority('inventory_check:view')")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        permissionGuard.require("inventory_check:view");
        return Result.success(inventoryCheckService.getInventoryCheckDashboard());
    }

    @PreAuthorize("hasAuthority('inventory_check:view')")
    @GetMapping("/{id}")
    public Result<InventoryCheck> getById(@PathVariable Long id) {
        permissionGuard.require("inventory_check:view");
        return Result.success(inventoryCheckService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('inventory_check:view')")
    @GetMapping("/{id}/analysis")
    public Result<Map<String, Object>> analysis(@PathVariable Long id) {
        permissionGuard.require("inventory_check:view");
        return Result.success(inventoryCheckService.getInventoryCheckAnalysis(id));
    }

    @PreAuthorize("hasAuthority('inventory_check:create')")
    @Log("创建盘点单")
    @PostMapping
    public Result<InventoryCheck> create(@RequestBody InventoryCheck inventoryCheck) {
        permissionGuard.require("inventory_check:create");
        return Result.success(inventoryCheckService.createInventoryCheck(inventoryCheck));
    }

    @PreAuthorize("hasAuthority('inventory_check:update')")
    @Log("修改盘点单")
    @PutMapping
    public Result<InventoryCheck> update(@RequestBody InventoryCheck inventoryCheck) {
        permissionGuard.require("inventory_check:update");
        return Result.success(inventoryCheckService.updateInventoryCheck(inventoryCheck));
    }

    @PreAuthorize("hasAuthority('inventory_check:submit')")
    @Log("提交盘点单审核")
    @PutMapping("/{id}/submit")
    public Result<Boolean> submit(@PathVariable Long id) {
        permissionGuard.require("inventory_check:submit");
        return Result.success(inventoryCheckService.submitInventoryCheck(id));
    }

    @PreAuthorize("hasAuthority('inventory_check:audit')")
    @Log("审核盘点单")
    @PutMapping("/{id}/audit")
    public Result<Boolean> audit(@PathVariable Long id) {
        permissionGuard.require("inventory_check:audit");
        return Result.success(inventoryCheckService.auditInventoryCheck(id));
    }

    @PreAuthorize("hasAuthority('inventory_check:cancel')")
    @Log("作废盘点单")
    @PutMapping("/{id}/cancel")
    public Result<Boolean> cancel(@PathVariable Long id) {
        permissionGuard.require("inventory_check:cancel");
        return Result.success(inventoryCheckService.cancelInventoryCheck(id));
    }

    @PreAuthorize("hasAuthority('inventory_check:delete')")
    @Log("删除盘点单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        permissionGuard.require("inventory_check:delete");
        return Result.success(inventoryCheckService.removeInventoryCheck(id));
    }
}
