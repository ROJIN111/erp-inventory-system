package com.example.erpinventory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erpinventory.entity.InventoryCheckItem;
import com.example.erpinventory.mapper.InventoryCheckItemMapper;
import com.example.erpinventory.service.InventoryCheckItemService;
import org.springframework.stereotype.Service;

@Service
public class InventoryCheckItemServiceImpl extends ServiceImpl<InventoryCheckItemMapper, InventoryCheckItem>
        implements InventoryCheckItemService {
}
