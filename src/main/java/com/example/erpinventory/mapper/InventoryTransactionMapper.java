package com.example.erpinventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erpinventory.entity.InventoryTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {
}
