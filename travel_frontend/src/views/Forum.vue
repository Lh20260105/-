<template>
  <div class="forum-page">
    <!-- 噪点纹理背景 -->
    <div class="noise-overlay"></div>
    
    <!-- 不规则渐变背景 -->
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="forum-container">
      <div class="forum-header">
        <div class="header-content">
          <h1>驴友广场</h1>
          <p>分享旅行故事，遇见志同道合的旅伴</p>
        </div>
        <el-button type="primary" size="large" round @click="openPostDialog" class="publish-btn">
          <el-icon><EditPen /></el-icon> 分享我的行程
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col v-for="post in postList" :key="post.id" :xs="24" :sm="12" :md="8">
          <el-card class="post-card" shadow="hover" @click="goToDetail(post.id)">
            <div v-if="post.imageUrl || post.image_url" class="post-cover">
              <img :src="post.imageUrl || post.image_url" alt="封面" class="cover-img">
            </div>
            
            <div class="post-title">{{ post.title }}</div>
            <p class="post-excerpt">{{ post.content ? post.content.substring(0, 60) : '' }}...</p>
            
            <div class="post-meta">
              <div class="author-info">
                <el-avatar :size="28" :src="post.avatar || `https://picsum.photos/100/100?random=${post.id}`" />
                <span class="author-name">{{ post.author || '匿名驴友' }}</span>
              </div>
              
              <div class="action-area">
                <span class="post-date">{{ formatDate(post.createTime) }}</span>
                <el-button 
                  type="danger" 
                  :icon="Delete" 
                  circle 
                  size="small" 
                  class="del-btn"
                  @click.stop="handleDelete(post.id)" 
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="postList.length === 0" description="还没有任何旅行分享，快来发布第一条吧！" />

      <el-dialog v-model="postDialogVisible" title="分享你的旅行感悟" width="500px" class="custom-dialog" @close="resetForm">
        <el-form :model="postForm" label-position="top">
          <el-form-item label="标题">
            <el-input v-model="postForm.title" placeholder="起一个吸引人的标题" />
          </el-form-item>
          
          <el-form-item label="上传图片 (可选)">
            <el-upload
              action="http://localhost:8080/api/files/upload"
              list-type="picture-card"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :limit="1"
              :file-list="fileList"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>

          <el-form-item label="心得体会">
            <el-input v-model="postForm.content" type="textarea" :rows="5" placeholder="分享这次旅行的故事..." />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="postDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPost">立即发布</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Plus, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const postList = ref([])
const postDialogVisible = ref(false)
const fileList = ref([])

const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

const postForm = ref({ 
  title: '', 
  content: '', 
  userId: userInfo.id,
  imageUrl: '' 
})

const loadPosts = async () => {
  const res = await request.get('/forum/list')
  if (res.data.success) {
    postList.value = res.data.data
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm(
    '确定要永久删除这条旅行分享吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    const res = await request.delete(`/forum/${id}`)
    if (res.data.success) {
      ElMessage.success('删除成功')
      loadPosts()
    } else {
      ElMessage.error('删除失败：' + res.data.message)
    }
  }).catch(() => {})
}

const goToDetail = (id) => {
  if (!id) return
  router.push(`/post/${id}`)
}

const openPostDialog = () => {
  if (!userInfo.id) return ElMessage.warning('请先登录')
  postDialogVisible.value = true
}

const handleUploadSuccess = (res) => {
  if (res.success) {
    postForm.value.imageUrl = res.data 
    ElMessage.success('图片上传成功')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const submitPost = async () => {
  if (!postForm.value.title || !postForm.value.content) {
    return ElMessage.warning('请填写完整内容')
  }
  const res = await request.post('/forum/add', postForm.value)
  if (res.data.success) {
    ElMessage.success('发布成功！')
    postDialogVisible.value = false
    resetForm()
    loadPosts()
  }
}

const resetForm = () => {
  postForm.value = { title: '', content: '', userId: userInfo.id, imageUrl: '' }
  fileList.value = []
}

const formatDate = (dateStr) => dateStr ? dateStr.substring(0, 10) : ''

onMounted(loadPosts)
</script>

<style scoped>
/* 基础设置 */
.forum-page {
  min-height: 100vh;
  position: relative;
  background: #FFF5F0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 噪点纹理 */
.noise-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1000;
  opacity: 0.04;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

/* 不规则渐变背景 */
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
  filter: blur(100px);
  opacity: 0.15;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: #F4A261;
  top: -150px;
  right: -100px;
  animation: blobMove1 28s infinite;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: #E76F51;
  bottom: 20%;
  left: -50px;
  animation: blobMove2 32s infinite;
}

@keyframes blobMove1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-40px, 30px) scale(1.1); }
}

@keyframes blobMove2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(50px, -30px) scale(1.05); }
}

/* 页面容器 */
.forum-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

/* 头部 */
.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding: 32px 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.6);
  box-shadow: 0 8px 32px rgba(0,0,0,0.06);
}

.header-content h1 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #264653;
  margin: 0 0 8px 0;
}

.header-content p {
  font-size: 14px;
  color: #5a6c7d;
  margin: 0;
}

.publish-btn {
  background: linear-gradient(135deg, #E76F51, #F4A261) !important;
  border: none !important;
  font-weight: 600;
  padding: 14px 28px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(231, 111, 81, 0.35);
}

/* 帖子卡片 */
.post-card {
  border-radius: 16px;
  margin-bottom: 20px;
  cursor: pointer;
  border: none;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

.post-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.12);
}

.post-cover {
  width: 100%;
  height: 180px;
  overflow: hidden;
  margin-bottom: 0;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.post-card:hover .cover-img {
  transform: scale(1.05);
}

.post-title {
  padding: 16px 20px 8px 20px;
  font-size: 16px;
  font-weight: 600;
  color: #264653;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-excerpt {
  padding: 0 20px;
  font-size: 13px;
  color: #5a6c7d;
  line-height: 1.6;
  margin: 0;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px 20px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-name {
  font-size: 13px;
  color: #264653;
  font-weight: 500;
}

.action-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-date {
  font-size: 12px;
  color: #94a3b8;
}

.del-btn {
  opacity: 0.5;
  transition: all 0.3s;
}

.del-btn:hover {
  opacity: 1;
}

/* 对话框样式 */
:deep(.custom-dialog) .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.custom-dialog) .el-dialog__header {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  padding: 20px 24px;
  margin: 0;
}

:deep(.custom-dialog) .el-dialog__title {
  color: white;
  font-weight: 600;
}

:deep(.custom-dialog) .el-dialog__headerbtn .el-dialog__close {
  color: white;
}

/* 响应式 */
@media (max-width: 768px) {
  .forum-header {
    flex-direction: column;
    text-align: center;
    gap: 20px;
    padding: 24px;
  }
  
  .forum-container {
    padding: 24px 16px;
  }
}
</style>
