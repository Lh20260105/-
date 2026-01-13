import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: 'http://localhost:8080/api', // 指向你的后端地址
    timeout: 5000
})

export default request