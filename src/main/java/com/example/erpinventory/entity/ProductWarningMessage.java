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
@TableName("product_warning_message")
public class ProductWarningMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("warning_id")
    @ColumnWidth(15)
    private Long id;

    @ExcelProperty("product_id")
    @ColumnWidth(12)
    private Long productId;

    @ExcelProperty("product_name")
    @ColumnWidth(20)
    private String productName;

    @ExcelProperty("sku")
    @ColumnWidth(15)
    private String sku;

    @ExcelProperty("current_stock")
    @ColumnWidth(12)
    private Integer currentStock;

    @ExcelProperty("min_stock")
    @ColumnWidth(12)
    private Integer minStock;

    @ExcelProperty("max_stock")
    @ColumnWidth(12)
    private Integer maxStock;

    @ExcelProperty("warning_type")
    @ColumnWidth(12)
    private Integer warningType;

    @ExcelProperty("message")
    @ColumnWidth(40)
    private String message;

    @ExcelProperty("status")
    @ColumnWidth(10)
    private Integer status;

    @ExcelProperty("handle_source")
    @ColumnWidth(12)
    private Integer handleSource;

    @ExcelProperty("handle_type")
    @ColumnWidth(12)
    private Integer handleType;

    @ExcelProperty("handle_by")
    @ColumnWidth(12)
    private Long handleBy;

    @ExcelProperty("handle_by_name")
    @ColumnWidth(16)
    private String handleByName;

    @ExcelProperty("owner_name")
    @ColumnWidth(16)
    private String ownerName;

    @ExcelProperty("handle_note")
    @ColumnWidth(40)
    private String handleNote;

    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty("create_time")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ExcelProperty("follow_up_time")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followUpTime;

    @ExcelProperty("handle_time")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;
}
