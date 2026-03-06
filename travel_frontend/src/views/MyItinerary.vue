<template>
  <div class="itinerary-page">
    <div class="noise-overlay"></div>
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-container">
      <el-card class="main-header" shadow="never">
        <div class="header-content">
          <div class="title-group">
            <div class="icon-circle">
              <el-icon size="24"><Calendar /></el-icon>
            </div>
            <div class="text-info">
              <span class="main-title">我的个人行程单</span>
              <span class="sub-title">让每一步旅行都井然有序</span>
            </div>
          </div>
          <el-button v-if="itineraryItems.length > 0" class="btn-export-modern" @click="exportToPDF">
            <el-icon><Download /></el-icon>
            <span>导出 PDF 手账</span>
          </el-button>
        </div>
      </el-card>

      <div id="pdf-content" class="pdf-container">
        <div class="pdf-banner-modern">
          <div class="banner-overlay">
            <h1>{{ nickname || username }} 的旅行计划</h1>
            <div class="banner-meta">
              <span>📅 生成日期：{{ new Date().toLocaleDateString() }}</span>
              <span class="divider">|</span>
              <span>✨ 云游推荐系统</span>
            </div>
          </div>
        </div>

        <div v-for="day in sortedDays" :key="day" class="day-wrapper">
          <div class="day-sticky-header">
            <div class="day-pill">
              <span class="day-number">Day {{ day }}</span>
              <span class="day-date">{{ calculateDate(day) }}</span>
            </div>
            
            <div v-if="weatherData[day]" class="weather-pill">
              <el-icon><PartlyCloudy /></el-icon>
              <span>{{ weatherData[day].city }} · {{ weatherData[day].status }} · {{ weatherData[day].temp }}°C</span>
            </div>
            <div v-else class="weather-dot-loading">同步天气中...</div>
          </div>

          <el-timeline class="custom-timeline">
            <el-timeline-item
              v-for="item in getItemsByDay(day)"
              :key="item.id"
              :timestamp="item.startTime ? item.startTime.substring(0, 5) : '--:--'"
              placement="top"
              type="primary"
              class="timeline-node"
            >
              <el-card class="itinerary-item-card" shadow="hover">
                <div class="item-layout">
                  <div class="item-main">
                    <div class="title-row">
                      <h4 class="attraction-name">{{ item.attractionName }}</h4>
                      <el-tag size="small" effect="light" class="rounded-tag">
                        {{ item.stayDays || 1 }} 天行程
                      </el-tag>
                    </div>
                    <div class="detail-row">
                      <div class="location-box">
                        <el-icon><Location /></el-icon>
                        <span>{{ item.location }}</span>
                      </div>
                      <div class="time-range-box">
                        <span class="time-tag">入住: {{ calculateDate(item.dayNumber) }}</span>
                        <span class="arrow">→</span>
                        <span class="time-tag">离店: {{ calculateDeparture(item.dayNumber, item.stayDays) }}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="item-ops">
                    <el-button class="op-btn edit" circle @click="openEditDialog(item)">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                    <el-button class="op-btn delete" circle @click="handleDelete(item.id)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>

        <el-empty v-if="itineraryItems.length === 0" description="这里还没有故事，去首页开启你的旅程吧" />
        
        <div class="pdf-footer-modern" v-if="itineraryItems.length > 0">
          <div class="footer-line"></div>
          <p>祝您旅途愉快，一路平安</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="修改行程计划" width="440px" class="modern-dialog">
      <el-form label-position="top" class="modern-form">
        <el-form-item label="当前景点">
          <div class="display-name">{{ editForm.attractionName }}</div>
        </el-form-item>
        
        <el-form-item label="计划到达时间">
          <el-date-picker
            v-model="editForm.fullDateTime"
            type="datetime"
            placeholder="请选择"
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="计划停留时长">
          <el-input-number v-model="editForm.stayDays" :min="1" :max="30" class="wide-number" />
        </el-form-item>

        <transition name="el-fade-in">
          <div v-if="calculatedDeparturePreview" class="preview-box">
             <span class="preview-label">预计离开：</span>
             <span class="preview-date">{{ calculatedDeparturePreview }}</span>
          </div>
        </transition>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" class="btn-confirm" @click="submitEdit">保存更改</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import html2pdf from 'html2pdf.js'
