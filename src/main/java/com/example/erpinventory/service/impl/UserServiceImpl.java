package com.example.erpinventory.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erpinventory.dto.request.UserLoginRequest;
import com.example.erpinventory.dto.response.UserInfoResponse;
import com.example.erpinventory.entity.SysUser;
import com.example.erpinventory.exception.BusinessException;
import com.example.erpinventory.mapper.UserMapper;
import com.example.erpinventory.service.AuthService;
import com.example.erpinventory.service.UserService;
import com.example.erpinventory.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Override
    public UserInfoResponse login(UserLoginRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        String username = request.getUsername().trim();
        SysUser user = baseMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        return UserInfoResponse.fromAuthInfo(authService.loadUserAuthByUserId(user.getId()), token);
    }

    @Override
    public boolean register(SysUser user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (user.getPassword().length() < 6) {
            throw new BusinessException("密码长度至少为 6 位");
        }

        String username = user.getUsername().trim();
        SysUser existingUser = baseMapper.selectByUsername(username);
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getDeleted() == null) {
            user.setDeleted(0);
        }
        if (user.getRoleId() == null) {
            user.setRoleId(3L);
        }

        try {
            return save(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名已存在，请更换其他用户名");
        }
    }

    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public List<SysUser> listUsers() {
        return list();
    }

    @Override
    public Page<SysUser> pageUsers(Integer pageNum, Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        return page(page);
    }

    @Override
    public boolean physicallyDeleteById(Long id) {
        return baseMapper.physicallyDeleteById(id) > 0;
    }
}
