package com.ding.demo.auth.service;

import com.ding.demo.auth.dto.AuthResponse;
import com.ding.demo.auth.dto.LoginRequest;
import com.ding.demo.auth.dto.RegisterRequest;
import com.ding.demo.auth.entity.User;
import com.ding.demo.auth.mapper.UserMapper;
import com.ding.demo.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final UserMapper userMapper;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                if (userMapper.countByUsername(request.getUsername()) > 0) {
                        throw new RuntimeException("Username already exists");
                }
                if (userMapper.countByEmail(request.getEmail()) > 0) {
                        throw new RuntimeException("Email already exists");
                }

                User user = new User();
                user.setUsername(request.getUsername());
                String encodedPassword = passwordEncoder.encode(request.getPassword());
                user.setPassword(encodedPassword);
                user.setEmail(request.getEmail());
                user.setPhone(request.getPhone());
                user.setUserType("USER");
                user.setEnabled(true);

                userMapper.insert(user);

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtTokenProvider.generateTokenFromUserId(user.getId().toString());

                return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getPhone(),
                                user.getUserType());
        }

        public AuthResponse login(LoginRequest request) {
                User user = userMapper.findByUsername(request.getUsername());
                if (user == null) {
                        throw new RuntimeException("User not found");
                }

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new RuntimeException("Invalid password");
                }

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtTokenProvider.generateTokenFromUserId(user.getId().toString());

                return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getPhone(),
                                user.getUserType());
        }
}