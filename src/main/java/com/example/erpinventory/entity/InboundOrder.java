package com.example.erpinventory.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erpinventory.converter.OrderStatusConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inbound_order")
public class InboundOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("ID")
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty("入库单号")
    @ColumnWidth(20)
    private String orderNo;

    @ExcelProperty("商品ID")
    @ColumnWidth(12)
    private Long productId;

    @ExcelProperty("数量")
    @ColumnWidth(10)
    private Integer quantity;

    @ExcelProperty("单价")
    @ColumnWidth(12)
    private BigDecimal price;

    @ExcelProperty("总金额")
    @ColumnWidth(12)
    private BigDecimal totalAmount;

    @ExcelProperty("供应商")
    @ColumnWidth(15)
    private String supplier;

    @ExcelProperty(value = "状态", converter = OrderStatusConverter.class)
    @ColumnWidth(10)
    private Integer status;

    @TableField("create_by")
    @ExcelProperty("制单人ID")
    @ColumnWidth(12)
    private Long createBy;

    @TableField("create_by_name")
    @ExcelProperty("制单人")
    @ColumnWidth(15)
    private String createByName;

    @TableField("audit_by")
    @ExcelProperty("审核人ID")
    @ColumnWidth(12)
    private Long auditBy;

    @TableField("audit_by_name")
    @ExcelProperty("审核人")
    @ColumnWidth(15)
    private String auditByName;

    @TableField("audit_time")
    @ExcelProperty("审核时间")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelProperty("更新时间")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @ExcelProperty("商品名称")
    @ColumnWidth(15)
    private String productName;
}
