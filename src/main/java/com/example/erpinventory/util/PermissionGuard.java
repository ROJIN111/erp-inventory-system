package com.example.erpinventory.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PermissionGuard {

    public void require(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> permissionCode.equals(authority.getAuthority()))) {
            throw new AccessDeniedException("没有权限访问该资源");
        }
    }
}
