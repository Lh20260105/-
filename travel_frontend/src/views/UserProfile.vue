<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>账户设置</span>
        </div>
      </template>

      <div class="user-info-section">
        <el-avatar :size="100" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        <h2 class="welcome-text">你好，{{ userForm.username }}</h2>
        <el-tag :type="userForm.role === 'ADMIN' ? 'danger' : 'success'">
          {{ userForm.role === 'ADMIN' ? '系统管理员' : '普通旅行者' }}
        </el-tag>
      </div>

      <el-divider />

      <el-form :model="userForm" label-width="100px" style="max-width: 460px; margin: 0 auto;">
        <el-form-item label="登录账号">
          <el-input v-model="userForm.username" disabled />
          <small style="color: #999;">账号暂不支持修改</small>
        </el-form-item>
        
        <el-form-item label="用户昵称">
          <el-input v-model="userForm.nickname" placeholder="请输入您的昵称" />
        </el-form-item>

        <el-form-item label="注册时间">
          <el-input v-model="userForm.createTime" disabled />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleUpdate" :loading="loading">保存修改</el-button>
          <el-button @click="$router.push('/home')">返回首页</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const userForm = ref({
  id: '',
  username: '',
  nickname: '',
  role: '',
  createTime: ''
})

// 1. 初始化加载用户信息
const loadUserInfo = () => {
  const info = JSON.parse(localStorage.getItem('user_info') || '{}')
  if (info.id) {
    userForm.value = { ...info }
  }
}

// 2. 提交修改
const handleUpdate = async () => {
  loading.value = true
  try {
    const res = await request.post('/users/update', userForm.value)
    if (res.data.success) {
      ElMessage.success('资料已同步更新！')
      // 关键：修改成功后，要更新本地存储的 user_info，否则刷新页面后又变回去了
      localStorage.setItem('user_info', JSON.stringify(res.data.data))
      // 强制刷新页面以同步导航栏的名字
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadUserInfo)
</script>

<style scoped>
.profile-container {
  padding: 40px;
  display: flex;
  justify-content: center;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.profile-card {
  width: 100%;
  max-width: 800px;
  border-radius: 12px;
}
.user-info-section {
  text-align: center;
  padding: 20px 0;
}
.welcome-text {
  margin: 15px 0 5px;
  color: #303133;
}
.card-header {
  font-weight: bold;
  font-size: 18px;
}
</style>