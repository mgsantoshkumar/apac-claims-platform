package com.chubb.apac.claims.service;

import com.chubb.apac.claims.domain.UserAccount;
import com.chubb.apac.claims.repository.UserAccountRepository;
import com.chubb.apac.claims.security.JwtService;
import com.chubb.apac.claims.web.dto.LoginRequest;
import com.chubb.apac.claims.web.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(
                jwtService.issueToken(user),
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getTeam()
        );
    }
}
