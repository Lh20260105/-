<template>
  <div class="user-home">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="animate-up">下一站，去哪儿？</h1>
        <p class="animate-up-delay">为您甄选全球景点，开启专属定制行程</p>
        
        <div class="search-container">
          <el-input 
            v-model="searchQuery.name" 
            placeholder="搜索目的地 / 景点名称" 
            class="main-search-modern" 
            clearable 
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button class="search-btn" @click="handleSearch">
                <el-icon><Search /></el-icon> <span>探索发现</span>
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
            class="modern-tag"
          >
            {{ cat }}
          </el-check-tag>
          
          <el-button class="community-shortcut" round @click="$router.push('/forum')">
            <el-icon><Compass /></el-icon> 进入驴友广场
          </el-button>
        </div>
      </div>
    </div>

    <div class="content-container">
      
      <div v-if="recommendList.length > 0 && !searchQuery.name" class="recommend-section">
        <div class="section-title">
          <div class="title-with-tag">
            <h2>✨ 猜你喜欢</h2>
            <el-tag type="danger" effect="dark" round size="small">AI 甄选</el-tag>
          </div>
          <p class="recommend-tip">{{ recommendMessage }}</p>
        </div>

        <el-row :gutter="25">
          <el-col v-for="item in recommendList" :key="'rec-'+item.id" :xs="24" :sm="12" :md="8" :lg="6">
            <el-card class="modern-sight-card rec-card" :body-style="{ padding: '0px' }">
              <div class="image-wrapper" @click="$router.push('/attraction/' + item.id)">
                <img :src="item.imageUrl || 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500'" />
                <div class="category-badge">{{ item.category }}</div>
                <div class="price-tag">￥{{ item.ticketPrice }}</div>
              </div>
              <div class="card-body">
                <h3>{{ item.name }}</h3>
                <div class="card-footer">
                   <el-button type="primary" size="small" link @click="$router.push('/attraction/' + item.id)">查看详情</el-button>
                   <span class="match-rate">匹配度 99%</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-divider><el-icon><Compass /></el-icon></el-divider>
      </div>

      <div class="section-title">
        <h2>{{ searchQuery.name ? '🔍 发现结果' : '🔥 热门推荐' }}</h2>
        <div class="title-underline"></div>
      </div>

      <el-row :gutter="25">
        <el-col v-for="item in attractionList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="modern-sight-card" :body-style="{ padding: '0px' }">
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
              <p class="card-desc" v-html="highlightText(item.description || '暂无描述信息')"></p>
              
              <div class="card-footer">
                <span class="open-time">⏰ {{ item.openTime }}</span>
                <div class="actions">
                  <el-button 
                    class="btn-icon-fav"
                    :class="{ 'is-active': item.isFavorite }"
                    @click.stop="handleToggleFavorite(item)"
                  >
                    <el-icon><StarFilled v-if="item.isFavorite" /><Star v-else /></el-icon>
                  </el-button>

                  <el-button type="primary" class="btn-add" round @click.stop="openItineraryDialog(item)">
                    加入行程
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div v-if="recentPosts.length > 0" class="forum-preview-section">
        <el-divider content-position="left">💬 驴友们正在聊</el-divider>
        <el-row :gutter="20">
          <el-col v-for="post in recentPosts.slice(0, 3)" :key="post.id" :span="8">
            <div class="mini-post-card" @click="$router.push('/forum')">
              <div class="mini-post-content">
                <span class="post-title">{{ post.title }}</span>
                <span class="post-author">@{{ post.author }}</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-empty v-if="attractionList.length === 0" description="未找到相关景点，换个词试试？" />
    </div>

    <el-dialog v-model="itineraryDialogVisible" title="🗓️ 规划我的行程" width="400px">
      <el-form :model="itineraryForm" label-position="top">
        <el-form-item label="所选景点">
          <div class="dialog-attr-name">{{ selectedAttraction.name }}</div>
        </el-form-item>
        <el-form-item label="计划天数">
          <el-input-number v-model="itineraryForm.dayNumber" :min="1" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="到达时间">
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
import { Search, Location, Star, StarFilled, Compass } from '@element-plus/icons-vue'

const attractionList = ref([])
const recommendList = ref([])
const recentPosts = ref([]) // 用于存放论坛预览帖子
const recommendMessage = ref('')
const searchQuery = ref({ name: '', category: '' })
const itineraryDialogVisible = ref(false)
const selectedAttraction = ref({})
const itineraryForm = ref({ startTime: '10:00:00', dayNumber: 1 })

const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

const highlightText = (text) => {
  const keyword = searchQuery.value.name;
  if (!keyword || !text) return text; 
  const reg = new RegExp(keyword, 'gi');
  return text.replace(reg, `<span class="highlight">${keyword}</span>`);
}

/**
 * 加载基础数据
 */
