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
@TableName("inventory_transaction")
public class InventoryTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("流水ID")
    @ColumnWidth(12)
    private Long id;

    @ExcelProperty("流水号")
    @ColumnWidth(20)
    private String transactionNo;

    @ExcelProperty("商品ID")
    @ColumnWidth(12)
    private Long productId;

    @ExcelProperty("商品名称")
    @ColumnWidth(20)
    private String productName;

    @ExcelProperty("商品编码")
    @ColumnWidth(15)
    private String sku;

    @ExcelProperty("交易类型")
    @ColumnWidth(12)
    private Integer transactionType;

    @ExcelProperty("变动数量")
    @ColumnWidth(12)
    private Integer quantity;

    @ExcelProperty("变动前库存")
    @ColumnWidth(15)
    private Integer beforeStock;

    @ExcelProperty("变动后库存")
    @ColumnWidth(15)
    private Integer afterStock;

    @ExcelProperty("关联单据ID")
    @ColumnWidth(15)
    private Long relatedId;

    @ExcelProperty("关联单据号")
    @ColumnWidth(20)
    private String relatedNo;

    @ExcelProperty("经手人ID")
    @ColumnWidth(15)
    private Long operatorId;

    @ExcelProperty("经手人")
    @ColumnWidth(18)
    private String operatorName;

    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
