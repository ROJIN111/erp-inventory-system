package com.example.erpinventory.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erpinventory.entity.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {

    @Select("SELECT oo.*, p.name as product_name FROM outbound_order oo LEFT JOIN product p ON oo.product_id = p.id ORDER BY oo.create_time DESC")
    List<OutboundOrder> selectListWithProductName();

    @Select("SELECT oo.*, p.name as product_name FROM outbound_order oo LEFT JOIN product p ON oo.product_id = p.id ${ew.customSqlSegment} ORDER BY oo.create_time DESC")
    Page<OutboundOrder> selectPageWithProductName(Page<OutboundOrder> page, @Param("ew") LambdaQueryWrapper<OutboundOrder> queryWrapper);

    @Select("SELECT COUNT(*) as totalOutbound, COALESCE(SUM(quantity), 0) as totalQuantity, COALESCE(SUM(total_amount), 0) as totalAmount FROM outbound_order WHERE status = 1")
    Map<String, Object> selectCompletedOutboundStats();
}
