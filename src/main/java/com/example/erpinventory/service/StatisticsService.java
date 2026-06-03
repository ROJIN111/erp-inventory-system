package com.example.erpinventory.service;

import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getProductStats();

    Map<String, Object> getInventoryStats();

    Map<String, Object> getInboundStats();

    Map<String, Object> getOutboundStats();
}
