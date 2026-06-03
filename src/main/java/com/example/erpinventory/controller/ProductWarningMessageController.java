package com.example.erpinventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.annotation.Log;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.dto.request.WarningHandleRequest;
import com.example.erpinventory.entity.ProductWarningMessage;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.service.ProductWarningMessageService;
import com.example.erpinventory.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/warning")
public class ProductWarningMessageController {

    @Autowired
    private ProductWarningMessageService productWarningMessageService;

    @Autowired
    private JwtUtil jwtUtil;

    @PreAuthorize("hasAuthority('warning:view')")
    @GetMapping("/page")
    public Result<Page<ProductWarningMessage>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer warningType,
            @RequestParam(required = false) String keyword) {
        return Result.success(productWarningMessageService.pageWarningMessages(pageNum, pageSize, status, warningType, keyword));
    }

    @PreAuthorize("hasAuthority('warning:view')")
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.success(productWarningMessageService.getWarningSummary());
    }

    @PreAuthorize("hasAuthority('warning:view')")
    @GetMapping("/{id}")
    public Result<ProductWarningMessage> getById(@PathVariable Long id) {
        return Result.success(productWarningMessageService.getById(id));
    }

    @PreAuthorize("hasAuthority('warning:config')")
    @Log("检查库存预警")
    @PostMapping("/check")
    public Result<Void> check() {
        productWarningMessageService.syncAllWarnings();
        return Result.success();
    }

    @PreAuthorize("hasAuthority('warning:handle')")
    @Log("处理预警消息")
    @PutMapping("/{id}/handle")
    public Result<Boolean> handle(@PathVariable Long id, @RequestBody WarningHandleRequest request, HttpServletRequest httpRequest) {
        CurrentOperator operator = resolveCurrentOperator(httpRequest);
        return Result.success(productWarningMessageService.handleWarning(id, request, operator.userId(), operator.username()));
    }

    @PreAuthorize("hasAuthority('warning:handle')")
    @Log("批量处理预警消息")
    @PutMapping("/handle-all")
    public Result<Boolean> handleAll(@RequestBody WarningHandleRequest request, HttpServletRequest httpRequest) {
        CurrentOperator operator = resolveCurrentOperator(httpRequest);
        return Result.success(productWarningMessageService.handleAllWarnings(request, operator.userId(), operator.username()));
    }

    @PreAuthorize("hasAuthority('warning:handle')")
    @Log("删除预警消息")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productWarningMessageService.removeById(id));
    }

    private CurrentOperator resolveCurrentOperator(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未获取到登录信息，请重新登录");
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.extractClaims(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            return new CurrentOperator(userId, username);
        } catch (Exception ex) {
            throw new BusinessException("登录信息已失效，请重新登录");
        }
    }

    private record CurrentOperator(Long userId, String username) {
    }
}
