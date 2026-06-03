package com.example.erpinventory.service.impl;

import com.example.erpinventory.entity.SysPermission;
import com.example.erpinventory.entity.SysRole;
import com.example.erpinventory.entity.SysUser;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.mapper.SysPermissionMapper;
import com.example.erpinventory.mapper.SysRoleMapper;
import com.example.erpinventory.mapper.UserMapper;
import com.example.erpinventory.service.AuthService;
import com.example.erpinventory.service.dto.UserAuthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public UserAuthInfo loadUserAuthByUsername(String username) {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserAuthInfo(user);
    }

    @Override
    public UserAuthInfo loadUserAuthByUserId(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserAuthInfo(user);
    }

    private UserAuthInfo buildUserAuthInfo(SysUser user) {
        UserAuthInfo authInfo = new UserAuthInfo();
        authInfo.setUser(user);

        SysRole role = user.getRoleId() == null ? null : sysRoleMapper.selectById(user.getRoleId());
        if (role != null && (role.getStatus() == null || role.getStatus() != 1)) {
            role = null;
        }
        authInfo.setRole(role);

        List<SysPermission> permissions = role == null ? List.of() : sysPermissionMapper.selectByRoleId(role.getId());
        authInfo.setPermissions(permissions);
        return authInfo;
    }
}
