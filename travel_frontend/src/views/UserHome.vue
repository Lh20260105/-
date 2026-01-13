<template>
  <div class="user-home">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="animate-up">下一站，去哪儿？</h1>
        <p class="animate-up-delay">为您甄选精选景点，开启定制行程</p>
        
        <div class="search-container">
          <el-input 
            v-model="searchQuery.name" 
            placeholder="搜索目的地 / 景点名称" 
            class="main-search" 
            clearable 
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="filter-group">
          <el-check-tag 
            v-for="cat in ['全部', '自然风光', '人文古迹', '美食品尝']" 
            :key="cat"
            :checked="searchQuery.category === (cat === '全部' ? '' : cat)" 
            @change="handleCategoryChange(cat)" 
            class="tag-item"
          >
            {{ cat }}
          </el-check-tag>
        </div>
      </div>
    </div>

    <div class="content-container">
      
      <div v-if="recommendList.length > 0 && !searchQuery.name" class="recommend-section">
        <div class="section-title">
          <div class="title-with-tag">
            <h2>✨ 猜你喜欢</h2>
            <el-tag type="danger" effect="dark" round size="small">个性化推荐</el-tag>
          </div>
          <p class="recommend-tip">{{ recommendMessage }}</p>
        </div>

        <el-row :gutter="25">
          <el-col v-for="item in recommendList" :key="'rec-'+item.id" :xs="24" :sm="12" :md="8" :lg="6">
            <el-card class="sight-card recommend-card" :body-style="{ padding: '0px' }">
              <div class="image-wrapper" @click="$router.push('/attraction/' + item.id)">
                <img :src="item.imageUrl || 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500'" />
                <div class="category-badge">{{ item.category }}</div>
                <div class="price-tag">￥{{ item.ticketPrice }}</div>
              </div>
              <div class="card-body">
                <div class="card-top">
                  <h3 @click="$router.push('/attraction/' + item.id)">{{ item.name }}</h3>
                </div>
                <div class="card-footer">
                   <el-button type="primary" size="small" link @click="$router.push('/attraction/' + item.id)">查看详情</el-button>
                   <span class="recommend-tag">匹配度 99%</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-divider><el-icon><Compass /></el-icon></el-divider>
      </div>

      <div class="section-title">
        <h2>{{ searchQuery.name ? '🔍 搜索结果' : '🔥 热门景点推荐' }}</h2>
        <div class="title-line"></div>
      </div>

      <el-row :gutter="25">
        <el-col v-for="item in attractionList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="sight-card" :body-style="{ padding: '0px' }">
            <div class="image-wrapper" @click="$router.push('/attraction/' + item.id)">
              <img :src="item.imageUrl || 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500'" />
              <div class="category-badge">{{ item.category }}</div>
              <div class="price-tag">￥{{ item.ticketPrice }}</div>
            </div>
            
            <div class="card-body">
              <div class="card-top">
                <h3 @click="$router.push('/attraction/' + item.id)" v-html="highlightText(item.name)"></h3>
                <span class="loc-text"><el-icon><Location /></el-icon>{{ item.location }}</span>
              </div>
              <p class="card-desc" v-html="highlightText(item.description || '暂无描述信息，期待您的探索。')"></p>
              
              <div class="card-footer">
                <span class="open-time">⏰ {{ item.openTime }}</span>
                <div class="actions">
                  <el-button 
                    size="small" 
                    @click="handleToggleFavorite(item)"
                    :type="item.isFavorite ? 'warning' : 'default'"
                    :plain="!item.isFavorite"
                  >
                    <el-icon>
                      <StarFilled v-if="item.isFavorite" />
                      <Star v-else />
                    </el-icon>
                    <span style="margin-left: 4px">{{ item.isFavorite ? '已收藏' : '收藏' }}</span>
                  </el-button>

                  <el-button type="primary" size="small" round @click="openItineraryDialog(item)">
                    加入行程
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-empty v-if="attractionList.length === 0" description="未找到相关景点，换个词试试？" />
    </div>

    <el-dialog v-model="itineraryDialogVisible" title="🗓️ 规划我的行程" width="400px">
      <el-form :model="itineraryForm" label-position="top">
        <el-form-item label="您选择的景点">
          <el-alert :title="selectedAttraction.name" type="info" :closable="false" show-icon />
        </el-form-item>
        <el-form-item label="计划第几天去 (Day 1-7)">
           <el-input-number v-model="itineraryForm.dayNumber" :min="1" :max="7" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="计划到达时间">
          <el-time-picker v-model="itineraryForm.startTime" value-format="HH:mm:ss" style="width: 100%"/>
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
import { ElMessage } from 'element-plus'
// --- 【图标导入加固】 ---
import { Search, Location, Star, StarFilled, Compass } from '@element-plus/icons-vue'

const attractionList = ref([])
const recommendList = ref([])
const recommendMessage = ref('')
const searchQuery = ref({ name: '', category: '' })
const itineraryDialogVisible = ref(false)
const selectedAttraction = ref({})
const itineraryForm = ref({ startTime: '10:00:00', dayNumber: 1 })

// --- 【核心修改点】改用 sessionStorage 确保与 App.vue 对齐 ---
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

/**
 * 关键词高亮逻辑
 */
const highlightText = (text) => {
  const keyword = searchQuery.value.name;
  if (!keyword || !text) return text; 
  const reg = new RegExp(keyword, 'gi');
  return text.replace(reg, `<span class="highlight">${keyword}</span>`);
}

/**
 * 加载首页数据
 */