import { Calendar, Location, Delete, Download, Edit, PartlyCloudy } from '@element-plus/icons-vue'

const itineraryItems = ref([]) 
const startDate = ref('') 
const weatherData = ref({}) 
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
const { username, nickname, id: userId } = userInfo

const editDialogVisible = ref(false)
const editForm = ref({ id: null, attractionName: '', fullDateTime: null, stayDays: 1 })

const loadData = async () => {
  if (!userId) return
  const res = await request.get(`/itinerary/user-items/${userId}`)
  if (res.data.success && res.data.data) {
    itineraryItems.value = res.data.data.items || []
    startDate.value = res.data.data.startDate || ''
    fetchWeatherForDays()
  }
}

const calculateDate = (dayNum) => {
  if (!startDate.value) return '日期待定'
  const date = new Date(startDate.value)
  date.setDate(date.getDate() + (dayNum - 1))
  return date.toLocaleDateString()
}

const calculateDeparture = (arrivalDay, stayDuration) => {
  if (!startDate.value) return ''
  const date = new Date(startDate.value)
  date.setDate(date.getDate() + (arrivalDay - 1) + (stayDuration || 1))
  return date.toLocaleDateString()
}

const fetchWeatherForDays = () => {
  const days = [...new Set(itineraryItems.value.map(i => i.dayNumber))]
  days.forEach(async (day) => {
    const item = itineraryItems.value.find(i => i.dayNumber === day)
    if (item && item.location) {
      const city = item.location.split(' ')[0].replace('市', '')
      try {
        const res = await request.get('/weather/info', { params: { city } })
        if (res.data.success) weatherData.value[day] = res.data.data
      } catch (e) { console.error("天气加载失败", e) }
    }
  })
}

const calculatedDeparturePreview = computed(() => {
  if (!editForm.value.fullDateTime) return null
  const departure = new Date(new Date(editForm.value.fullDateTime).getTime() + editForm.value.stayDays * 24 * 60 * 60 * 1000)
  return departure.toLocaleDateString()
})

const sortedDays = computed(() => [...new Set(itineraryItems.value.map(item => item.dayNumber))].sort((a, b) => a - b))
const getItemsByDay = (day) => itineraryItems.value.filter(item => item.dayNumber === day)

const openEditDialog = (item) => {
  editForm.value.id = item.id
  editForm.value.attractionName = item.attractionName
  editForm.value.stayDays = item.stayDays || 1
  const date = new Date(startDate.value)
  date.setDate(date.getDate() + (item.dayNumber - 1))
  if (item.startTime) {
    const [h, m] = item.startTime.split(':')
    date.setHours(h, m)
  }
  editForm.value.fullDateTime = date
  editDialogVisible.value = true
}

const submitEdit = async () => {
  const selected = new Date(editForm.value.fullDateTime)
  const start = new Date(startDate.value)
  if (selected < start) return ElMessage.error('时间超前了')
  const dayNumber = Math.floor((selected - start) / (1000 * 60 * 60 * 24)) + 1
  const startTime = `${String(selected.getHours()).padStart(2, '0')}:${String(selected.getMinutes()).padStart(2, '0')}:00`
  const res = await request.post('/itinerary/update-item', {
    id: editForm.value.id,
    dayNumber: dayNumber,
    startTime: startTime,
    stayDays: editForm.value.stayDays
  })
  if (res.data.success) {
    ElMessage.success('已同步修改')
    editDialogVisible.value = false
    loadData()
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要从计划中移除吗？', '提示').then(async () => {
    const res = await request.delete(`/itinerary/item/${id}`)
    if (res.data.success) { loadData() }
  })
}

