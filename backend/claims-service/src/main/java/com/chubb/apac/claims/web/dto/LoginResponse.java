package com.chubb.apac.claims.web.dto;

import com.chubb.apac.claims.common.enums.Role;

public record LoginResponse(
        String token,
        String userId,
        String username,
        String displayName,
        Role role,
        String team
) {
}
