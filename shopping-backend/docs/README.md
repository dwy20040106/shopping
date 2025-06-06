# 电商后台系统模块说明文档

## 1. 公共模块 (common)
**核心作用**：提供全系统共享的基础组件和工具
- 统一异常处理机制
- 通用工具类（日期处理、字符串处理等）
- 全局常量定义
- 基础DTO对象
- 系统日志配置
- 跨域处理配置

## 2. 认证授权模块 (auth)
**核心作用**：处理所有与身份认证和权限控制相关的功能
- 用户注册/登录/登出
- JWT令牌签发与验证
- 基于角色的权限控制(RBAC)
- 接口访问权限校验
- 多终端认证支持（Web/App/管理端）

## 3. 用户服务模块 (user)
**核心作用**：管理用户相关数据和业务
- 用户个人信息管理
- 收货地址管理
- 用户收藏夹
- 购物车功能
- 会员积分系统
- 用户行为记录

## 4. 管理后台模块 (admin)
**核心作用**：提供平台管理功能
- 商品信息管理（上架/下架/编辑）
- 订单管理系统
- 用户数据管理
- 销售数据统计
- 运营活动配置
- 系统参数设置

## 5. 商品服务模块 (product)
**核心作用**：处理商品相关业务
- 商品分类管理
- 商品信息维护
- 库存管理系统
- 商品搜索服务
- 商品评价管理
- 商品推荐算法

## 6. 订单服务模块 (order)
**核心作用**：处理订单全生命周期
- 订单创建与支付
- 订单状态管理
- 订单物流跟踪
- 退换货处理
- 订单结算
- 订单数据统计

## 7. 支付服务模块 (payment)
**核心作用**：处理所有支付相关业务
- 支付渠道对接（微信/支付宝等）
- 支付订单生成
- 支付结果回调处理
- 退款处理
- 支付对账
- 支付风控

## 8. 通知服务模块 (notify)
**核心作用**：处理系统各类消息通知
- 短信通知服务
- 邮件推送服务
- 站内消息系统
- 订单状态变更通知
- 营销活动推送
- 系统公告管理

## 模块间协作关系
1. **用户下单流程**：
   user → order → payment → notify
2. **商品管理流程**：
   admin → product
3. **权限控制流程**：
   所有模块 → auth
4. **数据统计流程**：
   admin ← order + payment + user

---

## ✅ 技术栈确定：

| 模块     | 技术栈                                                       |
| -------- | ------------------------------------------------------------ |
| 前端     | Vue 3 + Vite + Pinia + Vue Router + Axios + Element Plus     |
| 后端     | Spring Boot + Spring Security + MyBatis Plus + JWT + Lombok + Swagger |
| 数据库   | MySQL（推荐 8.x）                                            |
| 运维建议 | Nginx（前端部署）、Docker（后端/数据库容器化）               |

---

## 数据库

