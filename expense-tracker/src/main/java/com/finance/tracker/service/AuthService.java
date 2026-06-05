package com.finance.tracker.service;

import com.finance.tracker.dto.AuthDtos.*;
import com.finance.tracker.entity.Role;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ApiException;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        // bootstrap: first user becomes admin too — handy for a fresh deploy
        if (userRepository.count() == 0) {
            roles.add(Role.ADMIN);
        }

        User u = User.builder()
                .name(req.name().trim())
                .email(req.email().toLowerCase().trim())
                .password(passwordEncoder.encode(req.password()))
                .roles(roles)
                .build();

        userRepository.save(u);
        return issueToken(u);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User u = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return issueToken(u);
    }

    private AuthResponse issueToken(User u) {
        String primaryRole = u.getRoles().contains(Role.ADMIN) ? "ADMIN" : "USER";
        String token = jwtService.generateToken(
                u.getEmail(),
                Map.of("roles", u.getRoles().stream().map(Enum::name).toList())
        );
        return new AuthResponse(token, u.getEmail(), u.getName(), primaryRole);
    }
}
