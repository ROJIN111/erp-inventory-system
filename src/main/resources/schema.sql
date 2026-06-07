-- Database schema initialization script for ERP Inventory System.
-- The statements are designed to be idempotent for repeatable local setup.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    avatar VARCHAR(255) COMMENT '头像',
    role_id BIGINT COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除',
    UNIQUE KEY uk_username_deleted (username, deleted),
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sys_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    module_name VARCHAR(50) COMMENT '模块名称',
    action_type VARCHAR(50) COMMENT '动作类型',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sys_permission_module (module_name),
    INDEX idx_sys_permission_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_sys_role_permission_role (role_id),
    INDEX idx_sys_role_permission_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description VARCHAR(500) COMMENT '商品描述',
    sku VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编码',
    price DECIMAL(10,2) COMMENT '价格',
    category VARCHAR(50) COMMENT '分类',
    unit VARCHAR(20) COMMENT '单位',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-下架',
    stock INT DEFAULT 0 COMMENT '库存数量',
    min_stock INT DEFAULT 10 COMMENT '最低库存预警值',
    max_stock INT DEFAULT 1000 COMMENT '最高库存预警值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sku (sku),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS inbound_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '入库单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '入库单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '数量',
    price DECIMAL(10,2) COMMENT '单价',
    total_amount DECIMAL(10,2) COMMENT '总金额',
    supplier VARCHAR(100) COMMENT '供应商',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已完成',
    create_by BIGINT COMMENT '制单人ID',
    create_by_name VARCHAR(50) COMMENT '制单人',
    audit_by BIGINT COMMENT '审核人ID',
    audit_by_name VARCHAR(50) COMMENT '审核人',
    audit_time DATETIME COMMENT '审核时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_inbound_order_no (order_no),
    INDEX idx_inbound_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

CREATE TABLE IF NOT EXISTS outbound_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '出库单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '出库单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '数量',
    price DECIMAL(10,2) COMMENT '单价',
    total_amount DECIMAL(10,2) COMMENT '总金额',
    customer VARCHAR(100) COMMENT '客户',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已完成',
    create_by BIGINT COMMENT '制单人ID',
    create_by_name VARCHAR(50) COMMENT '制单人',
    audit_by BIGINT COMMENT '审核人ID',
    audit_by_name VARCHAR(50) COMMENT '审核人',
    audit_time DATETIME COMMENT '审核时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_outbound_order_no (order_no),
    INDEX idx_outbound_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operation_log_user_id (user_id),
    INDEX idx_operation_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS inventory_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '盘点单ID',
    check_no VARCHAR(50) NOT NULL UNIQUE COMMENT '盘点单号',
    check_date DATETIME NOT NULL COMMENT '盘点日期',
    check_mode TINYINT DEFAULT 1 COMMENT '盘点方式：1-静态盘点 2-动态盘点 3-循环盘点 4-抽盘',
    scope_name VARCHAR(100) COMMENT '盘点范围',
    blind_check TINYINT DEFAULT 0 COMMENT '是否盲盘：0-否 1-是',
    status TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-待审核 2-已审核 3-已作废',
    create_by_name VARCHAR(50) COMMENT '制单人',
    audit_by BIGINT COMMENT '审核人ID',
    audit_by_name VARCHAR(50) COMMENT '审核人',
    remark VARCHAR(500) COMMENT '备注',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    review_time DATETIME COMMENT '审核时间',
    snapshot_time DATETIME COMMENT '库存快照时间',
    INDEX idx_inventory_check_no (check_no),
    INDEX idx_inventory_check_date (check_date),
    INDEX idx_inventory_check_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单表';

CREATE TABLE IF NOT EXISTS inventory_check_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    check_id BIGINT NOT NULL COMMENT '盘点单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) COMMENT '商品名称',
    sku VARCHAR(50) COMMENT '商品编码',
    system_stock INT DEFAULT 0 COMMENT '账面库存',
    actual_stock INT DEFAULT 0 COMMENT '实盘库存',
    difference INT DEFAULT 0 COMMENT '差异数量',
    reason_type TINYINT COMMENT '差异原因类型：1-流程执行问题 2-记录与操作问题 3-客观损耗 4-其他',
    remark VARCHAR(200) COMMENT '差异说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_inventory_check_item_check_id (check_id),
    INDEX idx_inventory_check_item_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表';

