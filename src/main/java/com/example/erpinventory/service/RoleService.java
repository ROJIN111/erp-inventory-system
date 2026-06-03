package com.example.erpinventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erpinventory.entity.SysRole;

import java.util.List;

public interface RoleService extends IService<SysRole> {

    List<SysRole> listActiveRoles();
}
