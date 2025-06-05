import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置NProgress
NProgress.configure({
    showSpinner: false,
    minimum: 0.2,
    easing: 'ease',
    speed: 500
})

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: {
            requiresAuth: false,
            title: '登录',
            keepAlive: false
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: {
            requiresAuth: false,
            title: '注册',
            keepAlive: false
        }
    },
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: {
            requiresAuth: true,
            title: '首页',
            keepAlive: true
        }
    },
    {
        path: '/profile',
        name: 'UserProfile',
        component: () => import('../views/UserProfile.vue'),
        meta: {
            requiresAuth: true,
            title: '个人信息',
            keepAlive: false
        }
    },
    // 将所有未匹配的路由重定向到首页
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 清除用户信息
function clearUserInfo() {
    localStorage.removeItem('userId')
    localStorage.removeItem('userType')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
    localStorage.removeItem('phone')
    localStorage.removeItem('token')
}

// 检查用户是否已登录
function isLoggedIn() {
    return !!localStorage.getItem('token') && !!localStorage.getItem('userId')
}

// 路由守卫
router.beforeEach((to, from, next) => {
    // 开始进度条
    NProgress.start()

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 后台管理系统` : '后台管理系统'

    // 检查页面是否需要认证
    if (to.meta.requiresAuth) {
        if (!isLoggedIn()) {
            // 用户未登录，清除可能存在的残留信息并重定向到登录页
            clearUserInfo()
            next({
                name: 'Login',
                query: { redirect: to.fullPath }  // 保存原目标路径
            })
            NProgress.done()
            return
        }
        // 用户已登录，允许访问
        next()
    } else {
        // 不需要认证的页面
        if (to.path === '/login' || to.path === '/register') {
            // 如果用户已登录且尝试访问登录或注册页面，重定向到首页
            if (isLoggedIn()) {
                next({ name: 'Home' })
                NProgress.done()
                return
            }
        }
        next()
    }

    // 结束进度条
    NProgress.done()
})

// 路由后置钩子
router.afterEach(() => {
    // 结束进度条
    NProgress.done()
})

// 路由错误处理
router.onError((error) => {
    console.error('路由错误:', error)
    NProgress.done()
})

export default router