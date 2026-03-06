<template>
  <div class="statistics-page">
    <div class="noise-overlay"></div>
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <div class="page-container">
      <div class="page-header">
        <div class="header-content">
          <h2>数据统计</h2>
          <p>实时了解平台运营状况</p>
        </div>
        <el-button @click="loadAllData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>

      <el-row :gutter="24" class="stats-row">
        <el-col :xs="24" :sm="8">
          <div class="stat-card card-1">
            <div class="stat-icon">
              <el-icon><Location /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">总景点数</span>
              <span class="stat-number">{{ baseStats.totalAttractions || 0 }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card card-2">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">总用户数</span>
              <span class="stat-number">{{ baseStats.totalUsers || 0 }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card card-3">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">今日新增用户</span>
              <span class="stat-number">{{ baseStats.todayNewUsers || 0 }}</span>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-card class="chart-card" shadow="never">
        <template #header>
          <div class="chart-header">
            <span class="chart-title">
              <el-icon><Histogram /></el-icon>
              热门景点收藏排行 TOP 10
            </span>
          </div>
        </template>
        <div id="hotChart" style="width: 100%; height: 450px;"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Refresh, Location, User, TrendCharts, Histogram } from '@element-plus/icons-vue'

const baseStats = ref({})

const loadAllData = async () => {
  try {
    const baseRes = await request.get('/admin/statistics/base')
    if (baseRes.data.success) {
      baseStats.value = baseRes.data.data || {}
    }

    const hotRes = await request.get('/admin/statistics/hot-attractions')
    if (hotRes.data.success && hotRes.data.data) {
      initChart(hotRes.data.data)
    } else {
      console.warn("排行数据为空或请求失败")
    }
  } catch (error) {
    console.error("加载统计数据出错：", error)
    ElMessage.error('无法加载统计数据，请检查后端接口')
  }
}

const initChart = (data) => {
  const chartDom = document.getElementById('hotChart')
  if (!chartDom) return
  
  const existingInstance = echarts.getInstanceByDom(chartDom)
  if (existingInstance) {
    existingInstance.dispose()
  }

  const myChart = echarts.init(chartDom)
  
  const names = data.map(item => item.name)
  const counts = data.map(item => item.collectCount)

  const option = {
    title: {
      text: '景点收藏人气分布',
      left: 'center',
      textStyle: { fontSize: 16, color: '#264653' }
    },
    tooltip: { 
      trigger: 'axis',
      axisPointer: { type: 'shadow' } 
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLine: { show: true, lineStyle: { color: '#E76F51' } },
      axisLabel: { 
        interval: 0, 
        rotate: 35, 
        color: '#5a6c7d'
      }
    },
    yAxis: { 
      type: 'value',
      name: '收藏次数',
      minInterval: 1,
      axisLine: { 
        show: true, 
        lineStyle: { color: '#E76F51' } 
      },
      axisTick: { show: true },
      splitLine: {
        show: true,
        lineStyle: { type: 'dashed', color: 'rgba(0,0,0,0.06)' }
      }
    },
    series: [
      {
        name: '收藏数',
        data: counts,
        type: 'bar',
        barWidth: '40%',
        itemStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#E76F51' },
            { offset: 0.5, color: '#F4A261' },
            { offset: 1, color: '#F4A261' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#d65d3f' },
              { offset: 0.7, color: '#d65d3f' },
              { offset: 1, color: '#e8914a' }
            ])
          }
        }
      }
    ]
  }
  
  myChart.setOption(option)

  window.addEventListener('resize', () => {
    myChart.resize()
  })
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.statistics-page {
  position: relative;
  min-height: calc(100vh - 64px);
  padding: 24px;
}

.noise-overlay {
  position: absolute;
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
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
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
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-content h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #264653;
}

.header-content p {
  margin: 4px 0 0 0;
  font-size: 0.875rem;
  color: #5a6c7d;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.card-1 .stat-icon {
  background: linear-gradient(135deg, rgba(231, 111, 81, 0.15), rgba(231, 111, 81, 0.05));
  color: #E76F51;
}

.card-2 .stat-icon {
  background: linear-gradient(135deg, rgba(42, 157, 143, 0.15), rgba(42, 157, 143, 0.05));
  color: #2A9D8F;
}

.card-3 .stat-icon {
  background: linear-gradient(135deg, rgba(244, 162, 97, 0.15), rgba(244, 162, 97, 0.05));
  color: #F4A261;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 0.875rem;
  color: #5a6c7d;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #264653;
}

.chart-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.chart-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 16px 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  font-weight: 600;
  color: #264653;
}

.chart-title .el-icon {
  color: #E76F51;
}

@media (max-width: 768px) {
  .statistics-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .stat-card {
    padding: 16px;
    margin-bottom: 12px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
  
  .stat-number {
    font-size: 1.5rem;
  }
  
  .chart-card {
    margin-top: 16px;
  }
  
  #hotChart {
    height: 350px !important;
  }
}

@media (max-width: 480px) {
  .statistics-page {
    padding: 12px;
  }
  
  .header-content h2 {
    font-size: 1.25rem;
  }
  
  .stat-card {
    padding: 12px;
    gap: 12px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
  
  .stat-number {
    font-size: 1.25rem;
  }
  
  #hotChart {
    height: 280px !important;
  }
}
</style>
