package com.example.erpinventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.annotation.Log;
import com.example.erpinventory.common.Result;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.service.ProductService;
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

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private PermissionGuard permissionGuard;

    @PreAuthorize("hasAuthority('product:view')")
    @GetMapping("/list")
    public Result<List<Product>> list() {
        permissionGuard.require("product:view");
        return Result.success(productService.listProducts());
    }

    @PreAuthorize("hasAuthority('product:view')")
    @GetMapping("/page")
    public Result<Page<Product>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        permissionGuard.require("product:view");
        return Result.success(productService.pageProducts(pageNum, pageSize, keyword));
    }

    @PreAuthorize("hasAuthority('product:view')")
    @GetMapping("/sku/{sku}")
    public Result<Product> getBySku(@PathVariable String sku) {
        permissionGuard.require("product:view");
        return Result.success(productService.getBySku(sku));
    }

    @PreAuthorize("hasAuthority('product:view')")
    @GetMapping("/category/{category}")
    public Result<List<Product>> listByCategory(@PathVariable String category) {
        permissionGuard.require("product:view");
        return Result.success(productService.listByCategory(category));
    }

    @PreAuthorize("hasAuthority('product:view')")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        permissionGuard.require("product:view");
        return Result.success(productService.getById(id));
    }

    @PreAuthorize("hasAuthority('product:create')")
    @Log("新增商品")
    @PostMapping
    public Result<Boolean> save(@RequestBody Product product) {
        permissionGuard.require("product:create");
        return Result.success(productService.save(product));
    }

    @PreAuthorize("hasAuthority('product:update')")
    @Log("更新商品")
    @PutMapping
    public Result<Boolean> update(@RequestBody Product product) {
        permissionGuard.require("product:update");
        return Result.success(productService.updateById(product));
    }

    @PreAuthorize("hasAuthority('product:delete')")
    @Log("删除商品")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        permissionGuard.require("product:delete");
        return Result.success(productService.removeById(id));
    }
}
