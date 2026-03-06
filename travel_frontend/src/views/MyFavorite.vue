<template>
  <div class="favorite-page">
    <div class="noise-overlay"></div>
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-container">
      <div class="page-header">
        <div class="header-content">
          <div class="icon-circle">
            <el-icon size="24"><Star /></el-icon>
          </div>
          <div class="text-content">
            <h2 class="title">我的心动收藏</h2>
            <p class="subtitle">汇集您的每一个旅行梦想，随时准备出发</p>
          </div>
        </div>
        <el-button class="refresh-btn" @click="loadFavorites">
          <el-icon><Refresh /></el-icon>
          <span>刷新列表</span>
        </el-button>
      </div>

      <div class="cards-grid">
        <el-card v-for="item in favorites" :key="item.id" class="fav-card" shadow="hover">
          <div class="image-box" @click="$router.push('/attraction/' + item.id)">
            <el-image :src="item.imageUrl || `https://picsum.photos/seed/${item.id}/400/300`" fit="cover" class="fav-img">
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="category-tag">{{ item.category }}</div>
            <div class="price-tag">￥{{ item.ticketPrice }}</div>
          </div>
          
          <div class="card-content">
            <h3 class="attr-name" @click="$router.push('/attraction/' + item.id)">{{ item.name }}</h3>
            <p class="loc-info">
              <el-icon><Location /></el-icon>
              <span>{{ item.location }}</span>
            </p>
            
            <div class="card-actions">
              <el-button link class="remove-btn" @click="removeFavorite(item.id)">
                <el-icon><Delete /></el-icon>
                <span>移除</span>
              </el-button>
              
              <div class="action-btns">
                <el-button size="small" class="detail-btn" @click="$router.push('/attraction/' + item.id)">
                  详情
                </el-button>
                <el-button size="small" class="add-btn" @click="openItineraryDialog(item)">
                  加入行程
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <el-empty v-if="favorites.length === 0" description="收藏夹还是空的，快去首页发现美景吧！">
        <el-button type="primary" @click="$router.push('/home')">去逛逛</el-button>
      </el-empty>
    </div>

    <el-dialog v-model="itineraryDialogVisible" title="规划到我的行程" width="420px" class="itinerary-dialog">
      <el-form :model="itineraryForm" label-position="top" class="itinerary-form">
        <el-form-item label="景点名称">
          <div class="attraction-name-display">{{ selectedAttraction.name }}</div>
        </el-form-item>
        <el-form-item label="计划第几天去">
          <el-input-number v-model="itineraryForm.dayNumber" :min="1" :max="7" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划出发时间">
          <el-time-picker v-model="itineraryForm.startTime" value-format="HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itineraryDialogVisible = false">取消</el-button>
        <el-button type="primary" class="confirm-btn" @click="submitToItinerary">确认加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, Refresh, Picture, Location, Delete } from '@element-plus/icons-vue'

const favorites = ref([])
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

const itineraryDialogVisible = ref(false)
const selectedAttraction = ref({})
const itineraryForm = ref({ startTime: '09:00:00', dayNumber: 1 })

const loadFavorites = async () => {
  if (!userInfo.id) return
  const res = await request.get(`/favorites/list/${userInfo.id}`)
  if (res.data.success) {
    favorites.value = res.data.data || []
  }
}

const removeFavorite = (attractionId) => {
  ElMessageBox.confirm('确定要从收藏夹移除该景点吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await request.delete('/favorites/remove', {
      params: { userId: userInfo.id, attractionId: attractionId }
    })
    if (res.data.success) {
      ElMessage.success('已移除')
      loadFavorites() 
    }
  }).catch(() => {})
}

const openItineraryDialog = (attr) => {
  selectedAttraction.value = attr
  itineraryDialogVisible.value = true
}

const submitToItinerary = async () => {
  const res = await request.post('/itinerary/add-item', {
    userId: userInfo.id,
    attractionId: selectedAttraction.value.id,
    dayNumber: itineraryForm.value.dayNumber,
    startTime: itineraryForm.value.startTime
  })
  if (res.data.success) {
    ElMessage.success('成功加入行程单！')
    itineraryDialogVisible.value = false
  }
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorite-page {
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
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-circle {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #264653;
  margin: 0;
}

.subtitle {
  color: #5a6c7d;
  margin: 4px 0 0 0;
  font-size: 0.9rem;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(231, 111, 81, 0.2);
  color: #E76F51;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.refresh-btn:hover {
  background: rgba(231, 111, 81, 0.1);
  border-color: #E76F51;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.fav-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.fav-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.image-box {
  height: 200px;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

.fav-img {
  width: 100%;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.fav-card:hover .fav-img {
  transform: scale(1.08);
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f5f5, #ebebeb);
  color: #999;
}

.image-slot .el-icon {
  font-size: 40px;
}

.category-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  color: #264653;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.price-tag {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: linear-gradient(135deg, #E76F51, #F4A261);
  color: white;
  padding: 6px 12px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.9rem;
}

.card-content {
  padding: 16px;
}

.attr-name {
  margin: 0 0 8px;
  cursor: pointer;
  color: #264653;
  font-size: 1.1rem;
  font-weight: 600;
  transition: color 0.3s;
}

.attr-name:hover {
  color: #E76F51;
}

.loc-info {
  font-size: 0.85rem;
  color: #5a6c7d;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding-top: 12px;
}

.remove-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 0.85rem;
}

.remove-btn:hover {
  color: #ef4444;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.detail-btn {
  border-radius: 8px;
}

.add-btn {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border: none;
  color: white;
  border-radius: 8px;
}

.add-btn:hover {
  background: linear-gradient(135deg, #d65d3f, #e8914a);
  color: white;
}

.attraction-name-display {
  background: rgba(231, 111, 81, 0.08);
  padding: 12px 16px;
  border-radius: 10px;
  font-weight: 600;
  color: #E76F51;
}

.itinerary-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.itinerary-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #264653;
}

.confirm-btn {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border: none;
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #d65d3f, #e8914a);
}

@media (max-width: 768px) {
  .favorite-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>
