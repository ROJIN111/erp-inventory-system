package com.example.erpinventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erpinventory.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM product WHERE sku = #{sku}")
    Product selectBySku(String sku);

    @Select("SELECT * FROM product WHERE category = #{category} AND status = 1")
    List<Product> selectByCategory(String category);

    @Select("""
            SELECT COUNT(*) AS totalProducts,
                   COALESCE(SUM(CASE WHEN COALESCE(stock, 0) < #{lowStockThreshold} THEN 1 ELSE 0 END), 0) AS lowStockCount,
                   COALESCE(SUM(COALESCE(stock, 0)), 0) AS totalStock,
                   COALESCE(SUM(CASE
                                    WHEN price IS NOT NULL THEN price * COALESCE(stock, 0)
                                    ELSE 0
                                END), 0) AS totalValue
            FROM product
            """)
    Map<String, Object> selectProductAndInventorySummary(@Param("lowStockThreshold") int lowStockThreshold);

    @Select("""
            SELECT CASE
                       WHEN category IS NULL OR TRIM(category) = '' THEN '未分类'
                       ELSE category
                   END AS categoryName,
                   COUNT(*) AS categoryCount
            FROM product
            GROUP BY CASE
                         WHEN category IS NULL OR TRIM(category) = '' THEN '未分类'
                         ELSE category
                     END
            ORDER BY categoryCount DESC, categoryName ASC
            """)
    List<Map<String, Object>> selectCategoryStats();

    @Select("""
            SELECT name, COALESCE(stock, 0) AS stock
            FROM product
            WHERE COALESCE(stock, 0) < #{lowStockThreshold}
            ORDER BY COALESCE(stock, 0) ASC, id DESC
            """)
    List<Map<String, Object>> selectLowStockProducts(@Param("lowStockThreshold") int lowStockThreshold);

    @Update("""
            UPDATE product
            SET stock = #{targetStock}, update_time = #{updateTime}
            WHERE id = #{productId}
              AND stock = #{expectedStock}
            """)
    int updateStockIfMatches(@Param("productId") Long productId,
                             @Param("expectedStock") Integer expectedStock,
                             @Param("targetStock") Integer targetStock,
                             @Param("updateTime") LocalDateTime updateTime);
}
