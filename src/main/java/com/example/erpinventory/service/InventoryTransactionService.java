package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erpinventory.entity.InventoryTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InventoryTransactionService extends IService<InventoryTransaction> {

    Page<InventoryTransaction> pageTransactions(Integer pageNum, Integer pageSize,
                                               Long productId, Integer transactionType,
                                               String sku, String category,
                                               String relatedNo, String operatorName,
                                               String keyword,
                                               LocalDateTime startTime, LocalDateTime endTime);

    List<InventoryTransaction> listTransactions(Long productId, Integer transactionType,
                                                String sku, String category,
                                                String relatedNo, String operatorName,
                                                String keyword,
                                                LocalDateTime startTime, LocalDateTime endTime);

    void recordTransaction(Long productId, Integer transactionType, Integer quantity,
                           Integer beforeStock, Integer afterStock,
                           Long relatedId, String relatedNo, String remark,
                           LocalDateTime operationTime);

    List<InventoryTransaction> listRecentTransactions(int limit, List<Integer> transactionTypes);

    List<InventoryTransaction> listTransactionsByRelated(Long relatedId, String relatedNo);

    List<Map<String, Object>> buildTrend(int days);
}
