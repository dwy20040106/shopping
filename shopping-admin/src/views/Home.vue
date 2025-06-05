<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get } from '../utils/request'
import { Search, ShoppingCart, User } from '@element-plus/icons-vue'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '')
const searchKeyword = ref('')

// 商品分类
const categories = ref([
    { id: 1, name: '手机数码' },
    { id: 2, name: '电脑办公' },
    { id: 3, name: '家用电器' },
    { id: 4, name: '服装鞋包' },
    { id: 5, name: '食品生鲜' },
    { id: 6, name: '图书音像' }
])

// 热门商品
const hotProducts = ref([
    {
        id: 1,
        name: 'iPhone 15 Pro',
        price: 8999,
        image: 'https://via.placeholder.com/200x200',
        description: '最新款iPhone，搭载A17芯片'
    },
    {
        id: 2,
        name: 'MacBook Pro',
        price: 12999,
        image: 'https://via.placeholder.com/200x200',
        description: '强大的专业级笔记本'
    },
    {
        id: 3,
        name: 'iPad Air',
        price: 4799,
        image: 'https://via.placeholder.com/200x200',
        description: '轻薄便携的平板电脑'
    },
    {
        id: 4,
        name: 'AirPods Pro',
        price: 1999,
        image: 'https://via.placeholder.com/200x200',
        description: '主动降噪无线耳机'
    }
])

// 处理搜索
const handleSearch = () => {
    if (!searchKeyword.value) {
        ElMessage({
            message: '请输入搜索关键词',
            type: 'warning'
        })
        return
    }
    // TODO: 实现搜索功能
    console.log('搜索:', searchKeyword.value)
}

// 处理退出登录
const handleLogout = () => {
    localStorage.removeItem('userId')
    localStorage.removeItem('userType')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
    localStorage.removeItem('phone')
    localStorage.removeItem('token')

    ElMessage({
        message: '已退出登录',
        type: 'success'
    })

    router.push('/login')
}

// 跳转到用户信息页
const goToProfile = () => {
    router.push('/profile')
}

// 跳转到购物车
const goToCart = () => {
    router.push('/cart')
}
</script>

<template>
    <div class="home-container">
        <!-- 顶部导航栏 -->
        <el-header class="header">
            <div class="header-left">
                <h1 class="logo">购物商城</h1>
            </div>
            <div class="header-center">
                <el-input v-model="searchKeyword" placeholder="搜索商品" class="search-input">
                    <template #append>
                        <el-button :icon="Search" @click="handleSearch" />
                    </template>
                </el-input>
            </div>
            <div class="header-right">
                <el-button :icon="ShoppingCart" @click="goToCart" text>购物车</el-button>
                <el-dropdown>
                    <el-button :icon="User" text>
                        你好，{{ username }}
                    </el-button>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item @click="goToProfile">个人信息</el-dropdown-item>
                            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </el-header>

        <!-- 主要内容区 -->
        <div class="main-content">
            <!-- 分类导航 -->
            <el-row class="category-nav">
                <el-col :span="24">
                    <el-menu mode="horizontal" :ellipsis="false">
                        <el-menu-item v-for="category in categories" :key="category.id" :index="category.id.toString()">
                            {{ category.name }}
                        </el-menu-item>
                    </el-menu>
                </el-col>
            </el-row>

            <!-- 商品展示区 -->
            <div class="product-section">
                <h2>热门商品</h2>
                <el-row :gutter="20">
                    <el-col v-for="product in hotProducts" :key="product.id" :xs="24" :sm="12" :md="6">
                        <el-card :body-style="{ padding: '0px' }" class="product-card">
                            <img :src="product.image" class="product-image" />
                            <div class="product-info">
                                <h3>{{ product.name }}</h3>
                                <p class="description">{{ product.description }}</p>
                                <div class="price-row">
                                    <span class="price">¥{{ product.price }}</span>
                                    <el-button type="primary" size="small">加入购物车</el-button>
                                </div>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
            </div>
        </div>
    </div>
</template>

<style scoped>
.home-container {
    min-height: 100vh;
    background-color: #f5f5f5;
}

.header {
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    height: 60px;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
}

.header-left .logo {
    margin: 0;
    font-size: 24px;
    color: #409EFF;
}

.header-center {
    flex: 1;
    max-width: 500px;
    margin: 0 20px;
}

.search-input {
    width: 100%;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 20px;
}

.main-content {
    margin-top: 60px;
    padding: 20px;
}

.category-nav {
    background-color: #fff;
    margin-bottom: 20px;
}

.product-section {
    margin-top: 20px;
}

.product-section h2 {
    margin-bottom: 20px;
    color: #303133;
}

.product-card {
    margin-bottom: 20px;
    transition: transform 0.3s;
}

.product-card:hover {
    transform: translateY(-5px);
}

.product-image {
    width: 100%;
    height: 200px;
    object-fit: cover;
}

.product-info {
    padding: 14px;
}

.product-info h3 {
    margin: 0;
    font-size: 16px;
    color: #303133;
}

.description {
    font-size: 14px;
    color: #909399;
    margin: 8px 0;
}

.price-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10px;
}

.price {
    color: #f56c6c;
    font-size: 20px;
    font-weight: bold;
}

:deep(.el-menu--horizontal) {
    border-bottom: none;
}

:deep(.el-menu-item) {
    font-size: 14px;
}
</style>