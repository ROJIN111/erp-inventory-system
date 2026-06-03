package com.example.erpinventory.controller;

import com.example.erpinventory.common.Result;
import com.example.erpinventory.entity.SysRole;
import com.example.erpinventory.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PreAuthorize("hasAuthority('role:view')")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.listActiveRoles());
    }
}