const exportToPDF = () => {
  const element = document.getElementById('pdf-content')
  html2pdf().set({ 
    margin: 10, 
    filename: '我的旅行手账.pdf', 
    html2canvas: { scale: 3 },
    jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
  }).from(element).save()
}

onMounted(loadData)
</script>

<style scoped>
.itinerary-page {
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
  max-width: 1000px;
  margin: 0 auto;
}

.main-header {
  margin-bottom: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border: none;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06) !important;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

.icon-circle {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.main-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #264653;
  display: block;
}

.sub-title {
  font-size: 0.875rem;
  color: #5a6c7d;
}

.btn-export-modern {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.btn-export-modern:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(231, 111, 81, 0.3);
  color: white;
}

.pdf-container {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.pdf-banner-modern {
  height: 160px;
  background: linear-gradient(135deg, #E76F51 0%, #F4A261 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.banner-overlay h1 {
  font-size: 2rem;
  color: white;
  margin: 0;
  font-weight: 700;
}

.banner-meta {
  margin-top: 8px;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.9);
  display: flex;
  gap: 12px;
  justify-content: center;
}

.day-wrapper {
  padding: 0 32px 32px;
}

.day-sticky-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 24px 0 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.day-pill {
  background: #264653;
  color: white;
  padding: 8px 20px;
  border-radius: 50px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.day-number {
  font-weight: 700;
  font-size: 1rem;
}

.day-date {
  font-size: 0.875rem;
  opacity: 0.9;
}

.weather-pill {
  background: rgba(42, 157, 143, 0.1);
  color: #2A9D8F;
  padding: 6px 14px;
  border-radius: 50px;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid rgba(42, 157, 143, 0.2);
}

.custom-timeline :deep(.el-timeline-item__node) {
  background: #E76F51;
}

.custom-timeline :deep(.el-timeline-item__tail) {
  border-left: 2px solid rgba(231, 111, 81, 0.3);
}

.itinerary-item-card {
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 14px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.itinerary-item-card:hover {
  border-color: rgba(231, 111, 81, 0.3);
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.item-layout {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.attraction-name {
  font-size: 1.1rem;
  margin: 0;
  color: #264653;
  font-weight: 600;
}

.rounded-tag {
  background: rgba(42, 157, 143, 0.1);
  color: #2A9D8F;
  border: none;
  font-weight: 500;
}

.detail-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.location-box {
  font-size: 0.875rem;
  color: #5a6c7d;
  display: flex;
  align-items: center;
  gap: 4px;
}

.time-range-box {
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-tag {
  background: rgba(0, 0, 0, 0.04);
  padding: 2px 8px;
  border-radius: 4px;
  color: #5a6c7d;
}

.arrow {
  color: #ccc;
}

.item-ops {
  display: flex;
  gap: 8px;
}

.op-btn {
  border: none;
  background: rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}

.op-btn.edit:hover {
  background: rgba(231, 111, 81, 0.1);
  color: #E76F51;
}

.op-btn.delete:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.display-name {
  background: rgba(231, 111, 81, 0.08);
  padding: 12px 16px;
  border-radius: 10px;
  font-weight: 600;
  color: #E76F51;
}

.preview-box {
  margin-top: 12px;
  padding: 12px;
  background: rgba(42, 157, 143, 0.08);
  border-radius: 10px;
  text-align: center;
}

.preview-date {
  font-weight: 700;
  color: #2A9D8F;
  margin-left: 8px;
}

.footer-line {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
}

.pdf-footer-modern {
  text-align: center;
  padding: 16px 0 24px;
  color: #5a6c7d;
  font-size: 0.875rem;
}

.modern-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.modern-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #264653;
}

.btn-confirm {
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border: none;
}

.btn-confirm:hover {
  background: linear-gradient(135deg, #d65d3f, #e8914a);
}

@media (max-width: 768px) {
  .itinerary-page {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .day-wrapper {
    padding: 0 16px 24px;
  }
  
  .day-sticky-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .item-layout {
    flex-direction: column;
  }
  
  .item-ops {
    align-self: flex-end;
  }
}
</style>
