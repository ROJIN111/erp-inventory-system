package com.example.erpinventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.mapper.ProductMapper;
import com.example.erpinventory.service.ProductService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Product getBySku(String sku) {
        return baseMapper.selectBySku(sku);
    }

    @Override
    public List<Product> listProducts() {
        return list();
    }

    @Override
    public Page<Product> pageProducts(Integer pageNum, Integer pageSize, String keyword) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Product::getName, keyword)
                    .or()
                    .like(Product::getSku, keyword)
                    .or()
                    .like(Product::getCategory, keyword);
        }
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Product> listByCategory(String category) {
        return baseMapper.selectByCategory(category);
    }

    @Override
    public Map<Long, Product> mapByIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return listByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product, (left, _right) -> left, LinkedHashMap::new));
    }

    @Override
    public boolean updateStockIfMatches(Long productId, Integer expectedStock, Integer targetStock, LocalDateTime updateTime) {
        return baseMapper.updateStockIfMatches(productId, expectedStock, targetStock, updateTime) > 0;
    }
}
