package com.example.erpinventory.dto.response;

import com.example.erpinventory.service.dto.UserAuthInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserInfoResponse implements Serializable {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private String token;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private List<String> permissions;

    public static UserInfoResponse fromAuthInfo(UserAuthInfo authInfo, String token) {
        UserInfoResponse response = new UserInfoResponse();
        response.setId(authInfo.getUser().getId());
        response.setUsername(authInfo.getUser().getUsername());
        response.setRealName(authInfo.getUser().getRealName());
        response.setEmail(authInfo.getUser().getEmail());
        response.setPhone(authInfo.getUser().getPhone());
        response.setAvatar(authInfo.getUser().getAvatar());
        response.setToken(token);
        if (authInfo.getRole() != null) {
            response.setRoleId(authInfo.getRole().getId());
            response.setRoleCode(authInfo.getRole().getRoleCode());
            response.setRoleName(authInfo.getRole().getRoleName());
        }
        response.setPermissions(authInfo.getPermissions() == null
                ? List.of()
                : authInfo.getPermissions().stream().map(item -> item.getPermissionCode()).toList());
        return response;
    }
}
