package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erpinventory.dto.request.WarningHandleRequest;
import com.example.erpinventory.entity.Product;
import com.example.erpinventory.entity.ProductWarningMessage;

import java.util.Map;

public interface ProductWarningMessageService extends IService<ProductWarningMessage> {

    Page<ProductWarningMessage> pageWarningMessages(Integer pageNum, Integer pageSize, Integer status, Integer warningType, String keyword);

    Map<String, Object> getWarningSummary();

    void syncAllWarnings();

    void syncWarningsForProduct(Product product);

    boolean handleWarning(Long id, WarningHandleRequest request, Long currentUserId, String currentUsername);

    boolean handleAllWarnings(WarningHandleRequest request, Long currentUserId, String currentUsername);

    void removeWarningsByProductId(Long productId);
}
