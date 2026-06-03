package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erpinventory.entity.InventoryCheck;

import java.util.Map;

public interface InventoryCheckService extends IService<InventoryCheck> {

    Page<InventoryCheck> pageInventoryChecks(Integer pageNum, Integer pageSize, String keyword);

    InventoryCheck getDetailById(Long id);

    InventoryCheck createInventoryCheck(InventoryCheck inventoryCheck);

    InventoryCheck updateInventoryCheck(InventoryCheck inventoryCheck);

    boolean submitInventoryCheck(Long id);

    boolean auditInventoryCheck(Long id);

    boolean cancelInventoryCheck(Long id);

    boolean removeInventoryCheck(Long id);

    Map<String, Object> getInventoryCheckDashboard();

    Map<String, Object> getInventoryCheckAnalysis(Long id);
}
