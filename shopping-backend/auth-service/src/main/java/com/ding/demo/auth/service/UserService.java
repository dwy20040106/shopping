package com.ding.demo.auth.service;

import com.ding.demo.auth.dto.UserInfoResponse;
import com.ding.demo.auth.dto.UserUpdateRequest;

public interface UserService {
    UserInfoResponse getUserInfo(String username);

    void updateUserInfo(String username, UserUpdateRequest request);
}