CREATE TABLE IF NOT EXISTS inventory_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '流水ID',
    transaction_no VARCHAR(50) NOT NULL UNIQUE COMMENT '流水号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) COMMENT '商品名称',
    sku VARCHAR(50) COMMENT '商品编码',
    transaction_type TINYINT NOT NULL COMMENT '交易类型：1-入库 2-出库 3-盘盈调整 4-盘亏调整',
    quantity INT NOT NULL COMMENT '变动数量',
    before_stock INT COMMENT '变动前库存',
    after_stock INT COMMENT '变动后库存',
    related_id BIGINT COMMENT '关联单据ID',
    related_no VARCHAR(50) COMMENT '关联单据号',
    operator_id BIGINT COMMENT '经手人ID',
    operator_name VARCHAR(50) COMMENT '经手人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_inventory_transaction_product_id (product_id),
    INDEX idx_inventory_transaction_type (transaction_type),
    INDEX idx_inventory_transaction_create_time (create_time),
    INDEX idx_inventory_transaction_related_no (related_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

CREATE TABLE IF NOT EXISTS product_warning_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预警消息ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) COMMENT '商品名称',
    sku VARCHAR(50) COMMENT '商品编码',
    current_stock INT COMMENT '当前库存',
    min_stock INT COMMENT '最低库存',
    max_stock INT COMMENT '最高库存',
    warning_type TINYINT COMMENT '预警类型：1-库存过低 2-库存过高',
    message VARCHAR(500) COMMENT '预警消息',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未处理 1-已处理',
    handle_source TINYINT COMMENT '处理来源：1-人工处理 2-系统自动恢复 3-系统自动关闭',
    handle_type TINYINT COMMENT '处理方式：1-补货申请 2-调拨处理 3-促销去化 4-盘点复核 5-人工确认 6-其他',
    handle_by BIGINT COMMENT '处理人ID',
    handle_by_name VARCHAR(50) COMMENT '处理人名称',
    owner_name VARCHAR(50) COMMENT '责任人',
    handle_note VARCHAR(500) COMMENT '处理说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    follow_up_time DATETIME COMMENT '跟进时间',
    handle_time DATETIME COMMENT '处理时间',
    INDEX idx_warning_product_id (product_id),
    INDEX idx_warning_type (warning_type),
    INDEX idx_warning_status (status),
    INDEX idx_warning_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品预警消息表';

-- 兼容旧版拆分 SQL：如果数据库是从早期脚本升级而来，需要补齐新增字段。
SET @product_stock_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product'
      AND COLUMN_NAME = 'stock'
);
SET @product_stock_sql = IF(
    @product_stock_exists = 0,
    'ALTER TABLE product ADD COLUMN stock INT DEFAULT 0 COMMENT ''库存数量'' AFTER status',
    'SELECT 1'
);
PREPARE product_stock_stmt FROM @product_stock_sql;
EXECUTE product_stock_stmt;
DEALLOCATE PREPARE product_stock_stmt;

SET @product_min_stock_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product'
      AND COLUMN_NAME = 'min_stock'
);
SET @product_min_stock_sql = IF(
    @product_min_stock_exists = 0,
    'ALTER TABLE product ADD COLUMN min_stock INT DEFAULT 10 COMMENT ''最低库存预警值'' AFTER stock',
    'SELECT 1'
);
PREPARE product_min_stock_stmt FROM @product_min_stock_sql;
EXECUTE product_min_stock_stmt;
DEALLOCATE PREPARE product_min_stock_stmt;

SET @product_max_stock_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product'
      AND COLUMN_NAME = 'max_stock'
);
SET @product_max_stock_sql = IF(
    @product_max_stock_exists = 0,
    'ALTER TABLE product ADD COLUMN max_stock INT DEFAULT 1000 COMMENT ''最高库存预警值'' AFTER min_stock',
    'SELECT 1'
);
PREPARE product_max_stock_stmt FROM @product_max_stock_sql;
EXECUTE product_max_stock_stmt;
DEALLOCATE PREPARE product_max_stock_stmt;

