import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 用于存储请求标识和取消函数
const pendingMap = new Map()

/**
 * 生成每个请求唯一的键
 * @param {*} config 
 * @returns string
 */
function getPendingKey(config) {
    let { url, method, params, data } = config
    if (typeof data === 'string') data = JSON.parse(data)
    return [url, method, JSON.stringify(params), JSON.stringify(data)].join('&')
}

/**
 * 储存每个请求唯一值, 也就是cancel()方法, 用于取消请求
 * @param {*} config 
 */
function addPending(config) {
    const pendingKey = getPendingKey(config)
    config.cancelToken = config.cancelToken || new axios.CancelToken((cancel) => {
        if (!pendingMap.has(pendingKey)) {
            pendingMap.set(pendingKey, cancel)
        }
    })
}

/**
 * 删除重复的请求
 * @param {*} config 
 */
function removePending(config) {
    const pendingKey = getPendingKey(config)
    if (pendingMap.has(pendingKey)) {
        const cancelToken = pendingMap.get(pendingKey)
        cancelToken(pendingKey)
        pendingMap.delete(pendingKey)
    }
}

/**
 * 创建axios实例
 */
const service = axios.create({
    baseURL: '/api', // 使用相对路径，让 Vite 代理处理
    timeout: 15000, // 请求超时时间
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    }
})

/**
 * 请求拦截器
 */
service.interceptors.request.use(
    config => {
        // 检查是否存在重复请求，若存在则取消已发的请求
        removePending(config)
        // 把当前请求添加到pendingMap对象中
        addPending(config)

        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token) {
            // 确保token格式正确
            const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`
            config.headers['Authorization'] = formattedToken
            // 更新localStorage中的token格式
            if (formattedToken !== token) {
                localStorage.setItem('token', formattedToken)
            }
        }

        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

/**
 * 响应拦截器
 */
service.interceptors.response.use(
    response => {
        // 从pendingMap中移除请求
        removePending(response.config)

        // 直接返回响应数据
        return response.data
    },
    error => {
        // 从pendingMap中移除请求
        removePending(error.config || {})

        if (axios.isCancel(error)) {
            console.log('已取消的重复请求：' + error.message)
            return Promise.reject(error)
        }

        // 处理错误响应
        const status = error.response?.status
        let errorMessage = ''

        if (error.message === 'Network Error') {
            errorMessage = '网络错误，请检查API服务是否正常运行'
            console.error('网络错误:', error)
        } else if (status === 401) {
            errorMessage = '登录已过期，请重新登录'
            // 清除用户信息
            localStorage.removeItem('token')
            localStorage.removeItem('userId')
            localStorage.removeItem('userType')
            localStorage.removeItem('username')
            localStorage.removeItem('email')
            localStorage.removeItem('phone')
            // 跳转到登录页
            router.push('/login')
        } else if (status === 403) {
            errorMessage = '没有权限访问该资源'
            // 清除用户信息
            localStorage.removeItem('token')
            localStorage.removeItem('userId')
            localStorage.removeItem('userType')
            localStorage.removeItem('username')
            localStorage.removeItem('email')
            localStorage.removeItem('phone')
            // 跳转到登录页
            router.push('/login')
        } else if (status === 400) {
            if (error.response?.data?.error) {
                errorMessage = error.response.data.error
            } else if (error.response?.data?.errors) {
                errorMessage = Object.values(error.response.data.errors).join('; ')
            } else {
                errorMessage = '请求参数错误'
            }
        } else {
            errorMessage = error.response?.data?.error || error.message || '操作失败'
        }

        ElMessage({
            message: errorMessage,
            type: 'error',
            duration: 5000
        })

        return Promise.reject(error)
    }
)

/**
 * 封装 GET 请求
 * @param {string} url 
 * @param {object} params 
 * @param {object} config 
 * @returns Promise
 */
export function get(url, params = {}, config = {}) {
    return service({
        method: 'get',
        url,
        params,
        ...config
    })
}

/**
 * 封装 POST 请求
 * @param {string} url 
 * @param {object} data 
 * @param {object} config 
 * @returns Promise
 */
export function post(url, data = {}, config = {}) {
    return service({
        method: 'post',
        url,
        data,
        ...config
    })
}

/**
 * 封装 PUT 请求
 * @param {string} url 
 * @param {object} data 
 * @param {object} config 
 * @returns Promise
 */
export function put(url, data = {}, config = {}) {
    return service({
        method: 'put',
        url,
        data,
        ...config
    })
}

/**
 * 封装 DELETE 请求
 * @param {string} url 
 * @param {object} data 
 * @param {object} config 
 * @returns Promise
 */
export function del(url, data = {}, config = {}) {
    return service({
        method: 'delete',
        url,
        data,
        ...config
    })
}

export default service