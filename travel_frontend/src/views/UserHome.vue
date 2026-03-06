<template>
  <div class="user-home">
    <!-- 噪点纹理背景 -->
    <div class="noise-overlay"></div>
    
    <!-- 不规则渐变背景 -->
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <!-- Hero 轮播图区域 -->
    <div class="hero-carousel">
      <!-- 有轮播图数据时显示轮播 -->
      <el-carousel 
        v-if="carouselSlides.length > 0"
        :interval="5000" 
        :arrow="'hover'" 
        :indicator-position="'none'"
        height="420px"
        @change="handleCarouselChange"
      >
        <el-carousel-item v-for="(slide, index) in carouselSlides" :key="index">
          <div class="carousel-slide" :style="{ backgroundImage: `url(${slide.image})` }"></div>
        </el-carousel-item>
      </el-carousel>
      
      <!-- 无轮播图数据时显示默认背景 -->
      <div v-else class="carousel-slide default-slide">
        <div class="default-slide-content">
          <h2>探索精彩旅程</h2>
          <p>发现世界各地的美景与文化</p>
        </div>
      </div>
      
      <!-- 搜索浮层 -->
      <div class="search-layer">
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
        
        <!-- 轮播指示器 -->
        <div class="carousel-indicators">
          <span 
            v-for="(slide, index) in carouselSlides" 
            :key="index"
            class="indicator-dot"
            :class="{ active: currentSlide === index }"
          ></span>
        </div>
      </div>
    </div>

    <div class="content-container">
      
      <div v-if="recommendList.length > 0 && !searchQuery.name" class="recommend-section">
        <div class="section-title">
          <div class="title-with-tag">
            <h2>猜你喜欢</h2>
            <span class="ai-tag">AI 甄选</span>
          </div>
          <p class="recommend-tip">{{ recommendMessage }}</p>
        </div>

        <el-row :gutter="20">
          <el-col v-for="item in recommendList" :key="'rec-'+item.id" :xs="24" :sm="12" :md="8" :lg="6">
            <el-card class="modern-sight-card rec-card" :body-style="{ padding: '0px' }">
              <div class="image-wrapper" @click="$router.push('/attraction/' + item.id)">
                <img :src="item.imageUrl || getDefaultImage(item.id)" />
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
        <h2>{{ searchQuery.name ? '发现结果' : '热门推荐' }}</h2>
        <div class="title-underline"></div>
      </div>

      <el-row :gutter="20">
        <el-col v-for="item in attractionList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="modern-sight-card" :body-style="{ padding: '0px' }">
            <div class="image-wrapper" @click="$router.push('/attraction/' + item.id)">
              <img :src="item.imageUrl || getDefaultImage(item.id + 100)" />
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
                <span class="open-time">{{ item.openTime }}</span>
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
        <el-divider content-position="left">驴友们正在聊</el-divider>
        <el-row :gutter="16">
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

    <el-dialog v-model="itineraryDialogVisible" title="规划我的行程" width="400px" class="custom-dialog">
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
const recentPosts = ref([])
const recommendMessage = ref('')
const searchQuery = ref({ name: '', category: '' })
const itineraryDialogVisible = ref(false)
const selectedAttraction = ref({})
const itineraryForm = ref({ startTime: '10:00:00', dayNumber: 1 })
const currentSlide = ref(0)

const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

const carouselSlides = ref([])

const loadBanners = async () => {
  try {
    const res = await request.get('/banners')
    if (res.data.success && res.data.data.length > 0) {
      carouselSlides.value = res.data.data.map(banner => ({
        image: banner.imageUrl,
        title: banner.title || '',
        subtitle: '',
        linkUrl: banner.linkUrl
      }))
    } else {
      // 如果没有后台数据，显示空轮播图提示
      carouselSlides.value = []
    }
  } catch (e) {
    console.error('加载轮播图失败:', e)
    carouselSlides.value = []
  }
}

const handleCarouselChange = (index) => {
  currentSlide.value = index
}

const getDefaultImage = (id) => {
  return `https://picsum.photos/seed/travel${id}/400/300`
}

