package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erpinventory.entity.Product;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {

    Product getBySku(String sku);

    List<Product> listProducts();

    Page<Product> pageProducts(Integer pageNum, Integer pageSize, String keyword);

    List<Product> listByCategory(String category);

    Map<Long, Product> mapByIds(Collection<Long> productIds);

    boolean updateStockIfMatches(Long productId, Integer expectedStock, Integer targetStock, LocalDateTime updateTime);
}
