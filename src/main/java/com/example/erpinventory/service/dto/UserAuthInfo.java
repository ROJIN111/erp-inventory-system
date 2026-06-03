package com.example.erpinventory.service.dto;

import com.example.erpinventory.entity.SysPermission;
import com.example.erpinventory.entity.SysRole;
import com.example.erpinventory.entity.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class UserAuthInfo {

    private SysUser user;

    private SysRole role;

    private List<SysPermission> permissions;
}
