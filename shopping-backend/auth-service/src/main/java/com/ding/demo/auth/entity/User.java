package com.ding.demo.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField
    private String username;

    @TableField
    private String password;

    private String email;
    private String phone;

    @TableField(value = "user_type")
    private String userType = "USER";

    private Boolean enabled = true;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public boolean getEnabled() {
        return enabled != null && enabled;
    }

    public boolean isEnabled() {
        return getEnabled();
    }
}