const loadData = async () => {
  const res = await request.get('/attractions/search', { params: searchQuery.value })
  if (res.data.success) {
    let list = res.data.data
    if (userInfo.id) {
      try {
        const favRes = await request.get(`/favorites/ids/${userInfo.id}`)
        const favIds = favRes.data.data || []
        list = list.map(item => ({ ...item, isFavorite: favIds.includes(item.id) }))
      } catch (e) { console.error(e) }
    }
    attractionList.value = list
  }
  
  // 加载推荐
  if (userInfo.id && !searchQuery.value.name && !searchQuery.value.category) {
    try {
      const recRes = await request.get(`/recommend/personal/${userInfo.id}`)
      if (recRes.data.success) {
        recommendList.value = recRes.data.data
        recommendMessage.value = recRes.data.message
      }
    } catch (e) { console.error(e) }
  }

  // 加载广场动态预览
  try {
    const forumRes = await request.get('/forum/list')
    if (forumRes.data.success) {
      recentPosts.value = forumRes.data.data || []
    }
  } catch (e) { console.warn("论坛数据暂未同步") }
}

const handleToggleFavorite = async (item) => {
  if (!userInfo.id) return ElMessage.warning('请先登录')
  if (item.isFavorite) {
    const res = await request.delete('/favorites/remove', { params: { userId: userInfo.id, attractionId: item.id } })
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
const submitToItinerary = async () => {
  if (!userInfo.id) return ElMessage.warning('请先登录')
  const res = await request.post('/itinerary/add-item', {
    userId: userInfo.id,
    attractionId: selectedAttraction.value.id,
    dayNumber: itineraryForm.value.dayNumber,
    startTime: itineraryForm.value.startTime
  })
  if (res.data.success) { ElMessage.success('安排成功！'); itineraryDialogVisible.value = false; }
}
onMounted(loadData)
</script>

<style scoped>
/* 1. 高亮关键字 */
:deep(.highlight) { color: #ff4757; font-weight: bold; }

/* 2. Hero Section 修正 */
.hero-section {
  height: 480px;
  background: linear-gradient(rgba(0,0,0,0.4), rgba(0,0,0,0.4)), url('https://images.unsplash.com/photo-1488085061387-422e29b40080?w=1600');
  background-size: cover;
  background-position: center;
  display: flex; align-items: center; justify-content: center; color: white; text-align: center;
}
.hero-content h1 { font-size: 3.2rem; font-weight: 800; margin: 0; }
.search-container { width: 600px; margin: 30px auto; }

/* 3. 搜索栏：毛玻璃效果 */
.main-search-modern :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.15) !important;
  backdrop-filter: blur(12px);
  border-radius: 50px 0 0 50px;
  border: none !important;
  box-shadow: none !important;
  height: 50px; padding-left: 25px;
}
.main-search-modern :deep(.el-input__inner) { color: white !important; }
.main-search-modern :deep(.el-input-group__append) {
  background: #409EFF !important; border: none; border-radius: 0 50px 50px 0;
}
.search-btn { color: white !important; font-weight: bold; }

.filter-group { display: flex; align-items: center; justify-content: center; gap: 10px; }
.modern-tag { border-radius: 50px; background: rgba(255,255,255,0.1) !important; color: white !important; cursor: pointer; }
.modern-tag.is-checked { background: #409EFF !important; }

/* 新增：社区快捷入口按钮样式 */
.community-shortcut {
  background: rgba(255,255,255,0.2) !important;
  border: 1px solid rgba(255,255,255,0.4) !important;
  color: white !important;
  backdrop-filter: blur(5px);
  margin-left: 20px;
}
.community-shortcut:hover { background: #409EFF !important; border-color: #409EFF !important; }

/* 4. 景点卡片：定位修复 */
.content-container { max-width: 1200px; margin: 40px auto; padding: 0 20px; }
.modern-sight-card {
  border-radius: 20px; border: none; overflow: hidden; transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05); margin-bottom: 25px;
}
.modern-sight-card:hover { transform: translateY(-10px); box-shadow: 0 15px 30px rgba(0,0,0,0.1); }

.image-wrapper { position: relative; height: 230px; }
.image-wrapper img { width: 100%; height: 100%; object-fit: cover; }

.category-badge {
  position: absolute; top: 15px; left: 15px;
  background: rgba(0,0,0,0.5); color: white; padding: 4px 12px; border-radius: 50px; font-size: 11px;
}
.price-tag {
  position: absolute; bottom: 15px; right: 15px;
  background: #ff4757; color: white; padding: 6px 15px; border-radius: 10px; font-weight: 800;
}

/* 5. 社区动态预览样式 */
.forum-preview-section { margin-top: 40px; }
.mini-post-card {
  background: #fff;
  padding: 15px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.2s;
}
.mini-post-card:hover { background: #f0f7ff; }
.post-title { display: block; font-weight: bold; color: #2d3436; margin-bottom: 5px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.post-author { font-size: 12px; color: #95a5a6; }

/* 6. 基础辅助样式 */
.card-body { padding: 20px; }
.card-top { display: flex; justify-content: space-between; align-items: center; }
.card-top h3 { font-size: 1.2rem; margin: 0; color: #2d3436; }
.loc-text { font-size: 12px; color: #95a5a6; }
.card-desc { font-size: 13px; color: #636e72; height: 40px; margin: 15px 0; line-height: 1.6; }
.card-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f1f2f6; padding-top: 15px; }
.btn-icon-fav { background: #f1f2f6 !important; border: none !important; color: #b2bec3 !important; width: 36px; height: 36px; border-radius: 10px; }
.btn-icon-fav.is-active { background: #fff2f2 !important; color: #ff4757 !important; }
.dialog-attr-name { background: #f0f7ff; color: #409EFF; padding: 10px; border-radius: 8px; font-weight: bold; }
</style>