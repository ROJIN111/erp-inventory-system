package com.example.erpinventory.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erpinventory.converter.ProductStatusConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("商品ID")
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty("商品名称")
    @ColumnWidth(20)
    private String name;

    @ExcelProperty("商品描述")
    @ColumnWidth(30)
    private String description;

    @ExcelProperty("商品编码")
    @ColumnWidth(15)
    private String sku;

    @ExcelProperty("价格")
    @ColumnWidth(12)
    private BigDecimal price;

    @ExcelProperty("分类")
    @ColumnWidth(12)
    private String category;

    @ExcelProperty("单位")
    @ColumnWidth(10)
    private String unit;

    @ExcelProperty(value = "状态", converter = ProductStatusConverter.class)
    @ColumnWidth(10)
    private Integer status;

    @ExcelProperty("库存数量")
    @ColumnWidth(12)
    private Integer stock;

    @TableField("min_stock")
    @ExcelProperty("最低库存")
    @ColumnWidth(12)
    private Integer minStock;

    @TableField("max_stock")
    @ExcelProperty("最高库存")
    @ColumnWidth(12)
    private Integer maxStock;

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
}
