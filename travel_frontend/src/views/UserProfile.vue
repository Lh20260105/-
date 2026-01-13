<template>
  <div class="profile-page">
    <el-card class="modern-profile-card" shadow="never">
      <div class="profile-banner">
        <div class="banner-mask"></div>
      </div>

      <div class="profile-header">
        <div class="avatar-upload-section">
          <el-upload
            class="avatar-uploader-modern"
            action="/api/files/upload" 
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="userForm.avatarUrl || userForm.avatar" :src="userForm.avatarUrl || userForm.avatar" class="avatar-img" />
            <div v-else class="default-avatar">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="upload-overlay">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
        </div>
        
        <h2 class="display-name">你好，{{ userForm.nickname || userForm.username }}</h2>
        <div class="role-pill">
          <el-tag :type="userForm.role === 'ADMIN' ? 'danger' : 'success'" round effect="dark">
             {{ userForm.role === 'ADMIN' ? '系统管理员' : '普通旅行者' }}
          </el-tag>
        </div>
      </div>

      <el-divider class="custom-divider">
        <span class="divider-text">基本信息设置</span>
      </el-divider>

      <div class="form-container">
        <el-form :model="userForm" label-position="top" class="custom-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="登录账号 (不可修改)">
                <el-input v-model="userForm.username" disabled class="glass-input" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="用户昵称">
                <el-input v-model="userForm.nickname" placeholder="想个好听的名字" class="glass-input" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="注册时间">
            <el-input v-model="userForm.createTime" disabled class="glass-input" />
          </el-form-item>

          <div class="profile-actions">
            <el-button type="primary" class="btn-save-modern" @click="handleUpdate" :loading="loading">
               保存资料修改
            </el-button>
            <el-button class="btn-home-ghost" @click="$router.push('/home')">
               返回首页
            </el-button>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { UserFilled, Camera } from '@element-plus/icons-vue'
// 1. 【核心新增】：导入全局同步方法
import { updateGlobalUser } from '../utils/userBus'

const loading = ref(false)
const userForm = ref({
  id: '',
  username: '',
  nickname: '',
  role: '',
  avatarUrl: '', // 对应后端字段名
  createTime: ''
})

// 初始化：从缓存获取
const loadUserInfo = () => {
  const infoStr = sessionStorage.getItem('user_info')
  if (infoStr) {
    userForm.value = JSON.parse(infoStr)
  }
}

const beforeAvatarUpload = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  if (!isImage) ElMessage.error('只能上传图片格式!')
  return isImage
}

/**
 * 上传成功后的回调
 */
const handleAvatarSuccess = (response) => {
  if (response.success) {
    // 1. 获取后端返回的带后缀的新地址
    const newAvatarUrl = response.data 
    
    // 2. 更新当前表单显示的图片
    userForm.value.avatarUrl = newAvatarUrl
    
    // 3. 【核心修复】：通知全局 userBus 更新，同步右上角头像
    // 这一步会自动更新 sessionStorage，不再需要手动 JSON.stringify
    updateGlobalUser(userForm.value) 
    
    ElMessage.success('头像预览已更新，保存资料后生效')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

/**
 * 提交资料修改
 */
const handleUpdate = async () => {
  loading.value = true
  try {
    // 这里的接口请确保后端是 /api/users/update 或 /api/user/update
    const res = await request.post('/user/update', userForm.value)
    if (res.data.success) {
      ElMessage.success('资料已同步更新！')
      
      // 【核心修复】：同步全局状态，让导航栏名字立刻变化
      updateGlobalUser(userForm.value) 
    }
  } catch (error) {
    ElMessage.error('保存失败，请检查后端接口')
  } finally {
    loading.value = false
  }
}

onMounted(loadUserInfo)
</script>

<style scoped>
/* 保持你原本漂亮的样式不变 */
.profile-page { min-height: 100vh; padding: 60px 20px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); display: flex; justify-content: center; align-items: flex-start; }
.modern-profile-card { width: 100%; max-width: 650px; border-radius: 24px; border: none; overflow: hidden; box-shadow: 0 20px 50px rgba(0,0,0,0.1) !important; }
.profile-banner { height: 160px; background: url('https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800') center/cover; position: relative; }
.banner-mask { position: absolute; width: 100%; height: 100%; background: linear-gradient(to bottom, transparent, white); }
.profile-header { text-align: center; margin-top: -60px; position: relative; z-index: 10; }
.avatar-uploader-modern { width: 120px; height: 120px; margin: 0 auto; border-radius: 50%; border: 5px solid white; box-shadow: 0 5px 15px rgba(0,0,0,0.1); overflow: hidden; background: #f8fafc; cursor: pointer; position: relative; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.default-avatar { font-size: 50px; color: #cbd5e1; line-height: 110px; }
.upload-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); color: white; display: flex; flex-direction: column; justify-content: center; align-items: center; opacity: 0; transition: 0.3s; font-size: 13px; }
.avatar-uploader-modern:hover .upload-overlay { opacity: 1; }
.display-name { margin: 15px 0 5px; font-size: 26px; font-weight: 800; color: #1e293b; }
.role-pill { margin-bottom: 20px; }
.custom-divider { margin: 40px 0; }
.divider-text { color: #94a3b8; font-size: 13px; font-weight: normal; }
.form-container { padding: 0 40px 40px; }
.glass-input :deep(.el-input__wrapper) { border-radius: 10px; background: #f8fafc !important; box-shadow: none !important; border: 1px solid #e2e8f0; }
.profile-actions { margin-top: 40px; display: flex; flex-direction: column; gap: 15px; }
.btn-save-modern { height: 50px; border-radius: 12px; font-size: 16px; font-weight: bold; background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); border: none; box-shadow: 0 10px 20px rgba(79,172,254,0.3); }
.btn-save-modern:hover { transform: translateY(-2px); box-shadow: 0 15px 25px rgba(79,172,254,0.4); }
.btn-home-ghost { border: none; color: #94a3b8; }
</style>