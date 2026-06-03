package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erpinventory.dto.request.UserLoginRequest;
import com.example.erpinventory.dto.response.UserInfoResponse;
import com.example.erpinventory.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<SysUser> {

    /**
     * 用户登录
     * @param request 登录请求
     * @return 用户信息
     */
    UserInfoResponse login(UserLoginRequest request);

    /**
     * 用户注册
     * @param user 用户信息
     * @return 是否成功
     */
    boolean register(SysUser user);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    SysUser getByUsername(String username);

    /**
     * 获取用户列表
     * @return 用户列表
     */
    List<SysUser> listUsers();

    /**
     * 分页查询用户
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<SysUser> pageUsers(Integer pageNum, Integer pageSize);

    /**
     * 物理删除用户
     * @param id 用户ID
     * @return 是否成功
     */
    boolean physicallyDeleteById(Long id);
}
