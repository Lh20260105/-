<template>
  <div class="login-page">
    <div class="bg-overlay"></div>
    
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <el-icon size="40" color="#409EFF"><Position /></el-icon>
        <h2>云游推荐系统</h2>
        <p>探索世界，从这里开始</p>
      </div>

      <el-form :model="loginForm" label-width="0" size="large">
        <el-form-item>
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            prefix-icon="User"
            clearable 
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock" 
            show-password 
          />
        </el-form-item>
        <el-button 
          type="primary" 
          class="login-btn" 
          @click="handleLogin"
          :loading="loading"
        >
          立即登录
        </el-button>
      </el-form>
      
      <div class="login-footer">
        <span>还没有账号？<el-link type="primary" :underline="false" @click="$router.push('/register')">立即注册</el-link></span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const loginForm = ref({ username: '', password: '' })

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请填写完整的账号密码')
    return
  }
  
  loading.value = true
  try {
    const res = await request.post('/users/login', loginForm.value)
    const result = res.data 

    if (result.success) {
      ElMessage.success('欢迎回来！')
      const userInfo = result.data 
      
      // --- 【关键修复点】 ---
      // 必须使用 sessionStorage，这样才能和 App.vue 的读取逻辑匹配
      // 同时也解决了管理员和用户双开互相干扰的问题
      sessionStorage.setItem('user_info', JSON.stringify(userInfo))
      
      // 使用 window.location.href 强制刷新跳转，确保 App.vue 重新从 sessionStorage 读取最新角色
      if (userInfo && userInfo.role === 'ADMIN') {
        window.location.href = '/attractions' 
      } else {
        window.location.href = '/home'
      }
      
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error("请求出错：", error)
    ElMessage.error('服务器连接失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url('https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=1600') no-repeat center center;
  background-size: cover;
  position: relative;
}

.bg-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.3);
}

.login-card {
  width: 420px;
  padding: 20px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 10px 0;
  color: #303133;
  letter-spacing: 2px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 10px;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}
</style>