SET @inventory_check_mode_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check'
      AND COLUMN_NAME = 'check_mode'
);
SET @inventory_check_mode_sql = IF(
    @inventory_check_mode_exists = 0,
    'ALTER TABLE inventory_check ADD COLUMN check_mode TINYINT DEFAULT 1 COMMENT ''盘点方式：1-静态盘点 2-动态盘点 3-循环盘点 4-抽盘'' AFTER check_date',
    'SELECT 1'
);
PREPARE inventory_check_mode_stmt FROM @inventory_check_mode_sql;
EXECUTE inventory_check_mode_stmt;
DEALLOCATE PREPARE inventory_check_mode_stmt;

SET @inventory_check_scope_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check'
      AND COLUMN_NAME = 'scope_name'
);
SET @inventory_check_scope_sql = IF(
    @inventory_check_scope_exists = 0,
    'ALTER TABLE inventory_check ADD COLUMN scope_name VARCHAR(100) COMMENT ''盘点范围'' AFTER check_mode',
    'SELECT 1'
);
PREPARE inventory_check_scope_stmt FROM @inventory_check_scope_sql;
EXECUTE inventory_check_scope_stmt;
DEALLOCATE PREPARE inventory_check_scope_stmt;

SET @inventory_check_blind_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check'
      AND COLUMN_NAME = 'blind_check'
);
SET @inventory_check_blind_sql = IF(
    @inventory_check_blind_exists = 0,
    'ALTER TABLE inventory_check ADD COLUMN blind_check TINYINT DEFAULT 0 COMMENT ''是否盲盘：0-否 1-是'' AFTER scope_name',
    'SELECT 1'
);
PREPARE inventory_check_blind_stmt FROM @inventory_check_blind_sql;
EXECUTE inventory_check_blind_stmt;
DEALLOCATE PREPARE inventory_check_blind_stmt;

SET @inventory_check_review_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check'
      AND COLUMN_NAME = 'review_time'
);
SET @inventory_check_review_sql = IF(
    @inventory_check_review_exists = 0,
    'ALTER TABLE inventory_check ADD COLUMN review_time DATETIME COMMENT ''审核时间'' AFTER update_time',
    'SELECT 1'
);
PREPARE inventory_check_review_stmt FROM @inventory_check_review_sql;
EXECUTE inventory_check_review_stmt;
DEALLOCATE PREPARE inventory_check_review_stmt;

SET @inventory_check_snapshot_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check'
      AND COLUMN_NAME = 'snapshot_time'
);
SET @inventory_check_snapshot_sql = IF(
    @inventory_check_snapshot_exists = 0,
    'ALTER TABLE inventory_check ADD COLUMN snapshot_time DATETIME COMMENT ''库存快照时间'' AFTER review_time',
    'SELECT 1'
);
PREPARE inventory_check_snapshot_stmt FROM @inventory_check_snapshot_sql;
EXECUTE inventory_check_snapshot_stmt;
DEALLOCATE PREPARE inventory_check_snapshot_stmt;

SET @inventory_check_create_by_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check'
      AND COLUMN_NAME = 'create_by_name'
);
SET @inventory_check_create_by_name_sql = IF(
    @inventory_check_create_by_name_exists = 0,
    'ALTER TABLE inventory_check ADD COLUMN create_by_name VARCHAR(50) COMMENT ''制单人'' AFTER status',
    'SELECT 1'
);
PREPARE inventory_check_create_by_name_stmt FROM @inventory_check_create_by_name_sql;
EXECUTE inventory_check_create_by_name_stmt;
DEALLOCATE PREPARE inventory_check_create_by_name_stmt;

SET @inventory_check_audit_by_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check'
      AND COLUMN_NAME = 'audit_by'
);
SET @inventory_check_audit_by_sql = IF(
    @inventory_check_audit_by_exists = 0,
    'ALTER TABLE inventory_check ADD COLUMN audit_by BIGINT COMMENT ''审核人ID'' AFTER create_by_name',
    'SELECT 1'
);
PREPARE inventory_check_audit_by_stmt FROM @inventory_check_audit_by_sql;
EXECUTE inventory_check_audit_by_stmt;
DEALLOCATE PREPARE inventory_check_audit_by_stmt;

