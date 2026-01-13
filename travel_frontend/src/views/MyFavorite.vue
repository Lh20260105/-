<template>
  <div class="favorite-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="title">❤️ 我的心动收藏</h2>
        <p class="subtitle">汇集您的每一个旅行梦想，随时准备出发</p>
      </div>
      <el-button type="primary" plain icon="Refresh" @click="loadFavorites">刷新列表</el-button>
    </div>

    <el-row :gutter="25">
      <el-col v-for="item in favorites" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="fav-card" :body-style="{ padding: '0px' }" shadow="hover">
          <div class="image-box" @click="$router.push('/attraction/' + item.id)">
            <el-image :src="item.imageUrl" fit="cover" class="fav-img">
              <template #error>
                <div class="image-slot"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="category-tag">{{ item.category }}</div>
            <div class="price-tag">￥{{ item.ticketPrice }}</div>
          </div>
          
          <div class="card-content">
            <h3 class="attr-name" @click="$router.push('/attraction/' + item.id)">{{ item.name }}</h3>
            <p class="loc-info"><el-icon><Location /></el-icon> {{ item.location }}</p>
            
            <div class="card-actions">
              <el-button type="danger" link @click="removeFavorite(item.id)">
                <el-icon><Delete /></el-icon> 移除
              </el-button>
              
              <div class="right-btns">
                <el-button type="primary" size="small" plain @click="$router.push('/attraction/' + item.id)">
                  详情
                </el-button>
                <el-button type="success" size="small" @click="openItineraryDialog(item)">
                  加入行程
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="favorites.length === 0" description="收藏夹还是空的，快去首页发现美景吧！">
      <el-button type="primary" @click="$router.push('/home')">去逛逛</el-button>
    </el-empty>

    <el-dialog v-model="itineraryDialogVisible" title="🗓️ 规划到我的行程" width="400px">
      <el-form :model="itineraryForm" label-position="top">
        <el-form-item label="景点名称">
          <el-alert :title="selectedAttraction.name" type="info" :closable="false" show-icon />
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
        <el-button type="primary" @click="submitToItinerary" round>确认加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const favorites = ref([])
// --- 【关键修改点】统一使用 sessionStorage ---
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

// 行程弹窗控制
const itineraryDialogVisible = ref(false)
const selectedAttraction = ref({})
const itineraryForm = ref({ startTime: '09:00:00', dayNumber: 1 })

// 1. 加载收藏列表
const loadFavorites = async () => {
  if (!userInfo.id) return
  const res = await request.get(`/favorites/list/${userInfo.id}`)
  if (res.data.success) {
    // 适配后端 Result 包装类
    favorites.value = res.data.data || []
  }
}

// 2. 移除收藏
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

// 3. 打开加入行程弹窗
const openItineraryDialog = (attr) => {
  selectedAttraction.value = attr
  itineraryDialogVisible.value = true
}

// 4. 提交到行程单
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
.favorite-container { padding: 40px; max-width: 1200px; margin: 0 auto; background-color: #f8f9fa; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 40px; }
.title { font-size: 28px; color: #2c3e50; margin: 0; font-weight: bold; }
.subtitle { color: #7f8c8d; margin-top: 8px; }

.fav-card { border-radius: 16px; overflow: hidden; border: none; transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); margin-bottom: 25px; background: white; }
.fav-card:hover { transform: translateY(-10px); box-shadow: 0 15px 30px rgba(0,0,0,0.1); }

.image-box { height: 200px; position: relative; cursor: pointer; overflow: hidden; }
.fav-img { width: 100%; height: 100%; transition: transform 0.5s; }
.fav-card:hover .fav-img { transform: scale(1.1); }

.image-slot { display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399; }

.category-tag { position: absolute; top: 12px; left: 12px; background: rgba(0,0,0,0.5); backdrop-filter: blur(4px); color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; }
.price-tag { position: absolute; bottom: 12px; right: 12px; background: #f56c6c; color: white; padding: 4px 10px; border-radius: 6px; font-weight: bold; font-size: 14px; }

.card-content { padding: 20px; }
.attr-name { margin: 0 0 8px; cursor: pointer; color: #2c3e50; font-size: 18px; transition: color 0.2s; }
.attr-name:hover { color: #409EFF; }
.loc-info { font-size: 13px; color: #95a5a6; margin-bottom: 18px; display: flex; align-items: center; gap: 4px; }

.card-actions { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f1f2f6; padding-top: 15px; }
.right-btns { display: flex; gap: 8px; }
</style>