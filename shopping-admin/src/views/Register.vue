<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { post } from '../utils/request'

const router = useRouter()

// 表单数据
const registerForm = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: ''
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
    ],
    confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (value !== registerForm.password) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ]
}

// 表单引用
const registerFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 处理注册
const handleRegister = async () => {
    if (!registerFormRef.value) return

    try {
        // 表单验证
        await registerFormRef.value.validate()
        loading.value = true

        // 发送注册请求，直接发送原始密码，让后端处理加密
        const response = await post('/auth/register', {
            username: registerForm.username,
            password: registerForm.password,
            email: registerForm.email,
            phone: registerForm.phone || undefined // 如果为空则不发送
        })

        ElMessage({
            message: '注册成功',
            type: 'success'
        })

        // 注册成功后跳转到登录页
        router.push('/login')
    } catch (error) {
        console.error('注册失败:', error)
        let errorMessage = error.response?.data?.error
        if (error.response?.data?.errors) {
            errorMessage = Object.values(error.response.data.errors).join('; ')
        }
        ElMessage({
            message: errorMessage || '注册失败，请稍后重试',
            type: 'error',
            duration: 5000
        })
    } finally {
        loading.value = false
    }
}

// 跳转到登录页
const goToLogin = () => {
    router.push('/login')
}
</script>

<template>
    <div class="register-container">
        <el-card class="register-card">
            <template #header>
                <div class="card-header">
                    <h2>用户注册</h2>
                </div>
            </template>

            <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-position="top">

                <el-form-item label="用户名" prop="username">
                    <el-input v-model="registerForm.username" placeholder="请输入用户名" :prefix-icon="User" />
                </el-form-item>

                <el-form-item label="密码" prop="password">
                    <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password
                        :prefix-icon="Lock" />
                </el-form-item>

                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password
                        :prefix-icon="Lock" />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="registerForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
                </el-form-item>

                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" :loading="loading" @click="handleRegister" style="width: 100%">
                        注册
                    </el-button>
                </el-form-item>

                <div class="form-footer">
                    <el-button type="primary" link @click="goToLogin">
                        已有账号？立即登录
                    </el-button>
                </div>
            </el-form>
        </el-card>
    </div>
</template>

<style scoped>
.register-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f5f5;
}

.register-card {
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