const highlightText = (text) => {
  const keyword = searchQuery.value.name;
  if (!keyword || !text) return text; 
  const reg = new RegExp(keyword, 'gi');
  return text.replace(reg, `<span class="highlight">${keyword}</span>`);
}

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
  
  if (userInfo.id && !searchQuery.value.name && !searchQuery.value.category) {
    try {
      const recRes = await request.get(`/recommend/personal/${userInfo.id}`)
      if (recRes.data.success) {
        recommendList.value = recRes.data.data
        recommendMessage.value = recRes.data.message
      }
    } catch (e) { console.error(e) }
  }

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
onMounted(() => {
  loadBanners()
  loadData()
})
</script>

<style scoped>
/* 基础设置 */
.user-home {
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
  width: 600px;
  height: 600px;
  background: #E76F51;
  top: -200px;
  left: -100px;
  animation: blobMove1 25s infinite;
}

.blob-2 {
  width: 500px;
  height: 500px;
  background: #F4A261;
  bottom: 20%;
  right: -100px;
  animation: blobMove2 30s infinite;
}

.blob-3 {
  width: 400px;
  height: 400px;
  background: #E76F51;
  top: 50%;
  left: 30%;
  animation: blobMove3 22s infinite;
}

@keyframes blobMove1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(50px, 30px) scale(1.1); }
}

@keyframes blobMove2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-40px, -30px) scale(1.05); }
}

@keyframes blobMove3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -40px) scale(0.95); }
}

/* 高亮关键字 */
:deep(.highlight) { color: #E76F51; font-weight: bold; }

/* Hero 轮播图区域 */
.hero-carousel {
  position: relative;
  z-index: 1;
  height: 420px;
  overflow: hidden;
  border-radius: 20px;
  margin: 20px;
}

.hero-carousel :deep(.el-carousel) {
  height: 100%;
}

.hero-carousel :deep(.el-carousel__container) {
  height: 100%;
}

.hero-carousel :deep(.el-carousel__arrow) {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  width: 48px;
  height: 48px;
  font-size: 16px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.hero-carousel :deep(.el-carousel__arrow:hover) {
  background: rgba(231, 111, 81, 0.8);
  border-color: #E76F51;
}

.carousel-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
}

.carousel-slide.default-slide {
  background: linear-gradient(135deg, #E76F51 0%, #F4A261 50%, #2A9D8F 100%);
}

.default-slide-content {
  text-align: center;
  color: white;
  z-index: 2;
}

.default-slide-content h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 12px 0;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.default-slide-content p {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.25) 0%,
    rgba(0, 0, 0, 0.4) 50%,
    rgba(0, 0, 0, 0.6) 100%
  );
}

.carousel-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  padding: 0 20px;
}

.slide-title {
  font-size: 3rem;
  font-weight: 800;
  margin: 0 0 12px 0;
  letter-spacing: -1px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-subtitle {
  font-size: 1.15rem;
  opacity: 0.9;
  margin: 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.1s both;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 搜索浮层 */
.search-layer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  padding: 30px 20px 40px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent);
}

.search-container {
  width: 560px;
  margin: 0 auto 20px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.search-container:hover {
  transform: scale(1.08);
}

/* 搜索栏：毛玻璃效果 */
.main-search-modern :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.18) !important;
  backdrop-filter: blur(12px);
  border-radius: 50px 0 0 50px !important;
  border: 1px solid rgba(255,255,255,0.2) !important;
  box-shadow: none !important;
  height: 48px;
  padding-left: 24px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.search-container:hover .main-search-modern :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.25) !important;
  border-color: rgba(255,255,255,0.4) !important;
  box-shadow: 0 8px 32px rgba(231, 111, 81, 0.3) !important;
}

.main-search-modern :deep(.el-input__inner) {
  color: white !important;
}

.main-search-modern :deep(.el-input__inner::placeholder) {
  color: rgba(255,255,255,0.7) !important;
}

.main-search-modern :deep(.el-input-group__append) {
  background: #E76F51 !important;
  border: none !important;
  border-radius: 0 50px 50px 0 !important;
}

.search-btn {
  color: white !important;
  font-weight: 600;
  height: 48px;
  padding: 0 24px;
}