SET @inventory_check_audit_by_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check'
      AND COLUMN_NAME = 'audit_by_name'
);
SET @inventory_check_audit_by_name_sql = IF(
    @inventory_check_audit_by_name_exists = 0,
    'ALTER TABLE inventory_check ADD COLUMN audit_by_name VARCHAR(50) COMMENT ''审核人'' AFTER audit_by',
    'SELECT 1'
);
PREPARE inventory_check_audit_by_name_stmt FROM @inventory_check_audit_by_name_sql;
EXECUTE inventory_check_audit_by_name_stmt;
DEALLOCATE PREPARE inventory_check_audit_by_name_stmt;

SET @inventory_check_item_reason_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_check_item'
      AND COLUMN_NAME = 'reason_type'
);
SET @inventory_check_item_reason_sql = IF(
    @inventory_check_item_reason_exists = 0,
    'ALTER TABLE inventory_check_item ADD COLUMN reason_type TINYINT COMMENT ''差异原因类型：1-流程执行问题 2-记录与操作问题 3-客观损耗 4-其他'' AFTER difference',
    'SELECT 1'
);
PREPARE inventory_check_item_reason_stmt FROM @inventory_check_item_reason_sql;
EXECUTE inventory_check_item_reason_stmt;
DEALLOCATE PREPARE inventory_check_item_reason_stmt;

SET @inbound_create_by_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inbound_order'
      AND COLUMN_NAME = 'create_by'
);
SET @inbound_create_by_sql = IF(
    @inbound_create_by_exists = 0,
    'ALTER TABLE inbound_order ADD COLUMN create_by BIGINT COMMENT ''制单人ID'' AFTER status',
    'SELECT 1'
);
PREPARE inbound_create_by_stmt FROM @inbound_create_by_sql;
EXECUTE inbound_create_by_stmt;
DEALLOCATE PREPARE inbound_create_by_stmt;

SET @inbound_create_by_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inbound_order'
      AND COLUMN_NAME = 'create_by_name'
);
SET @inbound_create_by_name_sql = IF(
    @inbound_create_by_name_exists = 0,
    'ALTER TABLE inbound_order ADD COLUMN create_by_name VARCHAR(50) COMMENT ''制单人'' AFTER create_by',
    'SELECT 1'
);
PREPARE inbound_create_by_name_stmt FROM @inbound_create_by_name_sql;
EXECUTE inbound_create_by_name_stmt;
DEALLOCATE PREPARE inbound_create_by_name_stmt;

SET @inbound_audit_by_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inbound_order'
      AND COLUMN_NAME = 'audit_by'
);
SET @inbound_audit_by_sql = IF(
    @inbound_audit_by_exists = 0,
    'ALTER TABLE inbound_order ADD COLUMN audit_by BIGINT COMMENT ''审核人ID'' AFTER create_by_name',
    'SELECT 1'
);
PREPARE inbound_audit_by_stmt FROM @inbound_audit_by_sql;
EXECUTE inbound_audit_by_stmt;
DEALLOCATE PREPARE inbound_audit_by_stmt;

SET @inbound_audit_by_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inbound_order'
      AND COLUMN_NAME = 'audit_by_name'
);
SET @inbound_audit_by_name_sql = IF(
    @inbound_audit_by_name_exists = 0,
    'ALTER TABLE inbound_order ADD COLUMN audit_by_name VARCHAR(50) COMMENT ''审核人'' AFTER audit_by',
    'SELECT 1'
);
PREPARE inbound_audit_by_name_stmt FROM @inbound_audit_by_name_sql;
EXECUTE inbound_audit_by_name_stmt;
DEALLOCATE PREPARE inbound_audit_by_name_stmt;

SET @inbound_audit_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inbound_order'
      AND COLUMN_NAME = 'audit_time'
);
SET @inbound_audit_time_sql = IF(
    @inbound_audit_time_exists = 0,
    'ALTER TABLE inbound_order ADD COLUMN audit_time DATETIME COMMENT ''审核时间'' AFTER audit_by_name',
    'SELECT 1'
);
PREPARE inbound_audit_time_stmt FROM @inbound_audit_time_sql;
EXECUTE inbound_audit_time_stmt;
DEALLOCATE PREPARE inbound_audit_time_stmt;

