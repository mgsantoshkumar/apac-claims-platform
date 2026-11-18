package com.chubb.apac.claims.web;

import com.chubb.apac.claims.common.enums.Role;
import com.chubb.apac.claims.service.AuthService;
import com.chubb.apac.claims.web.dto.LoginRequest;
import com.chubb.apac.claims.web.dto.LoginResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void loginReturnsTokenFromService() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        LoginRequest request = new LoginRequest("claimant1", "password");
        when(authService.login(request)).thenReturn(
                new LoginResponse("token-123", "u-1", "claimant1", "Aisha Tan", Role.CLAIMANT, null)
        );

        LoginResponse response = controller.login(request);

        assertThat(response.token()).isEqualTo("token-123");
        assertThat(response.role()).isEqualTo(Role.CLAIMANT);
    }
}
