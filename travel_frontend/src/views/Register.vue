<template>
  <div class="register-page">
    <div class="bg-overlay"></div>
    <el-card class="register-card">
      <div class="header">
        <h2>加入云游</h2>
        <p>创建您的专属旅行账号</p>
      </div>

      <el-form :model="registerForm" :rules="rules" ref="registerRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="设置用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="设置密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Check" show-password />
        </el-form-item>
        
        <el-button type="primary" class="reg-btn" @click="handleRegister" :loading="loading">
          注 册
        </el-button>
      </el-form>

      <div class="footer">
        已有账号？<el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
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
const registerRef = ref(null)

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

// 表单校验规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = () => {
  registerRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await request.post('/users/register', registerForm.value)
      if (res.data.success) {
        ElMessage.success('注册成功！正在跳转登录...')
        setTimeout(() => router.push('/login'), 1500)
      } else {
        ElMessage.error(res.data.message)
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  height: 100vh; display: flex; justify-content: center; align-items: center;
  background: url('https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=1600') no-repeat center;
  background-size: cover; position: relative;
}
.bg-overlay { position: absolute; width: 100%; height: 100%; background: rgba(0,0,0,0.3); }
.register-card { width: 400px; padding: 20px; border-radius: 15px; z-index: 10; background: rgba(255,255,255,0.9); backdrop-filter: blur(10px); }
.header { text-align: center; margin-bottom: 25px; }
.reg-btn { width: 100%; height: 45px; margin-top: 10px; border-radius: 8px; }
.footer { margin-top: 20px; text-align: center; font-size: 14px; }
</style>