SET @outbound_create_by_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'outbound_order'
      AND COLUMN_NAME = 'create_by'
);
SET @outbound_create_by_sql = IF(
    @outbound_create_by_exists = 0,
    'ALTER TABLE outbound_order ADD COLUMN create_by BIGINT COMMENT ''制单人ID'' AFTER status',
    'SELECT 1'
);
PREPARE outbound_create_by_stmt FROM @outbound_create_by_sql;
EXECUTE outbound_create_by_stmt;
DEALLOCATE PREPARE outbound_create_by_stmt;

SET @outbound_create_by_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'outbound_order'
      AND COLUMN_NAME = 'create_by_name'
);
SET @outbound_create_by_name_sql = IF(
    @outbound_create_by_name_exists = 0,
    'ALTER TABLE outbound_order ADD COLUMN create_by_name VARCHAR(50) COMMENT ''制单人'' AFTER create_by',
    'SELECT 1'
);
PREPARE outbound_create_by_name_stmt FROM @outbound_create_by_name_sql;
EXECUTE outbound_create_by_name_stmt;
DEALLOCATE PREPARE outbound_create_by_name_stmt;

SET @outbound_audit_by_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'outbound_order'
      AND COLUMN_NAME = 'audit_by'
);
SET @outbound_audit_by_sql = IF(
    @outbound_audit_by_exists = 0,
    'ALTER TABLE outbound_order ADD COLUMN audit_by BIGINT COMMENT ''审核人ID'' AFTER create_by_name',
    'SELECT 1'
);
PREPARE outbound_audit_by_stmt FROM @outbound_audit_by_sql;
EXECUTE outbound_audit_by_stmt;
DEALLOCATE PREPARE outbound_audit_by_stmt;

SET @outbound_audit_by_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'outbound_order'
      AND COLUMN_NAME = 'audit_by_name'
);
SET @outbound_audit_by_name_sql = IF(
    @outbound_audit_by_name_exists = 0,
    'ALTER TABLE outbound_order ADD COLUMN audit_by_name VARCHAR(50) COMMENT ''审核人'' AFTER audit_by',
    'SELECT 1'
);
PREPARE outbound_audit_by_name_stmt FROM @outbound_audit_by_name_sql;
EXECUTE outbound_audit_by_name_stmt;
DEALLOCATE PREPARE outbound_audit_by_name_stmt;

SET @outbound_audit_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'outbound_order'
      AND COLUMN_NAME = 'audit_time'
);
SET @outbound_audit_time_sql = IF(
    @outbound_audit_time_exists = 0,
    'ALTER TABLE outbound_order ADD COLUMN audit_time DATETIME COMMENT ''审核时间'' AFTER audit_by_name',
    'SELECT 1'
);
PREPARE outbound_audit_time_stmt FROM @outbound_audit_time_sql;
EXECUTE outbound_audit_time_stmt;
DEALLOCATE PREPARE outbound_audit_time_stmt;

SET @inventory_transaction_operator_id_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_transaction'
      AND COLUMN_NAME = 'operator_id'
);
SET @inventory_transaction_operator_id_sql = IF(
    @inventory_transaction_operator_id_exists = 0,
    'ALTER TABLE inventory_transaction ADD COLUMN operator_id BIGINT COMMENT ''经手人ID'' AFTER related_no',
    'SELECT 1'
);
PREPARE inventory_transaction_operator_id_stmt FROM @inventory_transaction_operator_id_sql;
EXECUTE inventory_transaction_operator_id_stmt;
DEALLOCATE PREPARE inventory_transaction_operator_id_stmt;

SET @inventory_transaction_operator_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inventory_transaction'
      AND COLUMN_NAME = 'operator_name'
);
SET @inventory_transaction_operator_name_sql = IF(
    @inventory_transaction_operator_name_exists = 0,
    'ALTER TABLE inventory_transaction ADD COLUMN operator_name VARCHAR(50) COMMENT ''经手人'' AFTER operator_id',
    'SELECT 1'
);
PREPARE inventory_transaction_operator_name_stmt FROM @inventory_transaction_operator_name_sql;
EXECUTE inventory_transaction_operator_name_stmt;
DEALLOCATE PREPARE inventory_transaction_operator_name_stmt;

UPDATE inventory_transaction
SET operator_id = COALESCE(operator_id, 0),
    operator_name = '历史未记录'
