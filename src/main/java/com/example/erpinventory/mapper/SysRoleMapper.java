package com.example.erpinventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erpinventory.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} LIMIT 1")
    SysRole selectByCode(String roleCode);
}
