package com.example.erpinventory.util;

import com.example.erpinventory.entity.SysUser;
import com.example.erpinventory.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentOperatorResolver {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    public CurrentOperator resolve() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Long userId = jwtUtil.extractUserId(token);
                    String username = jwtUtil.extractUsername(token);
                    return buildOperator(userId, username);
                } catch (Exception ignored) {
                }
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String username && !"anonymousUser".equals(username)) {
                return buildOperator(null, username);
            }
        }

        return new CurrentOperator(0L, "system");
    }

    private CurrentOperator buildOperator(Long userId, String username) {
        SysUser user = null;
        if (userId != null) {
            user = userMapper.selectById(userId);
        }
        if (user == null && StringUtils.hasText(username)) {
            user = userMapper.selectByUsername(username);
        }

        Long resolvedUserId = userId;
        String resolvedName = username;

        if (user != null) {
            resolvedUserId = user.getId();
            resolvedName = StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
        }

        if (!StringUtils.hasText(resolvedName)) {
            resolvedName = "system";
        }
        if (resolvedUserId == null) {
            resolvedUserId = 0L;
        }

        return new CurrentOperator(resolvedUserId, resolvedName);
    }

    public record CurrentOperator(Long userId, String username) {
    }
}
