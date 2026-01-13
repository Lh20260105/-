<template>
  <div class="forum-container">
    <div class="forum-header">
      <h2>🌍 驴友广场</h2>
      <el-button type="primary" size="large" round @click="openPostDialog">
        <el-icon><EditPen /></el-icon> 分享我的行程
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col v-for="post in postList" :key="post.id" :span="8">
        <el-card class="post-card" shadow="hover" @click="goToDetail(post.id)">
          <div v-if="post.imageUrl || post.image_url" class="post-cover">
            <img :src="post.imageUrl || post.image_url" alt="封面" class="cover-img">
          </div>
          
          <div class="post-title">{{ post.title }}</div>
          <p class="post-excerpt">{{ post.content ? post.content.substring(0, 50) : '' }}...</p>
          
          <div class="post-meta">
            <div class="author-info">
              <el-avatar :size="24" :src="post.avatar" />
              <span>{{ post.author || '匿名驴友' }}</span>
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

    <el-dialog v-model="postDialogVisible" title="分享你的旅行感悟" width="500px" @close="resetForm">
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus' // 【新增】：导入 ElMessageBox
// 【新增导入】：Delete 图标
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

// 加载列表
const loadPosts = async () => {
  const res = await request.get('/forum/list')
  if (res.data.success) {
    postList.value = res.data.data
  }
}

// 【新增】：删除帖子的逻辑
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
    // 调用后端 @DeleteMapping("/{id}") 接口
    const res = await request.delete(`/forum/${id}`)
    if (res.data.success) {
      ElMessage.success('删除成功')
      loadPosts() // 刷新列表
    } else {
      ElMessage.error('删除失败：' + res.data.message)
    }
  }).catch(() => {
    // 用户取消了删除
  })
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
.forum-container { padding: 40px; background: #f5f7fa; min-height: 100vh; }
.forum-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }

.post-card { border-radius: 15px; margin-bottom: 20px; cursor: pointer; border: none; transition: transform 0.2s; overflow: hidden; position: relative; }
.post-card:hover { transform: translateY(-5px); }

.post-cover { width: 100%; height: 180px; overflow: hidden; margin-bottom: 10px; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }

.post-title { padding: 0 15px; font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px; }
.post-excerpt { padding: 0 15px; font-size: 14px; color: #64748b; line-height: 1.6; }

.post-meta { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding: 15px; border-top: 1px solid #f1f5f9; }
.author-info { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #475569; }

/* 【新增】：操作区域样式 */
.action-area { display: flex; align-items: center; gap: 10px; }
.post-date { font-size: 12px; color: #94a3b8; }
.del-btn { opacity: 0.6; transition: opacity 0.3s; }
.del-btn:hover { opacity: 1; }
</style>