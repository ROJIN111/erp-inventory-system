package com.example.erpinventory.service;

import com.example.erpinventory.service.dto.UserAuthInfo;

public interface AuthService {

    UserAuthInfo loadUserAuthByUsername(String username);

    UserAuthInfo loadUserAuthByUserId(Long userId);
}
