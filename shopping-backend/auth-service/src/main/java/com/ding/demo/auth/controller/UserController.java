package com.ding.demo.auth.controller;

import com.ding.demo.auth.dto.UserInfoResponse;
import com.ding.demo.auth.dto.UserUpdateRequest;
import com.ding.demo.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserInfo(authentication.getName()));
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateUserInfo(
            Authentication authentication,
            @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}