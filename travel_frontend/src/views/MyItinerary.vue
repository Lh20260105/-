<template>
  <div class="itinerary-page">
    <el-card class="modern-card main-header" shadow="never">
      <div class="header-content">
        <div class="title-group">
          <div class="icon-circle">
            <el-icon size="24" color="#409EFF"><Calendar /></el-icon>
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
            <el-card class="itinerary-item-glass-card" shadow="hover">
              <div class="item-layout">
                <div class="item-main">
                  <div class="title-row">
                    <h4 class="attraction-name">{{ item.attractionName }}</h4>
                    <div class="tag-group">
                      <el-tag size="small" effect="light" type="success" class="rounded-tag">
                        {{ item.stayDays || 1 }} 天行程
                      </el-tag>
                    </div>
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

    <el-dialog v-model="editDialogVisible" title="修改行程计划" width="440px" custom-class="modern-dialog">
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
// JavaScript 逻辑保持不变，确保功能 100% 可用
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
/* --- 全局背景与布局 --- */
.itinerary-page {
  padding: 40px 20px;
  background-color: #f8fafc;
  min-height: 100vh;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

/* --- 顶部卡片设计 --- */
.main-header {
  max-width: 1000px;
  margin: 0 auto 30px;
  border-radius: 20px;
  background: white;
  box-shadow: 0 4px 25px rgba(0,0,0,0.05) !important;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title-group { display: flex; align-items: center; gap: 15px; }
.icon-circle {
  background: #eff6ff;
  width: 50px;
  height: 50px;
  border-radius: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.main-title { font-size: 22px; font-weight: 800; color: #1e293b; display: block; }
.sub-title { font-size: 13px; color: #94a3b8; }

.btn-export-modern {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 20px 25px;
  font-weight: 600;
  transition: all 0.3s;
}
.btn-export-modern:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(37,99,235,0.3);
  color: white;
}

/* --- 内容容器 --- */
.pdf-container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0,0,0,0.03);
}

/* --- Banner 样式 --- */
.pdf-banner-modern {
  height: 180px;
  background: linear-gradient(120deg, #e0f2fe 0%, #dbeafe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.banner-overlay h1 { font-size: 28px; color: #1e3a8a; margin: 0; font-weight: 900; }
.banner-meta { margin-top: 10px; font-size: 14px; color: #64748b; display: flex; gap: 15px; justify-content: center; }

/* --- 每日标题设计 --- */
.day-wrapper { padding: 0 40px 40px; }
.day-sticky-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 30px 0 20px;
}
.day-pill {
  background: #1e293b;
  color: white;
  padding: 8px 20px;
  border-radius: 50px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.day-number { font-weight: 900; font-size: 16px; }
.day-date { font-size: 14px; opacity: 0.8; }

.weather-pill {
  background: #fff7ed;
  color: #c2410c;
  padding: 6px 15px;
  border-radius: 50px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #ffedd5;
}

/* --- 行程卡片设计 --- */
.itinerary-item-glass-card {
  border: 1px solid #f1f5f9;
  border-radius: 16px;
  transition: all 0.3s;
}
.itinerary-item-glass-card:hover {
  border-color: #3b82f6;
  background-color: #f8faff;
  transform: translateX(5px);
}
.item-layout { display: flex; justify-content: space-between; align-items: center; }
.title-row { display: flex; align-items: center; gap: 15px; margin-bottom: 10px; }
.attraction-name { font-size: 17px; margin: 0; color: #1e293b; }
.rounded-tag { border-radius: 6px; font-weight: 600; }

.detail-row { display: flex; flex-direction: column; gap: 8px; }
.location-box { font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 5px; }
.time-range-box { font-size: 12px; display: flex; align-items: center; gap: 10px; }
.time-tag { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; color: #475569; }
.arrow { color: #cbd5e1; }

/* --- 操作按钮设计 --- */
.item-ops { display: flex; gap: 10px; }
.op-btn { border: none; background: #f8fafc; transition: all 0.2s; }
.op-btn.edit:hover { background: #eff6ff; color: #3b82f6; transform: scale(1.1); }
.op-btn.delete:hover { background: #fef2f2; color: #ef4444; transform: scale(1.1); }

/* --- 弹窗样式 --- */
.display-name { 
  background: #f8fafc; 
  padding: 10px 15px; 
  border-radius: 10px; 
  font-weight: bold; 
  color: #3b82f6; 
}
.preview-box {
  margin-top: 15px;
  padding: 15px;
  background: #f0fdf4;
  border-radius: 10px;
  text-align: center;
}
.preview-date { font-weight: 900; color: #166534; margin-left: 10px; }

.footer-line { height: 1px; background: #f1f5f9; margin-bottom: 15px; }
.pdf-footer-modern { text-align: center; padding: 20px 0 40px; color: #94a3b8; font-size: 13px; }
</style>