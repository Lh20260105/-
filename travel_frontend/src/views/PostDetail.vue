<template>
  <div class="post-detail-page">
    <div class="noise-overlay"></div>
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-container">
      <div class="back-nav">
        <el-button class="back-btn" @click="$router.push('/forum')">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回广场</span>
        </el-button>
      </div>
      
      <el-card v-if="post" class="detail-card">
        <template #header>
          <h2 class="post-title">{{ post.title }}</h2>
          <div class="post-meta">
            <el-avatar :size="32" :src="post.authorAvatar || post.authorAvatarUrl" class="author-avatar">
              <el-icon :size="16"><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ post.authorName || '匿名驴友' }}</span>
            <span class="date">发布于：{{ formatDate(post.createTime) }}</span>
          </div>
        </template>
        
        <div class="post-body">
          <p class="text-content">{{ post.content }}</p>
          <div v-if="post.imageUrl || post.image_url" class="post-image-box">
            <img :src="post.imageUrl || post.image_url" alt="封面" class="post-image">
          </div>
        </div>

        <div class="comment-section">
          <div class="section-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>驴友评论 ({{ commentList.length }})</span>
          </div>

          <div class="comment-input-section">
            <el-input
              v-model="newCommentContent"
              type="textarea"
              :rows="3"
              placeholder="说点什么吧，你的评论是对驴友最大的支持..."
              maxlength="200"
              show-word-limit
            />
            <div class="submit-btn-box">
              <el-button type="primary" class="submit-comment-btn" @click="submitComment">发表评论</el-button>
            </div>
          </div>

          <div class="comment-list">
            <div v-for="item in commentList" :key="item.id" class="comment-item">
              <el-avatar 
                :size="40" 
                :src="item.avatarUrl || item.avatar" 
                class="comment-avatar"
              >
                <el-icon :size="20"><UserFilled /></el-icon>
              </el-avatar>
              <div class="comment-main">
                <div class="comment-header">
                  <span class="comment-user">{{ item.nickname || '匿名用户' }}</span>
                  <span class="comment-date">{{ formatDate(item.createTime) }}</span>
                  <el-button 
                    v-if="item.userId === userInfo.id" 
                    type="danger" 
                    size="small" 
                    text 
                    @click="deleteComment(item.id)"
                    class="delete-btn"
                  >
                    删除
                  </el-button>
                </div>
                <p class="comment-text">{{ item.content }}</p>
              </div>
            </div>
            
            <el-empty v-if="commentList.length === 0" description="暂无评论，快来抢沙发吧！" :image-size="80" />
          </div>
        </div>
      </el-card>

      <el-empty v-else description="正在加载帖子内容..." />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotRound, UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const post = ref(null)
const commentList = ref([])
const newCommentContent = ref('')

const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

const loadData = async () => {
  const id = route.params.id
  try {
    const postRes = await request.get(`/forum/${id}`)
    if (postRes.data.success) {
      post.value = postRes.data.data
    }

    const commentRes = await request.get(`/comment/list/${id}`)
    if (commentRes.data.success) {
      commentList.value = commentRes.data.data
      console.log('评论列表:', commentList.value)
    }
  } catch (error) {
    console.error("加载详情数据失败：", error)
  }
}

const submitComment = async () => {
  // 重新获取最新的用户信息（确保头像是最新的）
  const currentUserInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  
  if (!currentUserInfo.id) {
    return ElMessage.warning('请先登录后再发表评论')
  }
  if (!newCommentContent.value.trim()) {
    return ElMessage.warning('评论内容不能为空')
  }

  try {
    const res = await request.post('/comment/add', {
      postId: route.params.id,
      userId: currentUserInfo.id,
      content: newCommentContent.value
    })

    if (res.data.success) {
      ElMessage.success('评论发表成功！')
      newCommentContent.value = ''
      loadData()
    } else {
      ElMessage.error(res.data.message || '发表失败')
    }
  } catch (error) {
    ElMessage.error('服务器响应超时，请稍后再试')
  }
}

const deleteComment = async (commentId) => {
  try {
    const res = await request.delete(`/comment/delete/${commentId}`)
    if (res.data.success) {
      ElMessage.success('评论已删除')
      loadData()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

onMounted(loadData)
</script>

<style scoped>
.post-detail-page {
  position: relative;
  min-height: 100vh;
  padding: 24px;
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
  max-width: 900px;
  margin: 0 auto;
}

.back-nav {
  margin-bottom: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(231, 111, 81, 0.2);
  color: #264653;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.back-btn:hover {
  background: rgba(231, 111, 81, 0.1);
  border-color: #E76F51;
  color: #E76F51;
}

.detail-card {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border: none;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.detail-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 20px 24px;
}

.post-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #264653;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #5a6c7d;
  font-size: 0.9rem;
}

.username {
  font-weight: 500;
  color: #264653;
}

.date {
  color: #94a3b8;
}

.post-body {
  padding: 24px;
}

.text-content {
  font-size: 1rem;
  line-height: 1.8;
  color: #5a6c7d;
  white-space: pre-wrap;
  margin-bottom: 24px;
}

.post-image-box {
  text-align: center;
  margin-top: 20px;
}

.post-image {
  max-width: 100%;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.comment-section {
  padding: 0 24px 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #264653;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.section-header .el-icon {
  color: #E76F51;
}

.comment-input-section {
  margin-bottom: 24px;
}

.submit-btn-box {
  text-align: right;
  margin-top: 12px;
}

.submit-comment-btn {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border: none;
  font-weight: 500;
}

.submit-comment-btn:hover {
  background: linear-gradient(135deg, #d65d3f, #e8914a);
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.comment-main {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.comment-user {
  font-weight: 600;
  font-size: 0.95rem;
  color: #264653;
}

.comment-date {
  font-size: 0.8rem;
  color: #94a3b8;
  flex: 1;
}

.delete-btn {
  font-size: 0.75rem;
  padding: 2px 8px;
}

.comment-text {
  font-size: 0.9rem;
  color: #5a6c7d;
  line-height: 1.6;
  margin: 0;
}

@media (max-width: 768px) {
  .post-detail-page {
    padding: 16px;
  }
  
  .post-title {
    font-size: 1.4rem;
  }
  
  .post-body,
  .comment-section {
    padding: 16px;
  }
}
</style>
