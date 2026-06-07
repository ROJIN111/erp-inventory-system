-- Demo data for local Docker Compose deployment.
-- This script is idempotent for records prefixed with DEMO-.

SET NAMES utf8mb4;

SET @admin_id = (SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 LIMIT 1);
SET @purchase_id = COALESCE((SELECT id FROM sys_user WHERE username = 'purchase' AND deleted = 0 LIMIT 1), @admin_id);
SET @sales_id = COALESCE((SELECT id FROM sys_user WHERE username = 'sales' AND deleted = 0 LIMIT 1), @admin_id);
SET @warehouse_id = COALESCE((SELECT id FROM sys_user WHERE username = 'warehouse' AND deleted = 0 LIMIT 1), @admin_id);
SET @manager_id = COALESCE((SELECT id FROM sys_user WHERE username = 'manager' AND deleted = 0 LIMIT 1), @admin_id);

DELETE FROM operation_log WHERE operation LIKE 'DEMO-%' OR params LIKE '%DEMO-%';
DELETE FROM product_warning_message WHERE sku LIKE 'DEMO-%';
DELETE FROM inventory_transaction WHERE sku LIKE 'DEMO-%' OR related_no LIKE 'DEMO-%' OR transaction_no LIKE 'DEMO-%';
DELETE inventory_check_item
FROM inventory_check_item
JOIN inventory_check ON inventory_check_item.check_id = inventory_check.id
WHERE inventory_check.check_no LIKE 'DEMO-%';
DELETE FROM inventory_check WHERE check_no LIKE 'DEMO-%';
DELETE FROM inbound_order WHERE order_no LIKE 'DEMO-%';
DELETE FROM outbound_order WHERE order_no LIKE 'DEMO-%';
DELETE FROM product WHERE sku LIKE 'DEMO-%';

INSERT INTO product
    (name, description, sku, price, category, unit, status, stock, min_stock, max_stock, create_time, update_time)
