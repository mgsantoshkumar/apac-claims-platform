package com.chubb.apac.claims.security;

import com.chubb.apac.claims.common.enums.Role;

public record AuthenticatedUser(
        String userId,
        String username,
        String displayName,
        Role role
) {
}