WHERE operator_name IS NULL OR TRIM(operator_name) = '';

SET @warning_handle_source_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_warning_message'
      AND COLUMN_NAME = 'handle_source'
);
SET @warning_handle_source_sql = IF(
    @warning_handle_source_exists = 0,
    'ALTER TABLE product_warning_message ADD COLUMN handle_source TINYINT COMMENT ''处理来源：1-人工处理 2-系统自动恢复 3-系统自动关闭'' AFTER status',
    'SELECT 1'
);
PREPARE warning_handle_source_stmt FROM @warning_handle_source_sql;
EXECUTE warning_handle_source_stmt;
DEALLOCATE PREPARE warning_handle_source_stmt;

SET @warning_handle_type_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_warning_message'
      AND COLUMN_NAME = 'handle_type'
);
SET @warning_handle_type_sql = IF(
    @warning_handle_type_exists = 0,
    'ALTER TABLE product_warning_message ADD COLUMN handle_type TINYINT COMMENT ''处理方式：1-补货申请 2-调拨处理 3-促销去化 4-盘点复核 5-人工确认 6-其他'' AFTER handle_source',
    'SELECT 1'
);
PREPARE warning_handle_type_stmt FROM @warning_handle_type_sql;
EXECUTE warning_handle_type_stmt;
DEALLOCATE PREPARE warning_handle_type_stmt;

SET @warning_handle_by_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_warning_message'
      AND COLUMN_NAME = 'handle_by'
);
SET @warning_handle_by_sql = IF(
    @warning_handle_by_exists = 0,
    'ALTER TABLE product_warning_message ADD COLUMN handle_by BIGINT COMMENT ''处理人ID'' AFTER handle_type',
    'SELECT 1'
);
PREPARE warning_handle_by_stmt FROM @warning_handle_by_sql;
EXECUTE warning_handle_by_stmt;
DEALLOCATE PREPARE warning_handle_by_stmt;

SET @warning_handle_by_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_warning_message'
      AND COLUMN_NAME = 'handle_by_name'
);
SET @warning_handle_by_name_sql = IF(
    @warning_handle_by_name_exists = 0,
    'ALTER TABLE product_warning_message ADD COLUMN handle_by_name VARCHAR(50) COMMENT ''处理人名称'' AFTER handle_by',
    'SELECT 1'
);
PREPARE warning_handle_by_name_stmt FROM @warning_handle_by_name_sql;
EXECUTE warning_handle_by_name_stmt;
DEALLOCATE PREPARE warning_handle_by_name_stmt;

SET @warning_owner_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_warning_message'
      AND COLUMN_NAME = 'owner_name'
);
SET @warning_owner_name_sql = IF(
    @warning_owner_name_exists = 0,
    'ALTER TABLE product_warning_message ADD COLUMN owner_name VARCHAR(50) COMMENT ''责任人'' AFTER handle_by_name',
    'SELECT 1'
);
PREPARE warning_owner_name_stmt FROM @warning_owner_name_sql;
EXECUTE warning_owner_name_stmt;
DEALLOCATE PREPARE warning_owner_name_stmt;

SET @warning_handle_note_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_warning_message'
      AND COLUMN_NAME = 'handle_note'
);
SET @warning_handle_note_sql = IF(
    @warning_handle_note_exists = 0,
    'ALTER TABLE product_warning_message ADD COLUMN handle_note VARCHAR(500) COMMENT ''处理说明'' AFTER owner_name',
    'SELECT 1'
);
PREPARE warning_handle_note_stmt FROM @warning_handle_note_sql;
EXECUTE warning_handle_note_stmt;
DEALLOCATE PREPARE warning_handle_note_stmt;

SET @warning_follow_up_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_warning_message'
      AND COLUMN_NAME = 'follow_up_time'
);
SET @warning_follow_up_time_sql = IF(
    @warning_follow_up_time_exists = 0,
    'ALTER TABLE product_warning_message ADD COLUMN follow_up_time DATETIME COMMENT ''跟进时间'' AFTER create_time',
    'SELECT 1'
);
PREPARE warning_follow_up_time_stmt FROM @warning_follow_up_time_sql;
EXECUTE warning_follow_up_time_stmt;
DEALLOCATE PREPARE warning_follow_up_time_stmt;