### 1. 用户表 `users`

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名，唯一',
    password VARCHAR(100) NOT NULL COMMENT '用户密码，建议加密存储',
    email VARCHAR(100) COMMENT '邮箱地址，可用于找回密码',
    phone VARCHAR(20) COMMENT '手机号码',
    user_type VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '用户类型(USER/ADMIN)',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '用户注册时间',
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台用户表';
```

------

### 2. 店铺表 `stores`

```sql
CREATE TABLE stores (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '店铺ID，主键',
    name VARCHAR(100) NOT NULL COMMENT '店铺名称',
    owner_id INT NOT NULL COMMENT '店铺所有者，关联用户表ID',
    description TEXT COMMENT '店铺简介或描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '店铺创建时间',
    FOREIGN KEY (owner_id) REFERENCES users(id)
) COMMENT='入驻店铺信息表';
```

------

###  3. 商品表 `products`

```sql
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID，主键',
    store_id INT NOT NULL COMMENT '所属店铺ID，外键关联stores',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价，单位元',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    sales INT DEFAULT 0 COMMENT '销量统计',
    status ENUM('on_sale', 'off_shelf') DEFAULT 'on_sale' COMMENT '商品状态，on_sale在售，off_shelf下架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '商品上架时间',
    FOREIGN KEY (store_id) REFERENCES stores(id)
) COMMENT='商品信息表';
```

------

###  4. 商品图片表 `product_images`

```sql
CREATE TABLE product_images (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    product_id INT NOT NULL COMMENT '关联商品ID',
    image_url VARCHAR(255) NOT NULL COMMENT '图片链接地址',
    is_main BOOLEAN DEFAULT FALSE COMMENT '是否为主图（封面图）',
    FOREIGN KEY (product_id) REFERENCES products(id)
) COMMENT='商品图片表';
```

------

###  5. 购物车表 `cart_items`

```sql
CREATE TABLE cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
    user_id INT NOT NULL COMMENT '用户ID，外键关联users',
    product_id INT NOT NULL COMMENT '商品ID，外键关联products',
    quantity INT NOT NULL COMMENT '加入购物车的商品数量',
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
) COMMENT='购物车记录表';
```

------

###  6. 订单表 `orders`

```sql
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    user_id INT NOT NULL COMMENT '下单用户ID',
    store_id INT NOT NULL COMMENT '订单关联的店铺ID',
    total_price DECIMAL(10,2) NOT NULL COMMENT '订单总价',
    status ENUM('pending', 'paid', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending' COMMENT '订单状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (store_id) REFERENCES stores(id)
) COMMENT='用户订单表';
```

------

### 7. 订单详情表 `order_items`

```sql
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id INT NOT NULL COMMENT '所属订单ID',
    product_id INT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '购买数量',
    price DECIMAL(10,2) NOT NULL COMMENT '购买时商品单价',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
) COMMENT='订单商品明细表';
```

------

###  8. 发货状态表 `shipping`

```sql
CREATE TABLE shipping (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '发货记录ID',
    order_id INT NOT NULL COMMENT '订单ID',
    address VARCHAR(255) NOT NULL COMMENT '收货地址',
    shipping_status ENUM('not_shipped', 'shipped', 'delivered') DEFAULT 'not_shipped' COMMENT '物流状态',
    shipping_company VARCHAR(100) COMMENT '物流公司名称，如顺丰/圆通等',
    tracking_number VARCHAR(100) COMMENT '物流单号',
    shipped_at DATETIME COMMENT '发货时间',
    delivered_at DATETIME COMMENT '送达时间',
    FOREIGN KEY (order_id) REFERENCES orders(id)
) COMMENT='订单发货与物流信息表';
```

------



---

## 视图表

### 1. 用户订单视图（含商品信息）

```
sql复制编辑CREATE VIEW view_user_orders AS
SELECT
  o.id AS order_id,
  o.user_id,
  o.store_id,
  s.name AS store_name,
  oi.product_id,
  p.name AS product_name,
  p.price AS product_price,
  oi.quantity,
  o.total_price,
  o.status AS order_status,
  sh.status AS shipping_status,
  o.create_time
FROM
  orders o
  JOIN order_items oi ON o.id = oi.order_id
  JOIN products p ON oi.product_id = p.id
  JOIN stores s ON o.store_id = s.id
  LEFT JOIN shipping sh ON o.id = sh.order_id;
```

✅ 用于前端用户"我的订单"页接口。

------

### 2. 管理端商品销量统计视图

```
sql复制编辑CREATE VIEW view_product_sales AS
SELECT
  p.id AS product_id,
  p.name AS product_name,
  s.name AS store_name,
  SUM(oi.quantity) AS total_sales
FROM
  products p
  JOIN stores s ON p.store_id = s.id
  JOIN order_items oi ON p.id = oi.product_id
  JOIN orders o ON oi.order_id = o.id
WHERE o.status != 'cancelled'
GROUP BY p.id;
```

✅ 用于后台销售统计图表、排行榜等功能。

------

### 3. 用户购物车视图

```
sql复制编辑CREATE VIEW view_cart_items AS
SELECT
  c.id AS cart_id,
  c.user_id,
  p.id AS product_id,
  p.name AS product_name,
  p.price,
  c.quantity,
  (p.price * c.quantity) AS subtotal
FROM
  cart_items c
  JOIN products p ON c.product_id = p.id;
```

✅ 用于前端"购物车页面"接口返回结构。

------

### 4. 商品信息聚合视图（含封面图）

```
sql复制编辑CREATE VIEW view_product_info AS
SELECT
  p.id AS product_id,
  p.name,
  p.description,
  p.price,
  p.stock,
  s.name AS store_name,
  pi.image_url AS cover_image
FROM
  products p
  JOIN stores s ON p.store_id = s.id
  LEFT JOIN (
    SELECT product_id, MIN(image_url) AS image_url
    FROM product_images
    GROUP BY product_id
  ) pi ON p.id = pi.product_id;
```

✅ 用于前端首页、商品列表页。

