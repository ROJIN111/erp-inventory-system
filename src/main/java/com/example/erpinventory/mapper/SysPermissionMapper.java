package com.example.erpinventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erpinventory.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("""
            SELECT p.*
            FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            WHERE rp.role_id = #{roleId}
              AND p.status = 1
            ORDER BY p.module_name, p.action_type, p.id
            """)
    List<SysPermission> selectByRoleId(Long roleId);
}
