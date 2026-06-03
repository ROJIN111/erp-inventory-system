package com.example.erpinventory.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erpinventory.entity.InboundOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface InboundOrderMapper extends BaseMapper<InboundOrder> {

    @Select("SELECT io.*, p.name as product_name FROM inbound_order io LEFT JOIN product p ON io.product_id = p.id ORDER BY io.create_time DESC")
    List<InboundOrder> selectListWithProductName();

    @Select("SELECT io.*, p.name as product_name FROM inbound_order io LEFT JOIN product p ON io.product_id = p.id ${ew.customSqlSegment} ORDER BY io.create_time DESC")
    Page<InboundOrder> selectPageWithProductName(Page<InboundOrder> page, @Param("ew") LambdaQueryWrapper<InboundOrder> queryWrapper);

    @Select("SELECT COUNT(*) as totalInbound, COALESCE(SUM(quantity), 0) as totalQuantity, COALESCE(SUM(total_amount), 0) as totalAmount FROM inbound_order WHERE status = 1")
    Map<String, Object> selectCompletedInboundStats();
}
