package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erpinventory.entity.OutboundOrder;

import java.util.List;

public interface OutboundOrderService extends IService<OutboundOrder> {

    Page<OutboundOrder> pageOutboundOrders(Integer pageNum, Integer pageSize, String keyword);

    List<OutboundOrder> listOutboundOrders();

    OutboundOrder createOutboundOrder(OutboundOrder outboundOrder);

    boolean auditOutboundOrder(Long id);
}
