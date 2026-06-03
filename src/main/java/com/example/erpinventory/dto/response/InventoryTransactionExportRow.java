package com.example.erpinventory.dto.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryTransactionExportRow {

    @ExcelProperty("流水号")
    @ColumnWidth(22)
    private String transactionNo;

    @ExcelProperty("商品名称")
    @ColumnWidth(22)
    private String productName;

    @ExcelProperty("SKU")
    @ColumnWidth(18)
    private String sku;

    @ExcelProperty("分类")
    @ColumnWidth(16)
    private String category;

    @ExcelProperty("交易类型")
    @ColumnWidth(14)
    private String transactionTypeText;

    @ExcelProperty("变动数量")
    @ColumnWidth(12)
    private Integer quantity;

    @ExcelProperty("变动前库存")
    @ColumnWidth(14)
    private Integer beforeStock;

    @ExcelProperty("变动后库存")
    @ColumnWidth(14)
    private Integer afterStock;

    @ExcelProperty("关联单据号")
    @ColumnWidth(20)
    private String relatedNo;

    @ExcelProperty("经手人")
    @ColumnWidth(16)
    private String operatorName;

    @ExcelProperty("备注")
    @ColumnWidth(38)
    private String remark;

    @ExcelProperty("发生时间")
    @ColumnWidth(22)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
