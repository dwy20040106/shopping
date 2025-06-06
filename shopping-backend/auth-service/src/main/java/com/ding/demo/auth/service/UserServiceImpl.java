package com.ding.demo.auth.service;

import com.ding.demo.auth.dto.UserInfoResponse;
import com.ding.demo.auth.dto.UserUpdateRequest;
import com.ding.demo.auth.entity.User;
import com.ding.demo.auth.mapper.UserMapper;
import com.ding.demo.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public UserInfoResponse getUserInfo(String userId) {
        User user = userMapper.selectById(Long.parseLong(userId));
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setUserType(user.getUserType());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateUserInfo(String userId, UserUpdateRequest request) {
        User user = userMapper.selectById(Long.parseLong(userId));
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        boolean usernameChanged = false;
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userMapper.countByUsername(request.getUsername()) > 0) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(request.getUsername());
            usernameChanged = true;
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userMapper.countByEmail(request.getEmail()) > 0) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        int updated = userMapper.updateUserInfo(user);
        if (updated != 1) {
            throw new RuntimeException("Failed to update user information");
        }

        // Update authentication context if username was changed
        if (usernameChanged) {
            // Generate new token with updated username
            String newToken = jwtTokenProvider.generateTokenFromUserId(user.getId().toString());

            // Update security context
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    currentAuth.getPrincipal(),
                    currentAuth.getCredentials(),
                    currentAuth.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            return newToken;
        }

        return null;
    }
}