<template>
  <div class="post-detail-container">
    <div class="back-nav">
      <el-button @click="$router.push('/forum')" type="info" plain round>
        <el-icon><ArrowLeft /></el-icon> 返回广场
      </el-button>
    </div>
    
    <el-card v-if="post" class="detail-card">
      <template #header>
        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-meta">
          <el-avatar :size="24" :src="post.authorAvatar" />  
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

      <el-divider content-position="left">驴友评论 ({{ commentList.length }})</el-divider>

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
          <el-button type="primary" @click="submitComment">发表评论</el-button>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="item in commentList" :key="item.id" class="comment-item">
          <el-avatar :size="32" :src="item.avatar" />
          <div class="comment-main">
            <div class="comment-header">
              <span class="comment-user">{{ item.nickname }}</span>
              <span class="comment-date">{{ formatDate(item.createTime) }}</span>
            </div>
            <p class="comment-text">{{ item.content }}</p>
          </div>
        </div>
        
        <el-empty v-if="commentList.length === 0" description="暂无评论，快来抢沙发吧！" :image-size="100" />
      </div>
    </el-card>

    <el-empty v-else description="正在加载帖子内容..." />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const post = ref(null)
const commentList = ref([])      // 存储评论列表
const newCommentContent = ref('') // 存储新输入的评论

// 从本地存储获取当前登录的用户信息
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

// 加载帖子详情和评论列表
const loadData = async () => {
  const id = route.params.id
  try {
    // A. 获取帖子详情
    const postRes = await request.get(`/forum/${id}`)
    if (postRes.data.success) {
      post.value = postRes.data.data
    }

    // B. 获取该帖子的评论列表
    const commentRes = await request.get(`/comment/list/${id}`)
    if (commentRes.data.success) {
      commentList.value = commentRes.data.data
    }
  } catch (error) {
    console.error("加载详情数据失败：", error)
  }
}

// 提交评论逻辑
const submitComment = async () => {
  // 1. 检查是否登录
  if (!userInfo.id) {
    return ElMessage.warning('请先登录后再发表评论')
  }
  // 2. 检查内容是否为空
  if (!newCommentContent.value.trim()) {
    return ElMessage.warning('评论内容不能为空')
  }

  try {
    const res = await request.post('/comment/add', {
      postId: route.params.id,
      userId: userInfo.id,
      content: newCommentContent.value
    })

    if (res.data.success) {
      ElMessage.success('评论发表成功！')
      newCommentContent.value = '' // 清空输入框
      loadData() // 刷新页面数据以看到新评论
    } else {
      ElMessage.error(res.data.message || '发表失败')
    }
  } catch (error) {
    ElMessage.error('服务器响应超时，请稍后再试')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

onMounted(loadData)
</script>

<style scoped>
.post-detail-container { padding: 40px; background: #f8fafc; min-height: 100vh; }
.back-nav { margin-bottom: 20px; }
.detail-card { max-width: 900px; margin: 0 auto; border-radius: 15px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }

/* 帖子样式 */
.post-title { font-size: 28px; color: #1e293b; margin: 0; }
.post-meta { display: flex; align-items: center; gap: 12px; margin-top: 15px; color: #64748b; font-size: 14px; }
.post-body { padding: 20px 0; }
.text-content { font-size: 16px; line-height: 1.8; color: #334155; white-space: pre-wrap; margin-bottom: 20px; }
.post-image-box { text-align: center; margin-top: 20px; }
.post-image { max-width: 100%; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }

/* 评论输入样式 */
.comment-input-section { margin: 20px 0 30px; }
.submit-btn-box { text-align: right; margin-top: 10px; }

/* 评论列表样式 */
.comment-item { display: flex; gap: 15px; padding: 15px 0; border-bottom: 1px solid #f1f5f9; }
.comment-main { flex: 1; }
.comment-header { display: flex; justify-content: space-between; margin-bottom: 5px; }
.comment-user { font-weight: bold; font-size: 14px; color: #1e293b; }
.comment-date { font-size: 12px; color: #94a3b8; }
.comment-text { font-size: 14px; color: #475569; line-height: 1.5; margin: 0; }
</style>