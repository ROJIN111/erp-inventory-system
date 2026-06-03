package com.example.erpinventory.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("inventory_check")
public class InventoryCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("盘点单ID")
    @ColumnWidth(12)
    private Long id;

    @ExcelProperty("盘点单号")
    @ColumnWidth(20)
    private String checkNo;

    @ExcelProperty("盘点日期")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkDate;

    @ExcelProperty("盘点方式")
    @ColumnWidth(16)
    private Integer checkMode;

    @ExcelProperty("盘点范围")
    @ColumnWidth(20)
    private String scopeName;

    @ExcelProperty("盲盘")
    @ColumnWidth(10)
    private Integer blindCheck;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private Integer status;

    @ExcelProperty("制单人")
    @ColumnWidth(15)
    private String createByName;

    @ExcelProperty("审核人ID")
    @ColumnWidth(12)
    private Long auditBy;

    @ExcelProperty("审核人")
    @ColumnWidth(15)
    private String auditByName;

    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

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

    @ExcelProperty("审核时间")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    @ExcelProperty("snapshot_time")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime snapshotTime;

    @TableField(exist = false)
    private List<InventoryCheckItem> items;

    @TableField(exist = false)
    private Integer totalItems;

    @TableField(exist = false)
    private Integer differenceItems;

    @TableField(exist = false)
    private Integer profitQuantity;

    @TableField(exist = false)
    private Integer lossQuantity;

    @TableField(exist = false)
    private Integer netChangeQuantity;
}