.filter-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.modern-tag {
  border-radius: 50px;
  background: rgba(255,255,255,0.15) !important;
  color: rgba(255,255,255,0.9) !important;
  cursor: pointer;
  border: 1px solid rgba(255,255,255,0.2) !important;
  padding: 6px 16px;
  font-size: 13px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.modern-tag:hover {
  background: rgba(255,255,255,0.25) !important;
}

.modern-tag.is-checked {
  background: #E76F51 !important;
  border-color: #E76F51 !important;
}

.community-shortcut {
  background: rgba(42, 157, 143, 0.9) !important;
  border: 1px solid rgba(42, 157, 143, 0.5) !important;
  color: white !important;
  backdrop-filter: blur(5px);
  margin-left: 20px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.community-shortcut:hover {
  background: #2A9D8F !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(42, 157, 143, 0.35);
}

/* 轮播指示器 */
.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
}

.indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  cursor: pointer;
}

.indicator-dot.active {
  width: 24px;
  border-radius: 4px;
  background: #E76F51;
}

/* 内容区域 */
.content-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 24px;
}

/* 标题区域 */
.section-title {
  margin-bottom: 24px;
}

.section-title h2 {
  font-size: 1.6rem;
  font-weight: 700;
  color: #264653;
  margin: 0 0 8px 0;
}

.title-with-tag {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(231, 111, 81, 0.12);
  color: #E76F51;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
  border-radius: 20px;
  border: 1px solid rgba(231, 111, 81, 0.3);
}

.recommend-tip {
  font-size: 13px;
  color: #5a6c7d;
  margin: 0;
}

.title-underline {
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #E76F51, #F4A261);
  border-radius: 2px;
}

/* 景点卡片 */
.modern-sight-card {
  border-radius: 16px;
  border: none;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

.modern-sight-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.12);
}

.image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.modern-sight-card:hover .image-wrapper img {
  transform: scale(1.08);
}

.category-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(38, 70, 83, 0.85);
  color: white;
  padding: 4px 12px;
  border-radius: 50px;
  font-size: 11px;
  font-weight: 500;
}

.price-tag {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: #E76F51;
  color: white;
  padding: 5px 14px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 14px;
}

.card-body {
  padding: 16px;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.card-top h3 {
  font-size: 1.1rem;
  margin: 0;
  color: #264653;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.3s;
}

.card-top h3:hover {
  color: #E76F51;
}

.loc-text {
  font-size: 12px;
  color: #5a6c7d;
  white-space: nowrap;
  margin-left: 8px;
}

.card-desc {
  font-size: 13px;
  color: #5a6c7d;
  height: 38px;
  margin: 8px 0 12px 0;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(0,0,0,0.06);
  padding-top: 12px;
}

.open-time {
  font-size: 12px;
  color: #5a6c7d;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-icon-fav {
  background: #f0f0f0 !important;
  border: none !important;
  color: #999 !important;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.btn-icon-fav:hover {
  background: #fff2f2 !important;
  color: #E76F51 !important;
}

.btn-icon-fav.is-active {
  background: #fff2f2 !important;
  color: #E76F51 !important;
}

.btn-add {
  background: #2A9D8F !important;
  border: none !important;
  font-size: 12px;
  padding: 8px 16px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.btn-add:hover {
  background: #248277 !important;
  transform: translateY(-1px);
}

/* 社区动态预览 */
.forum-preview-section {
  margin-top: 36px;
}

.mini-post-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid rgba(255,255,255,0.5);
}

.mini-post-card:hover {
  background: #fff;
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.mini-post-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.post-title {
  display: block;
  font-weight: 600;
  color: #264653;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-author {
  font-size: 12px;
  color: #E76F51;
  font-weight: 500;
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

.dialog-attr-name {
  background: rgba(42, 157, 143, 0.1);
  color: #2A9D8F;
  padding: 12px 16px;
  border-radius: 10px;
  font-weight: 600;
  border: 1px solid rgba(42, 157, 143, 0.2);
}

/* 响应式 */
@media (max-width: 768px) {
  .slide-title {
    font-size: 2rem;
  }
  
  .search-container {
    width: 90%;
    padding: 0 16px;
  }
  
  .filter-group {
    flex-wrap: wrap;
  }
  
  .community-shortcut {
    margin-left: 0;
    margin-top: 10px;
  }
  
  .content-container {
    padding: 0 16px;
  }
}
</style>
