package com.ding.demo.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String userType;
    private LocalDateTime createdAt;
}