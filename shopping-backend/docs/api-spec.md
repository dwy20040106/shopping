# 认证服务 API 文档

## 基础信息

- 基础路径: `http://localhost:8080/api/auth`
- 所有请求和响应的 Content-Type 均为 `application/json`

## 接口列表

### 1. 用户登录

#### 请求信息

- 路径: `/login`
- 方法: `POST`
- 请求体:

```json
{
    "username": "string", // 用户名
    "password": "string"  // 密码
}
```

#### 响应信息

- 成功响应 (200 OK):

```json
{
    "token": "string",     // JWT令牌
    "username": "string",  // 用户名
    "userType": "string"  // 用户类型（USER/ADMIN）
}
```

- 错误响应 (401 Unauthorized):

```json
{
    "error": "用户名或密码错误"
}
```

### 2. 用户注册

#### 请求信息

- 路径: `/register`
- 方法: `POST`
- 请求体:

```json
{
    "username": "string", // 用户名（必填）
    "password": "string", // 密码（必填）
    "email": "string",    // 邮箱（必填，需符合邮箱格式）
    "phone": "string"     // 手机号（选填，如填写需符合格式：1开头的11位数字）
}
```

#### 响应信息

- 成功响应 (200 OK):

```json
{
    "id": 1,
    "username": "string",
    "email": "string",
    "phone": "string",
    "userType": "USER",
    "enabled": true,
    "createdAt": "2024-03-14T12:00:00",
    "updatedAt": null
}
```

- 错误响应 (400 Bad Request):

```json
{
    "error": "用户名已存在"
}
```

或

```json
{
    "errors": {
        "username": "用户名不能为空",
        "password": "密码不能为空",
        "email": "邮箱格式不正确",
        "phone": "手机号格式不正确"
    }
}
```

## 注意事项

1. 密码在传输和存储时都经过加密处理
2. 注册用户默认为 USER 角色
3. 新注册用户默认状态为启用（enabled=true）
4. 登录成功后获取的 JWT 令牌需要在后续请求中通过 Authorization 头部传递，格式为：`Bearer {token}`
5. 令牌有效期为 24 小时