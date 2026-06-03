package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erpinventory.entity.InboundOrder;

import java.util.List;

public interface InboundOrderService extends IService<InboundOrder> {

    Page<InboundOrder> pageInboundOrders(Integer pageNum, Integer pageSize, String keyword);

    List<InboundOrder> listInboundOrders();

    InboundOrder createInboundOrder(InboundOrder inboundOrder);

    boolean auditInboundOrder(Long id);
}
