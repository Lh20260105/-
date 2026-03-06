<template>
  <div class="detail-page">
    <div class="noise-overlay"></div>
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="detail-container">
      <el-page-header @back="$router.back()" class="back-header">
        <template #content>
          <span class="back-text">返回列表</span>
        </template>
      </el-page-header>
      
      <div class="detail-content" v-if="attraction.id">
        <el-row :gutter="32">
          <el-col :md="12">
            <div class="image-wrapper">
              <el-image :src="attraction.imageUrl || attraction.image_url" class="main-image" fit="cover">
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                    <span>暂无图片</span>
                  </div>
                </template>
              </el-image>
            </div>
          </el-col>
          
          <el-col :md="12">
            <div class="info-panel">
              <h1 class="attr-name">{{ attraction.name }}</h1>
              
              <div class="attr-tags">
                <el-tag class="category-tag">{{ attraction.category }}</el-tag>
                <el-tag class="price-tag">
                  <span class="price-currency">￥</span>
                  <span class="price-value">{{ attraction.ticketPrice }}</span>
                  <span class="price-unit">/人</span>
                </el-tag>
              </div>
              
              <div class="info-list">
                <div class="info-item">
                  <div class="info-icon">
                    <el-icon><Location /></el-icon>
                  </div>
                  <div class="info-content">
                    <span class="info-label">详细地址</span>
                    <span class="info-value">{{ attraction.location }}</span>
                  </div>
                </div>
                <div class="info-item">
                  <div class="info-icon">
                    <el-icon><Timer /></el-icon>
                  </div>
                  <div class="info-content">
                    <span class="info-label">开放时间</span>
                    <span class="info-value">{{ attraction.openTime }}</span>
                  </div>
                </div>
              </div>
              
              <div class="action-buttons">
                <el-button type="primary" size="large" class="action-btn primary-btn" @click="itineraryDialogVisible = true">
                  <el-icon><Calendar /></el-icon>
                  <span>加入行程计划</span>
                </el-button>
                <el-button 
                  size="large" 
                  class="action-btn collect-btn" 
                  :class="{ 'collected': isCollected }"
                  @click="handleCollect"
                >
                  <el-icon><Star /></el-icon>
                  <span>{{ isCollected ? '已收藏' : '收藏该景点' }}</span>
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <div class="description-section">
          <div class="section-header">
            <el-icon><Document /></el-icon>
            <span>景点深度介绍</span>
          </div>
          <div class="description-box">
            {{ attraction.description || '该景点暂无详细描述，期待您的实地探索！' }}
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="itineraryDialogVisible" title="规划我的行程" width="420px" class="itinerary-dialog">
      <el-form :model="itineraryForm" label-position="top" class="itinerary-form">
        <el-form-item label="计划第几天去">
          <el-input-number v-model="itineraryForm.dayNumber" :min="1" :max="7" />
        </el-form-item>
        <el-form-item label="计划到达时间">
          <el-time-picker v-model="itineraryForm.startTime" value-format="HH:mm:ss" style="width: 100%"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itineraryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitToItinerary">确认加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Location, Timer, Calendar, Star, Document, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const attraction = ref({})
const itineraryDialogVisible = ref(false)
const itineraryForm = ref({ startTime: '10:00:00', dayNumber: 1 })
const isCollected = ref(false)

const getUserInfo = () => JSON.parse(sessionStorage.getItem('user_info') || '{}')

const loadDetail = async () => {
  const id = route.params.id
  const res = await request.get(`/attractions/${id}`)
  if (res.data.success) {
    attraction.value = res.data.data
  }
  
  const userInfo = getUserInfo()
  if (userInfo.id) {
    const favoriteRes = await request.get('/favorites/ids/' + userInfo.id)
    if (favoriteRes.data.success) {
      const favoriteIds = favoriteRes.data.data || []
      isCollected.value = favoriteIds.includes(Number(id))
    }
  }
}

const handleCollect = async () => {
  const userInfo = getUserInfo()
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (isCollected.value) {
    await request.delete('/favorites/remove', { params: { userId: userInfo.id, attractionId: attraction.value.id } })
    ElMessage.success('已取消收藏')
    isCollected.value = false
  } else {
    await request.post('/favorites/add', { userId: userInfo.id, attractionId: attraction.value.id })
    ElMessage.success('已加入收藏夹')
    isCollected.value = true
  }
}

const submitToItinerary = async () => {
  const userInfo = getUserInfo()
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }
  const res = await request.post('/itinerary/add-item', {
    userId: userInfo.id,
    attractionId: attraction.value.id,
    dayNumber: itineraryForm.value.dayNumber,
    startTime: itineraryForm.value.startTime
  })
  if (res.data.success) {
    ElMessage.success('已加入行程单！')
    itineraryDialogVisible.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
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
  50% { transform: translate(40px, -30px) scale(1.1); }
}

.detail-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.back-header {
  margin-bottom: 24px;
}

.back-text {
  font-size: 1rem;
  color: #5a6c7d;
  font-weight: 500;
}

.detail-content {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.image-wrapper {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.main-image {
  width: 100%;
  height: 400px;
  display: block;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  color: #94a3b8;
}

.image-slot .el-icon {
  font-size: 48px;
}

.info-panel {
  padding: 16px 0;
}

.attr-name {
  font-size: 2rem;
  font-weight: 700;
  color: #264653;
  margin: 0 0 20px 0;
  letter-spacing: -0.5px;
}

.attr-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.category-tag {
  background: rgba(42, 157, 143, 0.1);
  color: #2A9D8F;
  border: none;
  font-weight: 500;
}

.price-tag {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 1rem;
}

.price-currency {
  font-size: 0.9rem;
}

.price-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.price-unit {
  font-size: 0.85rem;
  opacity: 0.9;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.info-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(231, 111, 81, 0.1);
  color: #E76F51;
  border-radius: 10px;
  font-size: 18px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 0.85rem;
  color: #5a6c7d;
}

.info-value {
  font-size: 1rem;
  color: #264653;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 14px 28px;
  border-radius: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.primary-btn {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border: none;
  color: white;
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(231, 111, 81, 0.3);
}

.collect-btn {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(231, 111, 81, 0.3);
  color: #E76F51;
}

.collect-btn:hover {
  background: rgba(231, 111, 81, 0.1);
  border-color: #E76F51;
}

.collect-btn.collected {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border: none;
  color: white;
}

.collect-btn.collected:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(231, 111, 81, 0.3);
}

.description-section {
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 600;
  color: #264653;
  margin-bottom: 20px;
}

.section-header .el-icon {
  color: #E76F51;
}

.description-box {
  line-height: 1.8;
  color: #5a6c7d;
  font-size: 1rem;
  white-space: pre-line;
}

.itinerary-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.itinerary-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #264653;
}

@media (max-width: 768px) {
  .detail-page {
    padding: 16px;
  }
  
  .detail-content {
    padding: 20px;
  }
  
  .attr-name {
    font-size: 1.5rem;
  }
  
  .main-image {
    height: 250px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
