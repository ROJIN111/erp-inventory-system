package com.example.erpinventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erpinventory.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(String username);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int physicallyDeleteById(Long id);
}
