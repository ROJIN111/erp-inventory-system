package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erpinventory.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    Page<OperationLog> pageLogs(Integer pageNum, Integer pageSize);
}
