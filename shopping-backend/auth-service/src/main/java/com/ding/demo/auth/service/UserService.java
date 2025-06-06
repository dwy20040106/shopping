package com.ding.demo.auth.service;

import com.ding.demo.auth.dto.UserInfoResponse;
import com.ding.demo.auth.dto.UserUpdateRequest;

public interface UserService {
    UserInfoResponse getUserInfo(String userId);

    String updateUserInfo(String userId, UserUpdateRequest request);
}