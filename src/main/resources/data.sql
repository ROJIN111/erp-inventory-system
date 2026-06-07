-- Seed data initialization script for ERP Inventory System.

SET NAMES utf8mb4;

INSERT INTO sys_user (username, password, real_name, email, status, deleted)
SELECT 'admin', '$2a$10$rOz9hOaR0P.U3vH8Dy7SpeAX.EOvL9Pn4zItDskZ6rQqQZ3Bq4rIy', '系统管理员', 'admin@erp.com', 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'admin'
);

INSERT INTO sys_user (username, password, real_name, email, status, deleted)
SELECT 'warehouse', '$2a$10$V2uI7.q1N0PzV9g3H9HwUuZzYFm7Q4D9W9m2G3v9A3N7B1t7R5p4O', '仓库管理员', 'warehouse@erp.com', 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'warehouse'
);

INSERT INTO sys_user (username, password, real_name, email, status, deleted)
SELECT 'employee', '$2a$10$F9z9Q9m5B5N7F8r9T0y1UuE2V3W4X5Y6Z7a8b9c0d1e2f3g4h5i6j', '普通员工', 'employee@erp.com', 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'employee'
);

INSERT INTO product (name, description, sku, price, category, unit, status, stock, min_stock, max_stock)
SELECT '苹果笔记本电脑', 'MacBook Pro 13 寸，M1 芯片', 'MBP-2023-001', 12999.00, '电子产品', '台', 1, 20, 5, 40
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE sku = 'MBP-2023-001'
);

INSERT INTO product (name, description, sku, price, category, unit, status, stock, min_stock, max_stock)
SELECT '无线蓝牙耳机', 'AirPods Pro 第二代', 'AP-2023-001', 1899.00, '电子产品', '副', 1, 50, 20, 100
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE sku = 'AP-2023-001'
);

INSERT INTO product (name, description, sku, price, category, unit, status, stock, min_stock, max_stock)
SELECT '办公椅', '人体工学转椅，黑色', 'OC-2023-001', 899.00, '办公用品', '把', 1, 15, 5, 40
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE sku = 'OC-2023-001'
);

INSERT INTO product (name, description, sku, price, category, unit, status, stock, min_stock, max_stock)
SELECT 'A4 打印纸', '80g 复印纸，500 张/包', 'PP-2023-001', 25.50, '办公用品', '包', 1, 200, 50, 500
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE sku = 'PP-2023-001'
);

INSERT INTO product (name, description, sku, price, category, unit, status, stock, min_stock, max_stock)
SELECT '矿泉水', '500ml*24 瓶/箱', 'WD-2023-001', 24.00, '饮品', '箱', 1, 80, 24, 180
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE sku = 'WD-2023-001'
);
