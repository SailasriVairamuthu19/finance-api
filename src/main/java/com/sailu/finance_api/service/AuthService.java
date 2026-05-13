package com.sailu.finance_api.service;

import com.sailu.finance_api.dto.AuthResponse;
import com.sailu.finance_api.dto.LoginRequest;
import com.sailu.finance_api.dto.RegisterRequest;
import com.sailu.finance_api.entity.User;
import com.sailu.finance_api.repository.UserRepository;
import com.sailu.finance_api.util.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(
                customUserDetailsService.loadUserByUsername(user.getEmail()));

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(
                customUserDetailsService.loadUserByUsername(user.getEmail()));

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}