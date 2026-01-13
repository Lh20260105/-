<template>
  <div style="padding: 20px;">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" style="text-align: center;">
          <template #header>总景点数</template>
          <div class="stat-number" style="color: #409EFF;">
            {{ baseStats.totalAttractions || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" style="text-align: center;">
          <template #header>总用户数</template>
          <div class="stat-number" style="color: #67C23A;">
            {{ baseStats.totalUsers || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" style="text-align: center;">
          <template #header>今日新增用户</template>
          <div class="stat-number" style="color: #F56C6C;">
            {{ baseStats.todayNewUsers || 0 }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="always" style="margin-top: 30px;">
      <template #header>
        <div style="font-weight: bold; display: flex; justify-content: space-between; align-items: center;">
          <span>🔥 热门景点收藏排行 (TOP 10)</span>
          <el-button size="small" @click="loadAllData">刷新数据</el-button>
        </div>
      </template>
      <div id="hotChart" style="width: 100%; height: 450px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const baseStats = ref({})

/**
 * 获取所有统计数据
 */
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

/**
 * 初始化 ECharts 图表
 */
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
      textStyle: { fontSize: 16, color: '#303133' }
    },
    tooltip: { 
      trigger: 'axis',
      axisPointer: { type: 'shadow' } 
    },
    grid: {
      left: '5%', // 稍微调大左边距，给纵坐标数值留出空间
      right: '5%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLine: { show: true, lineStyle: { color: '#909399' } }, // 显示横坐标轴线
      axisLabel: { 
        interval: 0, 
        rotate: 35, 
        color: '#606266'
      }
    },
    yAxis: { 
      type: 'value',
      name: '收藏次数',
      minInterval: 1,
      // --- 【本次修改的核心：显示纵坐标轴线和刻度】 ---
      axisLine: { 
        show: true, 
        lineStyle: { color: '#909399' } 
      },
      axisTick: { 
        show: true 
      },
      splitLine: {
        show: true,
        lineStyle: { type: 'dashed', color: '#E4E7ED' } // 背景网格设为虚线，更有质感
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
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
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
.stat-number {
  font-size: 28px; 
  font-weight: bold; 
  margin: 10px 0;
}
</style>