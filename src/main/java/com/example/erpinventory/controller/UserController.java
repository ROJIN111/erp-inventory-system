package com.example.erpinventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.annotation.Log;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.dto.request.UserLoginRequest;
import com.example.erpinventory.dto.response.UserInfoResponse;
import com.example.erpinventory.entity.SysUser;
import com.example.erpinventory.service.AuthService;
import com.example.erpinventory.service.UserService;
import com.example.erpinventory.util.JwtUtil;
import com.example.erpinventory.util.PermissionGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PermissionGuard permissionGuard;

    @Log("用户登录")
    @PostMapping("/login")
    public Result<UserInfoResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Log("用户注册")
    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody SysUser user) {
        return Result.success(userService.register(user));
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> info(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        return Result.success(UserInfoResponse.fromAuthInfo(authService.loadUserAuthByUserId(userId), token));
    }

    @PreAuthorize("hasAuthority('user:view')")
    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        permissionGuard.require("user:view");
        return Result.success(userService.listUsers());
    }

    @PreAuthorize("hasAuthority('user:view')")
    @GetMapping("/page")
    public Result<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionGuard.require("user:view");
        return Result.success(userService.pageUsers(pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('user:view')")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        permissionGuard.require("user:view");
        return Result.success(userService.getById(id));
    }

    @PreAuthorize("hasAuthority('user:create')")
    @Log("新增用户")
    @PostMapping
    public Result<Boolean> save(@RequestBody SysUser user) {
        permissionGuard.require("user:create");
        return Result.success(userService.save(user));
    }

    @PreAuthorize("hasAuthority('user:update')")
    @Log("更新用户")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysUser user) {
        permissionGuard.require("user:update");
        if (user.getId() != null && user.getId().equals(1L)) {
            SysUser existingUser = userService.getById(1L);
            if (existingUser != null && "admin".equals(existingUser.getUsername())) {
                boolean modifyingCriticalFields = false;
                StringBuilder modifiedFields = new StringBuilder();

                if (user.getUsername() != null && !user.getUsername().equals(existingUser.getUsername())) {
                    modifyingCriticalFields = true;
                    modifiedFields.append("用户名、");
                }
                if (user.getRoleId() != null && !user.getRoleId().equals(existingUser.getRoleId())) {
                    modifyingCriticalFields = true;
                    modifiedFields.append("角色、");
                }
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    modifyingCriticalFields = true;
                    modifiedFields.append("密码、");
                }
                if (user.getStatus() != null && user.getStatus() == 0) {
                    modifyingCriticalFields = true;
                    modifiedFields.append("状态、");
                }

                if (modifyingCriticalFields) {
                    if (modifiedFields.length() > 0 && modifiedFields.charAt(modifiedFields.length() - 1) == '、') {
                        modifiedFields.deleteCharAt(modifiedFields.length() - 1);
                    }
                    return Result.error(400, "不能修改超级管理员的" + modifiedFields);
                }
            }
        }
        return Result.success(userService.updateById(user));
    }

    @PreAuthorize("hasAuthority('user:delete')")
    @Log("删除用户")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        permissionGuard.require("user:delete");

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long currentUserId = jwtUtil.extractUserId(token);
            if (currentUserId != null && currentUserId.equals(id)) {
                return Result.error(400, "不能删除当前登录的账号");
            }
        }

        if (id != null && id.equals(1L)) {
            SysUser existingUser = userService.getById(1L);
            if (existingUser != null && "admin".equals(existingUser.getUsername())) {
                return Result.error(400, "不能删除超级管理员账号");
            }
        }

        return Result.success(userService.physicallyDeleteById(id));
    }
}
