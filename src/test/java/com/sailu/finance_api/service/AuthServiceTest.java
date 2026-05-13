package com.sailu.finance_api.service;

import com.sailu.finance_api.dto.AuthResponse;
import com.sailu.finance_api.dto.LoginRequest;
import com.sailu.finance_api.dto.RegisterRequest;
import com.sailu.finance_api.entity.User;
import com.sailu.finance_api.entity.UserRole;
import com.sailu.finance_api.repository.UserRepository;
import com.sailu.finance_api.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(UserRole.USER)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Register - success")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("test@example.com")
                        .password("encodedPassword")
                        .roles("USER")
                        .build());
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("mockToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register - email already exists throws exception")
    void register_EmailAlreadyExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(request));

        assertEquals("Email already registered", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login - success")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("test@example.com")
                        .password("encodedPassword")
                        .roles("USER")
                        .build());
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("mockToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getAccessToken());
        assertEquals("test@example.com", response.getEmail());
    }
}