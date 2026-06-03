package com.example.erpinventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.entity.OperationLog;
import com.example.erpinventory.service.OperationLogService;
import com.example.erpinventory.util.PermissionGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private PermissionGuard permissionGuard;

    @PreAuthorize("hasAuthority('log:view')")
    @GetMapping("/page")
    public Result<Page<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionGuard.require("log:view");
        Page<OperationLog> page = operationLogService.pageLogs(pageNum, pageSize);
        return Result.success(page);
    }

    @PreAuthorize("hasAuthority('log:delete')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        permissionGuard.require("log:delete");
        boolean result = operationLogService.removeById(id);
        return Result.success(result);
    }
}
