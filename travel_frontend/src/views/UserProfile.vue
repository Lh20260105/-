<template>
  <div class="profile-page">
    <div class="noise-overlay"></div>
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-container">
      <el-card class="modern-profile-card" shadow="never">
        <div class="profile-banner">
          <div class="banner-mask"></div>
        </div>

        <div class="profile-header">
          <div class="avatar-upload-section">
            <el-upload
              ref="uploadRef"
              class="avatar-uploader-modern"
              action="/api/files/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleUploadError"
              :on-progress="handleUploadProgress"
              :before-upload="beforeAvatarUpload"
              :http-request="customUpload"
              accept="image/*"
            >
              <img v-if="(userForm.avatarUrl || userForm.avatar) && !isUploading" :src="userForm.avatarUrl || userForm.avatar" class="avatar-img" />
              <div v-else-if="isUploading" class="avatar-uploading">
                <el-progress 
                  type="circle" 
                  :percentage="uploadProgress" 
                  :status="uploadProgress >= 90 ? 'success' : ''"
                  :stroke-width="4"
                  :width="80"
                />
                <span class="upload-text">{{ uploadProgress >= 90 ? '处理中...' : `${uploadProgress}%` }}</span>
              </div>
              <div v-else class="default-avatar">
                <el-icon><UserFilled /></el-icon>
              </div>
              <div v-if="!isUploading" class="upload-overlay">
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

        <div class="section-divider">
          <span class="divider-text">基本信息设置</span>
        </div>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { UserFilled, Camera } from '@element-plus/icons-vue'
import { updateGlobalUser } from '../utils/userBus'

const loading = ref(false)
const uploadProgress = ref(0)
const isUploading = ref(false)
const uploadRef = ref(null)
const userForm = ref({
  id: '',
  username: '',
  nickname: '',
  role: '',
  avatarUrl: '',
  createTime: ''
})

const loadUserInfo = () => {
  const infoStr = sessionStorage.getItem('user_info')
  if (infoStr) {
    const user = JSON.parse(infoStr)
    // 兼容 avatar 和 avatarUrl 字段
    userForm.value = {
      ...user,
      avatarUrl: user.avatarUrl || user.avatar || ''
    }
  }
}

const beforeAvatarUpload = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片格式!')
    return false
  }
  isUploading.value = true
  uploadProgress.value = 0
  return true
}

const handleUploadProgress = (event) => {
  uploadProgress.value = Math.round(event.percent)
}

const handleAvatarSuccess = (response) => {
  isUploading.value = false
  uploadProgress.value = 0
  if (response.success) {
    const newAvatarUrl = response.data 
    userForm.value.avatarUrl = newAvatarUrl
    updateGlobalUser(userForm.value) 
    ElMessage.success('头像预览已更新，保存资料后生效')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = (error) => {
  isUploading.value = false
  uploadProgress.value = 0
  console.error('上传错误:', error)
  let errorMsg = '上传失败'
  if (error.status === 413) {
    errorMsg = '文件太大，请联系管理员'
  } else if (error.status === 0) {
    errorMsg = '网络错误，请检查连接'
  } else if (error.message) {
    errorMsg = error.message
  }
  ElMessage.error(errorMsg)
}

const customUpload = async (options) => {
  const { file, onProgress, onSuccess, onError } = options
  
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userForm.value.id)
  
  try {
    const response = await request.post('/users/upload-avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress({ percent })
      }
    })
    
    if (response.data.success) {
      onSuccess(response.data)
    } else {
      onError(new Error(response.data.message))
    }
  } catch (error) {
    onError(error)
  }
}

const handleUpdate = async () => {
  loading.value = true
  try {
    const res = await request.post('/users/update', userForm.value)
    if (res.data.success) {
      ElMessage.success('资料已同步更新！')
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
.profile-page {
  position: relative;
  min-height: 100vh;
  padding: 60px 20px;
}

.noise-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0.025;
  z-index: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

.gradient-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: #E76F51;
  top: -200px;
  right: -100px;
  animation: blobMove1 20s ease-in-out infinite;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: #F4A261;
  bottom: -150px;
  left: -100px;
  animation: blobMove2 25s ease-in-out infinite;
}

@keyframes blobMove1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 40px) scale(1.05); }
}

@keyframes blobMove2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, -30px) scale(0.95); }
}

.page-container {
  position: relative;
  z-index: 1;
  max-width: 650px;
  margin: 0 auto;
}

.modern-profile-card {
  border-radius: 24px;
  border: none;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1) !important;
}

.profile-banner {
  height: 160px;
  background: url('https://images.pexels.com/photos/1464822759023-fed622ff2c3b?w=800') center/cover;
  position: relative;
}

.banner-mask {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, transparent, rgba(255, 255, 255, 0.9));
}

.profile-header {
  text-align: center;
  margin-top: -60px;
  position: relative;
  z-index: 10;
}

.avatar-uploader-modern {
  width: 120px;
  height: 120px;
  margin: 0 auto;
  border-radius: 50%;
  border: 5px solid white;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  background: #f8fafc;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader-modern :deep(.el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.default-avatar {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50px;
  color: #cbd5e1;
}

.avatar-uploading {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  gap: 4px;
}

.upload-text {
  font-size: 11px;
  color: #5a6c7d;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  font-size: 13px;
  gap: 4px;
}

.avatar-uploader-modern:hover .upload-overlay {
  opacity: 1;
}

.display-name {
  margin: 15px 0 5px;
  font-size: 1.75rem;
  font-weight: 700;
  color: #264653;
}

.role-pill {
  margin-bottom: 20px;
}

.section-divider {
  margin: 32px 40px 24px;
  text-align: center;
  position: relative;
}

.section-divider::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
}

.divider-text {
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  padding: 0 20px;
  color: #5a6c7d;
  font-size: 0.875rem;
}

.form-container {
  padding: 0 40px 40px;
}

.glass-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.03) !important;
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.glass-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(231, 111, 81, 0.3);
}

.glass-input :deep(.el-input__wrapper.is-disabled) {
  background: rgba(0, 0, 0, 0.02) !important;
}

.profile-actions {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn-save-modern {
  height: 50px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border: none;
  box-shadow: 0 8px 20px rgba(231, 111, 81, 0.3);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.btn-save-modern:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(231, 111, 81, 0.4);
  background: linear-gradient(135deg, #d65d3f, #e8914a);
}

.btn-home-ghost {
  border: none;
  color: #5a6c7d;
  background: transparent;
}

.btn-home-ghost:hover {
  color: #E76F51;
}

@media (max-width: 768px) {
  .profile-page {
    padding: 24px 16px;
  }
  
  .form-container {
    padding: 0 20px 24px;
  }
  
  .section-divider {
    margin: 24px 20px 20px;
  }
}
</style>