VALUES
    ('ThinkPad E14 办公笔记本', '行政和销售部门常用办公设备，适合演示电子产品库存。', 'DEMO-NB-001', 5299.00, '电子产品', '台', 1, 42, 10, 120, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW()),
    ('罗技 K380 蓝牙键盘', '办公外设常备库存。', 'DEMO-KB-002', 199.00, '电子产品', '个', 1, 8, 20, 200, DATE_SUB(NOW(), INTERVAL 17 DAY), NOW()),
    ('A4 复印纸 80g', '日常办公消耗品，库存周转较快。', 'DEMO-PP-003', 26.50, '办公用品', '箱', 1, 260, 60, 500, DATE_SUB(NOW(), INTERVAL 16 DAY), NOW()),
    ('人体工学办公椅', '办公家具类商品，用于展示高库存预警。', 'DEMO-CH-004', 699.00, '办公家具', '把', 1, 36, 5, 30, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
    ('瓶装矿泉水 24瓶', '会议和仓库日常消耗品。', 'DEMO-WA-005', 32.00, '饮品', '箱', 1, 8, 30, 300, DATE_SUB(NOW(), INTERVAL 14 DAY), NOW()),
    ('标签打印纸', '出入库标签打印耗材。', 'DEMO-LB-006', 18.00, '耗材', '卷', 1, 95, 20, 180, DATE_SUB(NOW(), INTERVAL 13 DAY), NOW());

SET @p_nb = (SELECT id FROM product WHERE sku = 'DEMO-NB-001');
SET @p_kb = (SELECT id FROM product WHERE sku = 'DEMO-KB-002');
SET @p_pp = (SELECT id FROM product WHERE sku = 'DEMO-PP-003');
SET @p_ch = (SELECT id FROM product WHERE sku = 'DEMO-CH-004');
SET @p_wa = (SELECT id FROM product WHERE sku = 'DEMO-WA-005');
SET @p_lb = (SELECT id FROM product WHERE sku = 'DEMO-LB-006');

INSERT INTO inbound_order
    (order_no, product_id, quantity, price, total_amount, supplier, status, create_by, create_by_name, audit_by, audit_by_name, audit_time, remark, create_time, update_time)
VALUES
    ('DEMO-IN-20260518-001', @p_nb, 20, 5150.00, 103000.00, '联想企业采购中心', 1, @purchase_id, '入库员', @manager_id, '仓库主管', DATE_SUB(NOW(), INTERVAL 12 DAY), '首批办公电脑采购入库', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
    ('DEMO-IN-20260520-002', @p_pp, 180, 24.80, 4464.00, '晨光办公用品', 1, @purchase_id, '入库员', @manager_id, '仓库主管', DATE_SUB(NOW(), INTERVAL 10 DAY), '办公用纸补货', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
    ('DEMO-IN-20260522-003', @p_ch, 40, 650.00, 26000.00, '华东办公家具', 1, @purchase_id, '入库员', @manager_id, '仓库主管', DATE_SUB(NOW(), INTERVAL 8 DAY), '新办公区座椅到货', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
    ('DEMO-IN-20260528-004', @p_lb, 60, 15.00, 900.00, '仓储耗材供应商', 0, @purchase_id, '入库员', NULL, NULL, NULL, '待主管审核的标签纸补货单', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO outbound_order
    (order_no, product_id, quantity, price, total_amount, customer, status, create_by, create_by_name, audit_by, audit_by_name, audit_time, remark, create_time, update_time)
VALUES
    ('DEMO-OUT-20260524-001', @p_nb, 8, 5299.00, 42392.00, '上海分公司', 1, @sales_id, '出库员', @manager_id, '仓库主管', DATE_SUB(NOW(), INTERVAL 7 DAY), '上海分公司办公电脑领用', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
    ('DEMO-OUT-20260526-002', @p_wa, 48, 32.00, 1536.00, '行政部会议保障', 1, @sales_id, '出库员', @manager_id, '仓库主管', DATE_SUB(NOW(), INTERVAL 5 DAY), '大型会议物资出库', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
    ('DEMO-OUT-20260529-003', @p_kb, 12, 199.00, 2388.00, '销售一部', 0, @sales_id, '出库员', NULL, NULL, NULL, '待审核的键盘领用申请', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO inventory_check
    (check_no, check_date, check_mode, scope_name, blind_check, status, create_by_name, audit_by, audit_by_name, remark, create_by, create_time, update_time, review_time, snapshot_time)
VALUES
    ('DEMO-CHECK-20260530-001', DATE_SUB(NOW(), INTERVAL 2 DAY), 1, '电子产品与办公用品抽盘', 0, 2, '仓库管理员', @manager_id, '仓库主管', '月末抽盘，发现矿泉水账实差异', @warehouse_id, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

SET @check_id = (SELECT id FROM inventory_check WHERE check_no = 'DEMO-CHECK-20260530-001');

INSERT INTO inventory_check_item
    (check_id, product_id, product_name, sku, system_stock, actual_stock, difference, reason_type, remark, create_time)
VALUES
    (@check_id, @p_wa, '瓶装矿泉水 24瓶', 'DEMO-WA-005', 6, 8, 2, 2, '会议领用退回后未及时入账，盘盈调整', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (@check_id, @p_pp, 'A4 复印纸 80g', 'DEMO-PP-003', 260, 260, 0, NULL, '账实一致', DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO inventory_transaction
    (transaction_no, product_id, product_name, sku, transaction_type, quantity, before_stock, after_stock, related_id, related_no, operator_id, operator_name, remark, create_time)
VALUES
    ('DEMO-TX-20260518-001', @p_nb, 'ThinkPad E14 办公笔记本', 'DEMO-NB-001', 1, 20, 22, 42, (SELECT id FROM inbound_order WHERE order_no = 'DEMO-IN-20260518-001'), 'DEMO-IN-20260518-001', @manager_id, '仓库主管', '入库审核通过，补充办公电脑库存', DATE_SUB(NOW(), INTERVAL 12 DAY)),
    ('DEMO-TX-20260520-002', @p_pp, 'A4 复印纸 80g', 'DEMO-PP-003', 1, 180, 80, 260, (SELECT id FROM inbound_order WHERE order_no = 'DEMO-IN-20260520-002'), 'DEMO-IN-20260520-002', @manager_id, '仓库主管', '办公用纸补货入库', DATE_SUB(NOW(), INTERVAL 10 DAY)),
    ('DEMO-TX-20260522-003', @p_ch, '人体工学办公椅', 'DEMO-CH-004', 1, 40, 0, 40, (SELECT id FROM inbound_order WHERE order_no = 'DEMO-IN-20260522-003'), 'DEMO-IN-20260522-003', @manager_id, '仓库主管', '办公椅集中采购入库', DATE_SUB(NOW(), INTERVAL 8 DAY)),
    ('DEMO-TX-20260524-004', @p_nb, 'ThinkPad E14 办公笔记本', 'DEMO-NB-001', 2, 8, 42, 34, (SELECT id FROM outbound_order WHERE order_no = 'DEMO-OUT-20260524-001'), 'DEMO-OUT-20260524-001', @manager_id, '仓库主管', '上海分公司领用出库', DATE_SUB(NOW(), INTERVAL 7 DAY)),
    ('DEMO-TX-20260526-005', @p_wa, '瓶装矿泉水 24瓶', 'DEMO-WA-005', 2, 48, 54, 6, (SELECT id FROM outbound_order WHERE order_no = 'DEMO-OUT-20260526-002'), 'DEMO-OUT-20260526-002', @manager_id, '仓库主管', '会议保障物资出库', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    ('DEMO-TX-20260530-006', @p_wa, '瓶装矿泉水 24瓶', 'DEMO-WA-005', 3, 2, 6, 8, @check_id, 'DEMO-CHECK-20260530-001', @manager_id, '仓库主管', '盘点盘盈调整', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO product_warning_message
    (product_id, product_name, sku, current_stock, min_stock, max_stock, warning_type, message, status, handle_source, handle_type, handle_by, handle_by_name, owner_name, handle_note, create_time, follow_up_time, handle_time)
VALUES
    (@p_kb, '罗技 K380 蓝牙键盘', 'DEMO-KB-002', 8, 20, 200, 1, '当前库存 8，低于安全下限 20，建议尽快补货。', 0, NULL, NULL, NULL, NULL, '入库员', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL),
    (@p_wa, '瓶装矿泉水 24瓶', 'DEMO-WA-005', 8, 30, 300, 1, '当前库存 8，低于安全下限 30，会议季需重点关注。', 0, NULL, NULL, NULL, NULL, '仓库管理员', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), NULL),
    (@p_ch, '人体工学办公椅', 'DEMO-CH-004', 36, 5, 30, 2, '当前库存 36，高于安全上限 30，建议控制采购或调拨。', 1, 1, 3, @manager_id, '仓库主管', '仓库主管', '已通知行政部优先消化库存，暂停新增采购。', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));

INSERT INTO operation_log
    (user_id, username, operation, method, params, ip, create_time)
VALUES
    (@admin_id, 'admin', 'DEMO-登录系统', 'UserController.login', '{"username":"admin"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 12 DAY)),
    (@purchase_id, 'purchase', 'DEMO-创建入库单', 'InboundOrderController.create', '{"orderNo":"DEMO-IN-20260518-001"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 12 DAY)),
    (@manager_id, 'manager', 'DEMO-审核入库单', 'InboundOrderController.audit', '{"orderNo":"DEMO-IN-20260518-001"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 12 DAY)),
    (@sales_id, 'sales', 'DEMO-创建出库单', 'OutboundOrderController.create', '{"orderNo":"DEMO-OUT-20260524-001"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (@manager_id, 'manager', 'DEMO-审核出库单', 'OutboundOrderController.audit', '{"orderNo":"DEMO-OUT-20260524-001"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (@warehouse_id, 'warehouse', 'DEMO-提交库存盘点', 'InventoryCheckController.submit', '{"checkNo":"DEMO-CHECK-20260530-001"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (@manager_id, 'manager', 'DEMO-处理库存预警', 'ProductWarningMessageController.handle', '{"sku":"DEMO-CH-004"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 4 DAY));

SELECT 'demo data ready' AS result;
SELECT COUNT(*) AS demo_products FROM product WHERE sku LIKE 'DEMO-%';
SELECT COUNT(*) AS demo_inbound_orders FROM inbound_order WHERE order_no LIKE 'DEMO-%';
SELECT COUNT(*) AS demo_outbound_orders FROM outbound_order WHERE order_no LIKE 'DEMO-%';
SELECT COUNT(*) AS demo_transactions FROM inventory_transaction WHERE transaction_no LIKE 'DEMO-%';
SELECT COUNT(*) AS demo_warnings FROM product_warning_message WHERE sku LIKE 'DEMO-%';