const loadData = async () => {
  // 1. 加载常规景点列表
  const res = await request.get('/attractions/search', { params: searchQuery.value })
  if (res.data.success) {
    let list = res.data.data
    // 如果登录了，获取当前用户的收藏 ID 列表来渲染红星
    if (userInfo.id) {
      try {
        const favRes = await request.get(`/favorites/ids/${userInfo.id}`)
        const favIds = favRes.data.data || []
        list = list.map(item => ({ ...item, isFavorite: favIds.includes(item.id) }))
      } catch (e) { console.error("加载收藏状态失败", e) }
    }
    attractionList.value = list
  }

  // 2. 加载个性化推荐 (仅限登录用户且未搜索时)
  if (userInfo.id && !searchQuery.value.name && !searchQuery.value.category) {
    try {
      const recRes = await request.get(`/recommend/personal/${userInfo.id}`)
      if (recRes.data.success) {
        recommendList.value = recRes.data.data
        recommendMessage.value = recRes.data.message
      }
    } catch (e) { console.error("加载推荐失败", e) }
  }
}

/**
 * 收藏/取消收藏
 */
const handleToggleFavorite = async (item) => {
  if (!userInfo.id) return ElMessage.warning('请先登录')
  if (item.isFavorite) {
    const res = await request.delete('/favorites/remove', {
      params: { userId: userInfo.id, attractionId: item.id }
    })
    if (res.data.success) { ElMessage.success('已取消收藏'); item.isFavorite = false; }
  } else {
    const res = await request.post('/favorites/add', { userId: userInfo.id, attractionId: item.id })
    if (res.data.success) { ElMessage.success('收藏成功'); item.isFavorite = true; }
  }
}

const handleSearch = () => loadData()

const handleCategoryChange = (cat) => {
  searchQuery.value.category = cat === '全部' ? '' : cat
  loadData()
}

const openItineraryDialog = (attr) => {
  selectedAttraction.value = attr
  itineraryDialogVisible.value = true
}

/**
 * 加入行程单 (关键：使用 session 中的用户信息)
 */
const submitToItinerary = async () => {
  if (!userInfo.id) return ElMessage.warning('请先登录')
  
  try {
    const res = await request.post('/itinerary/add-item', {
      userId: userInfo.id,
      attractionId: selectedAttraction.value.id,
      dayNumber: itineraryForm.value.dayNumber,
      startTime: itineraryForm.value.startTime
    })
    if (res.data.success) {
      ElMessage.success('安排成功！')
      itineraryDialogVisible.value = false
    }
  } catch (e) {
    ElMessage.error('服务响应超时，请检查后端运行状态')
  }
}

onMounted(loadData)
</script>

<style scoped>
/* 高亮样式 */
:deep(.highlight) {
  color: #f56c6c;
  font-weight: bold;
  background-color: rgba(245, 108, 108, 0.1);
  padding: 0 2px;
  border-radius: 2px;
}

/* 布局样式 */
.hero-section { 
  height: 450px; 
  background: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.3)), url('https://images.unsplash.com/photo-1488085061387-422e29b40080?w=1600'); 
  background-size: cover; 
  background-position: center; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  color: white; 
  text-align: center; 
}
.hero-content h1 { font-size: 3.5rem; margin: 0; text-shadow: 2px 2px 10px rgba(0,0,0,0.5); }
.hero-content p { font-size: 1.2rem; opacity: 0.9; margin-top: 10px; }
.search-container { width: 600px; margin: 30px auto; }

.main-search :deep(.el-input__wrapper) { border-radius: 30px 0 0 30px !important; padding-left: 20px; font-size: 16px; height: 50px; }
.main-search :deep(.el-input-group__append) { border-radius: 0 30px 30px 0 !important; background-color: #409EFF !important; color: white; border: none !important; padding: 0 25px; cursor: pointer; }

.tag-item { margin: 0 8px; cursor: pointer; border-radius: 15px; padding: 5px 15px; background: rgba(255,255,255,0.2) !important; color: white !important; border: 1px solid rgba(255,255,255,0.3) !important; }
.tag-item.is-checked { background: #409EFF !important; color: #fff !important; }

.content-container { max-width: 1200px; margin: 40px auto 50px; padding: 0 20px; }

.recommend-section { margin-bottom: 50px; }
.title-with-tag { display: flex; align-items: center; gap: 15px; }
.recommend-tip { font-size: 14px; color: #909399; margin: 5px 0 20px; }
.recommend-card { border: 1px solid #fde2e2; background-color: #fffafa; }
.recommend-tag { font-size: 12px; color: #f56c6c; font-style: italic; }

.section-title { margin-bottom: 30px; }
.title-line { width: 50px; height: 4px; background: #409EFF; border-radius: 2px; }
.sight-card { border-radius: 15px; overflow: hidden; transition: all 0.3s; border: none; margin-bottom: 25px; cursor: pointer;}
.sight-card:hover { transform: translateY(-10px); box-shadow: 0 15px 30px rgba(0,0,0,0.1); }
.image-wrapper { position: relative; height: 220px; overflow: hidden; }
.image-wrapper img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.category-badge { position: absolute; top: 15px; left: 15px; background: rgba(0,0,0,0.6); color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; }
.price-tag { position: absolute; bottom: 15px; right: 15px; background: #f56c6c; color: white; padding: 5px 15px; border-radius: 8px; font-weight: bold; }
.card-body { padding: 20px; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.card-top h3 { margin: 0; font-size: 1.25rem; color: #2c3e50; }
.loc-text { font-size: 13px; color: #909399; }
.card-desc { font-size: 14px; color: #606266; line-height: 1.6; height: 44px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; margin-bottom: 15px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 15px; border-top: 1px solid #f2f2f2; }
.open-time { font-size: 12px; color: #999; }

.animate-up { animation: fadeInUp 0.8s ease-out; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
</style>