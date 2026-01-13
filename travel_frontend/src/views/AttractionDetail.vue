<template>
  <div class="detail-container">
    <el-page-header @back="$router.back()" title="返回列表" />
    
    <div class="detail-content" v-if="attraction.id">
      <el-row :gutter="40">
        <el-col :md="12">
          <el-image :src="attraction.imageUrl" class="main-image" fit="cover">
            <template #error><div class="image-slot">暂无图片</div></template>
          </el-image>
        </el-col>
        
        <el-col :md="12">
          <h1 class="attr-name">{{ attraction.name }}</h1>
          <div class="attr-tags">
            <el-tag type="success">{{ attraction.category }}</el-tag>
            <el-tag type="warning" class="price-tag">￥{{ attraction.ticketPrice }} / 人</el-tag>
          </div>
          
          <div class="info-list">
            <p><el-icon><Location /></el-icon> <strong>详细地址：</strong>{{ attraction.location }}</p>
            <p><el-icon><Timer /></el-icon> <strong>开放时间：</strong>{{ attraction.openTime }}</p>
          </div>
          
          <div class="action-buttons">
            <el-button type="primary" size="large" round @click="itineraryDialogVisible = true">
              <el-icon><Calendar /></el-icon> 加入行程计划
            </el-button>
            <el-button type="warning" plain size="large" round @click="handleCollect">
              <el-icon><Star /></el-icon> 收藏该景点
            </el-button>
          </div>
        </el-col>
      </el-row>

      <el-divider content-position="left">景点深度介绍</el-divider>
      <div class="description-box">
        {{ attraction.description || '该景点暂无详细描述，期待您的实地探索！' }}
      </div>
    </div>

    <el-dialog v-model="itineraryDialogVisible" title="🗓️ 规划我的行程" width="400px">
      <el-form :model="itineraryForm" label-position="top">
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

const route = useRoute()
const attraction = ref({})
const itineraryDialogVisible = ref(false)
const itineraryForm = ref({ startTime: '10:00:00', dayNumber: 1 })

// 获取当前登录用户
const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')

const loadDetail = async () => {
  const id = route.params.id
  const res = await request.get(`/attractions/${id}`)
  if (res.data.success) {
    attraction.value = res.data.data
  }
}

// 收藏逻辑
const handleCollect = async () => {
  if (!userInfo.id) return ElMessage.warning('请先登录')
  const res = await request.post('/favorites/add', { 
    userId: userInfo.id, 
    attractionId: attraction.value.id 
  })
  if (res.data.success) ElMessage.success('已加入收藏夹')
  else ElMessage.info(res.data.message)
}

// 加入行程逻辑 (与首页一致)
const submitToItinerary = async () => {
  if (!userInfo.id) return ElMessage.warning('请先登录')
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
.detail-container { padding: 40px; max-width: 1200px; margin: 0 auto; }
.detail-content { margin-top: 30px; background: white; padding: 40px; border-radius: 15px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }
.main-image { width: 100%; height: 450px; border-radius: 12px; }
.attr-name { font-size: 36px; color: #303133; margin-bottom: 20px; }
.attr-tags { margin-bottom: 25px; display: flex; gap: 15px; align-items: center; }
.price-tag { font-size: 18px; font-weight: bold; }
.info-list p { margin: 15px 0; font-size: 16px; color: #606266; display: flex; align-items: center; gap: 8px; }
.action-buttons { margin-top: 50px; display: flex; gap: 20px; }
.description-box { line-height: 2; color: #444; font-size: 16px; white-space: pre-line; }
</style>