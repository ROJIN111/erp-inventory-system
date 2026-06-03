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

@Data
@TableName("inventory_check_item")
public class InventoryCheckItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("明细ID")
    @ColumnWidth(12)
    private Long id;

    @ExcelProperty("盘点单ID")
    @ColumnWidth(12)
    private Long checkId;

    @ExcelProperty("商品ID")
    @ColumnWidth(12)
    private Long productId;

    @ExcelProperty("商品名称")
    @ColumnWidth(20)
    private String productName;

    @ExcelProperty("商品编码")
    @ColumnWidth(15)
    private String sku;

    @ExcelProperty("系统库存")
    @ColumnWidth(12)
    private Integer systemStock;

    @ExcelProperty("实盘库存")
    @ColumnWidth(12)
    private Integer actualStock;

    @ExcelProperty("差异数量")
    @ColumnWidth(12)
    private Integer difference;

    @ExcelProperty("差异原因类型")
    @ColumnWidth(16)
    private Integer reasonType;

    @ExcelProperty("差异说明")
    @ColumnWidth(30)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
