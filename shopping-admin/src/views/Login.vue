<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { post } from '../utils/request'

const router = useRouter()

// 表单数据
const loginForm = reactive({
    username: '',
    password: ''
})

// 表单验证规则
const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
    ]
}

// 表单引用
const loginFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 处理登录
const handleLogin = async () => {
    if (!loginFormRef.value) return

    try {
        // 表单验证
        await loginFormRef.value.validate()
        loading.value = true

        // 发送登录请求
        const response = await post('/auth/login', {
            username: loginForm.username,
            password: loginForm.password
        })

        // 保存用户信息到 localStorage
        localStorage.setItem('userId', response.id)
        localStorage.setItem('username', response.username)
        localStorage.setItem('userType', response.userType)
        localStorage.setItem('email', response.email)
        localStorage.setItem('phone', response.phone)
        // 保存 token
        localStorage.setItem('token', response.token || `Bearer ${response.id}`) // 如果后端没有返回 token，使用 id 作为临时 token

        ElMessage({
            message: '登录成功',
            type: 'success'
        })

        // 跳转到首页
        router.push('/')
    } catch (error) {
        console.error('登录失败:', error)
        ElMessage({
            message: error.response?.data?.error || '登录失败，请检查用户名和密码',
            type: 'error',
            duration: 5000
        })
    } finally {
        loading.value = false
    }
}

// 跳转到注册页
const goToRegister = () => {
    router.push('/register')
}

</script>

<template>
    <div class="login-container">
        <el-card class="login-card">
            <template #header>
                <div class="card-header">
                    <h2>系统登录</h2>
                </div>
            </template>

            <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="loginForm.username" placeholder="请输入用户名" @keyup.enter="handleLogin">
                        <template #prefix>
                            <el-icon>
                                <User />
                            </el-icon>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item label="密码" prop="password">
                    <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password
                        @keyup.enter="handleLogin">
                        <template #prefix>
                            <el-icon>
                                <Lock />
                            </el-icon>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
                        登录
                    </el-button>
                </el-form-item>

                <div class="form-footer">
                    <el-button type="primary" link @click="goToRegister">
                        还没有账号？立即注册
                    </el-button>
                </div>
            </el-form>
        </el-card>
    </div>
</template>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f5f5;
}

.login-card {
    width: 480px;
}

.card-header {
    text-align: center;
}

.card-header h2 {
    margin: 0;
    font-size: 24px;
    color: #303133;
}

.form-footer {
    margin-top: 20px;
    text-align: center;
}

:deep(.el-form-item__label) {
    font-weight: bold;
}

:deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #409eff inset;
}
</style>