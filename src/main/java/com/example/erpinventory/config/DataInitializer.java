package com.example.erpinventory.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.erpinventory.entity.SysPermission;
import com.example.erpinventory.entity.SysRole;
import com.example.erpinventory.entity.SysRolePermission;
import com.example.erpinventory.entity.SysUser;
import com.example.erpinventory.mapper.SysPermissionMapper;
import com.example.erpinventory.mapper.SysRoleMapper;
import com.example.erpinventory.mapper.SysRolePermissionMapper;
import com.example.erpinventory.mapper.UserMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class DataInitializer {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Bean
    @ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
    public ApplicationRunner initDefaultUsers() {
        return args -> {
            initializeRolesAndPermissions();
            initializeUsers();
        };
    }

    private void initializeRolesAndPermissions() {
        upsertRole("ADMIN", "系统管理员", "拥有全部权限");
        upsertRole("PURCHASE", "入库员", "负责入库制单");
        upsertRole("SALES", "出库员", "负责出库制单");
        upsertRole("WAREHOUSE", "仓库管理员", "负责盘点和库存查看");
        upsertRole("WAREHOUSE_MANAGER", "仓库主管", "负责审核与库存监管");
        upsertRole("FINANCE", "财务只读", "负责只读查看核心数据");

        upsertPermission("dashboard:view", "首页查看", "dashboard", "view", "查看首页统计信息");
        upsertPermission("product:view", "商品查看", "product", "view", "查看商品");
        upsertPermission("product:create", "商品新增", "product", "create", "新增商品");
        upsertPermission("product:update", "商品编辑", "product", "update", "编辑商品");
        upsertPermission("product:delete", "商品删除", "product", "delete", "删除商品");
        upsertPermission("inbound:view", "入库查看", "inbound", "view", "查看入库单");
        upsertPermission("inbound:create", "入库制单", "inbound", "create", "新增入库单");
        upsertPermission("inbound:audit", "入库审核", "inbound", "audit", "审核入库单");
        upsertPermission("inbound:delete", "入库删除", "inbound", "delete", "删除入库单");
        upsertPermission("outbound:view", "出库查看", "outbound", "view", "查看出库单");
        upsertPermission("outbound:create", "出库制单", "outbound", "create", "新增出库单");
        upsertPermission("outbound:audit", "出库审核", "outbound", "audit", "审核出库单");
        upsertPermission("outbound:delete", "出库删除", "outbound", "delete", "删除出库单");
        upsertPermission("inventory_check:view", "盘点查看", "inventory_check", "view", "查看盘点单");
        upsertPermission("inventory_check:create", "盘点新增", "inventory_check", "create", "新增盘点单");
        upsertPermission("inventory_check:update", "盘点编辑", "inventory_check", "update", "编辑盘点单");
        upsertPermission("inventory_check:submit", "盘点提交", "inventory_check", "submit", "提交盘点单");
        upsertPermission("inventory_check:audit", "盘点审核", "inventory_check", "audit", "审核盘点单");
        upsertPermission("inventory_check:cancel", "盘点作废", "inventory_check", "cancel", "作废盘点单");
        upsertPermission("inventory_check:delete", "盘点删除", "inventory_check", "delete", "删除盘点单");
        upsertPermission("inventory_flow:view", "流水查看", "inventory_flow", "view", "查看库存流水");
        upsertPermission("inventory_flow:export", "流水导出", "inventory_flow", "export", "导出库存流水");
        upsertPermission("warning:view", "预警查看", "warning", "view", "查看预警消息");
        upsertPermission("warning:config", "预警配置", "warning", "config", "维护预警阈值");
        upsertPermission("warning:handle", "预警处理", "warning", "handle", "处理预警消息");
        upsertPermission("user:view", "用户查看", "user", "view", "查看用户");
        upsertPermission("user:create", "用户新增", "user", "create", "新增用户");
        upsertPermission("user:update", "用户更新", "user", "update", "更新用户");
        upsertPermission("user:delete", "用户删除", "user", "delete", "删除用户");
        upsertPermission("role:view", "角色查看", "role", "view", "查看角色");
        upsertPermission("log:view", "日志查看", "log", "view", "查看操作日志");
        upsertPermission("log:delete", "日志删除", "log", "delete", "删除操作日志");

        bindRolePermissions("ADMIN", Set.of(
                "dashboard:view", "product:view", "product:create", "product:update", "product:delete",
                "inbound:view", "inbound:create", "inbound:audit", "inbound:delete",
                "outbound:view", "outbound:create", "outbound:audit", "outbound:delete",
                "inventory_check:view", "inventory_check:create", "inventory_check:update", "inventory_check:submit",
                "inventory_check:audit", "inventory_check:cancel", "inventory_check:delete",
                "inventory_flow:view", "inventory_flow:export",
                "warning:view", "warning:config", "warning:handle",
                "user:view", "user:create", "user:update", "user:delete",
                "role:view", "log:view", "log:delete"
        ));
        bindRolePermissions("PURCHASE", Set.of(
                "dashboard:view", "product:view", "inbound:view", "inbound:create", "inventory_flow:view"
        ));
        bindRolePermissions("SALES", Set.of(
                "dashboard:view", "product:view", "outbound:view", "outbound:create", "inventory_flow:view"
        ));
        bindRolePermissions("WAREHOUSE", Set.of(
                "dashboard:view", "product:view", "inbound:view", "outbound:view",
                "inventory_check:view", "inventory_check:create", "inventory_check:update", "inventory_check:submit",
                "inventory_flow:view", "warning:view"
        ));
        bindRolePermissions("WAREHOUSE_MANAGER", Set.of(
                "dashboard:view", "product:view", "product:update", "inbound:view", "inbound:audit",
                "outbound:view", "outbound:audit",
                "inventory_check:view", "inventory_check:audit", "inventory_check:cancel",
                "inventory_flow:view", "inventory_flow:export",
                "warning:view", "warning:config", "warning:handle", "log:view"
        ));
        bindRolePermissions("FINANCE", Set.of(
                "dashboard:view", "product:view", "inbound:view", "outbound:view",
                "inventory_check:view", "inventory_flow:view", "warning:view"
        ));
    }

    private void initializeUsers() {
        upsertUser("admin", "admin123", "系统管理员", "admin@erp.com", "ADMIN", 1);
        upsertUser("purchase", "purchase123", "入库员", "purchase@erp.com", "PURCHASE", 1);
        upsertUser("sales", "sales123", "出库员", "sales@erp.com", "SALES", 1);
        upsertUser("warehouse", "warehouse123", "仓库管理员", "warehouse@erp.com", "WAREHOUSE", 1);
        upsertUser("manager", "manager123", "仓库主管", "manager@erp.com", "WAREHOUSE_MANAGER", 1);
        upsertUser("finance", "finance123", "财务只读", "finance@erp.com", "FINANCE", 1);
        upsertUser("employee", "employee123", "普通员工", "employee@erp.com", "SALES", 1);
        upsertUser("disabled", "disabled123", "停用账号", "disabled@erp.com", "WAREHOUSE", 0);
    }

    private void upsertUser(String username, String rawPassword, String realName, String email, String roleCode, Integer status) {
        SysUser existingUser = userMapper.selectByUsername(username);
        SysRole role = sysRoleMapper.selectByCode(roleCode);

        if (existingUser == null) {
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRealName(realName);
            user.setEmail(email);
            user.setRoleId(role == null ? null : role.getId());
            user.setStatus(status);
            user.setDeleted(0);
            userMapper.insert(user);
            return;
        }

        existingUser.setRealName(realName);
        existingUser.setEmail(email);
        existingUser.setRoleId(role == null ? null : role.getId());
        existingUser.setStatus(status);
        existingUser.setDeleted(0);
        if (shouldMigratePassword(username, rawPassword, existingUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(rawPassword));
        }
        userMapper.updateById(existingUser);
    }

    private void upsertRole(String roleCode, String roleName, String remark) {
        SysRole role = sysRoleMapper.selectByCode(roleCode);
        if (role == null) {
            role = new SysRole();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setRemark(remark);
            role.setStatus(1);
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            sysRoleMapper.insert(role);
            return;
        }

        role.setRoleName(roleName);
        role.setRemark(remark);
        role.setStatus(1);
        role.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(role);
    }

    private void upsertPermission(String code, String name, String moduleName, String actionType, String remark) {
        SysPermission permission = sysPermissionMapper.selectOne(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermissionCode, code)
                .last("LIMIT 1"));
        if (permission == null) {
            permission = new SysPermission();
            permission.setPermissionCode(code);
            permission.setPermissionName(name);
            permission.setModuleName(moduleName);
            permission.setActionType(actionType);
            permission.setRemark(remark);
            permission.setStatus(1);
            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());
            sysPermissionMapper.insert(permission);
            return;
        }

        permission.setPermissionName(name);
        permission.setModuleName(moduleName);
        permission.setActionType(actionType);
        permission.setRemark(remark);
        permission.setStatus(1);
        permission.setUpdateTime(LocalDateTime.now());
        sysPermissionMapper.updateById(permission);
    }

    private void bindRolePermissions(String roleCode, Set<String> permissionCodes) {
        SysRole role = sysRoleMapper.selectByCode(roleCode);
        if (role == null) {
            return;
        }

        Map<String, Long> permissionIdMap = new LinkedHashMap<>();
        sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getPermissionCode, permissionCodes))
                .forEach(permission -> permissionIdMap.put(permission.getPermissionCode(), permission.getId()));

        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, role.getId()));

        LocalDateTime now = LocalDateTime.now();
        permissionCodes.forEach(code -> {
            Long permissionId = permissionIdMap.get(code);
            if (permissionId == null) {
                return;
            }
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(role.getId());
            relation.setPermissionId(permissionId);
            relation.setCreateTime(now);
            sysRolePermissionMapper.insert(relation);
        });
    }

    private boolean shouldMigratePassword(String username, String rawPassword, String currentPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            return true;
        }
        if (passwordEncoder.matches(rawPassword, currentPassword)) {
            return false;
        }
        return legacyHashes(username).contains(currentPassword);
    }

    private List<String> legacyHashes(String username) {
        return switch (username) {
            case "admin" -> List.of(
                    "$2a$10$w9ziP5W./aF5.fZsW45eF.XJrZI6V3P4N8q7J4N9.4r4M2P7G9yQG",
                    "$2a$10$rOz9hOaR0P.U3vH8Dy7SpeAX.EOvL9Pn4zItDskZ6rQqQZ3Bq4rIy"
            );
            case "warehouse" -> List.of(
                    "$2a$10$V2uI7.q1N0PzV9g3H9HwUuZzYFm7Q4D9W9m2G3v9A3N7B1t7R5p4O"
            );
            case "employee" -> List.of(
                    "$2a$10$F9z9Q9m5B5N7F8r9T0y1UuE2V3W4X5Y6Z7a8b9c0d1e2f3g4h5i6j"
            );
            default -> List.of();
        };
    }
}
