package com.focustimer.backend.service;

import com.focustimer.backend.dto.AuthRequest;
import com.focustimer.backend.dto.AuthResponse;
import com.focustimer.backend.dto.RegisterRequest;
import com.focustimer.backend.entity.User;
import com.focustimer.backend.repository.UserRepository;
import com.focustimer.backend.security.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username already taken");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already registered");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();
        userRepository.save(user);

        return new AuthResponse(
                jwtTokenProvider.generateToken(user.getUsername()),
                user.getUsername(),
                user.getId());
    }

    public AuthResponse login(AuthRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow();
        return new AuthResponse(
                jwtTokenProvider.generateToken(user.getUsername()),
                user.getUsername(),
                user.getId());
    }
}
