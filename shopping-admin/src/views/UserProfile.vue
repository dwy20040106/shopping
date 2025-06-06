<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get, put } from '../utils/request'

const router = useRouter()

// 用户信息
const userInfo = ref({
    id: '',
    username: '',
    email: '',
    phone: '',
    userType: '',
    createdAt: ''
})

// 编辑状态
const isEditing = ref(false)
// 编辑表单
const editForm = ref({
    username: '',
    email: '',
    phone: ''
})
// 表单规则
const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ]
}

// 加载状态
const loading = ref(true)
// 表单引用
const editFormRef = ref(null)

// 处理登出
const handleLogout = () => {
    // 清除用户信息
    localStorage.removeItem('userId')
    localStorage.removeItem('userType')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
    localStorage.removeItem('phone')
    localStorage.removeItem('token')

    // 跳转到登录页
    router.push('/login')
}

// 获取用户信息
const fetchUserInfo = async () => {
    try {
        loading.value = true
        const response = await get('/auth/user/info')

        // 更新用户信息
        userInfo.value = {
            id: response.id,
            username: response.username,
            email: response.email,
            phone: response.phone,
            userType: response.userType,
            createdAt: response.createdAt || new Date().toISOString()
        }

        // 同步更新 localStorage 中的信息
        localStorage.setItem('userId', response.id)
        localStorage.setItem('username', response.username)
        localStorage.setItem('email', response.email)
        localStorage.setItem('phone', response.phone)
        localStorage.setItem('userType', response.userType)
    } catch (error) {
        console.error('获取用户信息失败:', error)
        if (error.response?.status === 401 || error.response?.status === 403) {
            ElMessage({
                message: '登录已过期，请重新登录',
                type: 'error',
                duration: 5000
            })
            handleLogout()
        } else {
            ElMessage({
                message: error.response?.data?.error || '获取用户信息失败',
                type: 'error',
                duration: 5000
            })
        }
    } finally {
        loading.value = false
    }
}

// 开始编辑
const startEdit = () => {
    editForm.value = {
        username: userInfo.value.username,
        email: userInfo.value.email,
        phone: userInfo.value.phone || ''
    }
    isEditing.value = true
}

// 取消编辑
const cancelEdit = () => {
    isEditing.value = false
    editFormRef.value?.resetFields()
}

// 保存修改
const saveChanges = async () => {
    if (!editFormRef.value) return

    try {
        await editFormRef.value.validate()
        loading.value = true

        // 发送更新请求
        const response = await put('/auth/user/update', {
            username: editForm.value.username,
            email: editForm.value.email,
            phone: editForm.value.phone || ''
        })

        // 如果返回了新token，更新token
        if (response.token) {
            localStorage.setItem('token', response.token)
        }

        // 更新成功后刷新用户信息
        await fetchUserInfo()

        ElMessage({
            message: '信息更新成功',
            type: 'success'
        })

        isEditing.value = false
    } catch (error) {
        console.error('更新失败:', error)
        ElMessage({
            message: error.response?.data?.error || '更新失败，请稍后重试',
            type: 'error',
            duration: 5000
        })
    } finally {
        loading.value = false
    }
}

// 格式化日期
const formatDate = (dateString) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toLocaleString()
}

// 组件挂载时获取用户信息
onMounted(() => {
    fetchUserInfo()
})
</script>

<template>
    <div class="profile-container">
        <el-card class="profile-card" v-loading="loading">
            <template #header>
                <div class="card-header">
                    <h2>个人信息</h2>
                    <div class="header-actions" v-if="!isEditing">
                        <el-button type="primary" @click="startEdit">编辑信息</el-button>
                    </div>
                </div>
            </template>

            <!-- 查看模式 -->
            <div v-if="!isEditing" class="user-info">
                <el-descriptions :column="1" border>
                    <el-descriptions-item label="用户ID">{{ userInfo.id }}</el-descriptions-item>
                    <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
                    <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
                    <el-descriptions-item label="手机号">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
                    <el-descriptions-item label="用户类型">
                        <el-tag :type="userInfo.userType === 'ADMIN' ? 'danger' : 'primary'">
                            {{ userInfo.userType === 'ADMIN' ? '管理员' : '普通用户' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="注册时间">{{ formatDate(userInfo.createdAt) }}</el-descriptions-item>
                </el-descriptions>
            </div>

            <!-- 编辑模式 -->
            <div v-else class="edit-form">
                <el-form ref="editFormRef" :model="editForm" :rules="rules" label-position="top">
                    <el-form-item label="用户名" prop="username">
                        <el-input v-model="editForm.username" placeholder="请输入用户名" />
                    </el-form-item>

                    <el-form-item label="邮箱" prop="email">
                        <el-input v-model="editForm.email" placeholder="请输入邮箱" />
                    </el-form-item>

                    <el-form-item label="手机号" prop="phone">
                        <el-input v-model="editForm.phone" placeholder="请输入手机号" />
                    </el-form-item>

                    <el-form-item>
                        <el-button type="primary" @click="saveChanges">保存</el-button>
                        <el-button @click="cancelEdit">取消</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>
    </div>
</template>

<style scoped>
.profile-container {
    padding: 20px;
}

.profile-card {
    max-width: 800px;
    margin: 0 auto;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-header h2 {
    margin: 0;
    font-size: 24px;
    color: #303133;
}

.user-info {
    margin: 20px 0;
}

.edit-form {
    max-width: 500px;
    margin: 20px auto;
}

:deep(.el-descriptions__title) {
    font-size: 16px;
    font-weight: bold;
}

:deep(.el-descriptions__label) {
    width: 100px;
}

:deep(.el-form-item__label) {
    font-weight: bold;
}
</style>