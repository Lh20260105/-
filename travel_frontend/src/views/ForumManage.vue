<template>
  <div class="manage-page">
    <div class="page-header">
      <div class="header-content">
        <h2>论坛管理</h2>
        <p>管理驴友广场的帖子内容</p>
      </div>
      <div class="header-actions">
        <el-input v-model="searchText" placeholder="搜索帖子标题..." style="width: 240px" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadPage(1)">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image 
              v-if="scope.row.imageUrl" 
              :src="scope.row.imageUrl" 
              :preview-src-list="[scope.row.imageUrl]"
              style="width: 60px; height: 40px; border-radius: 6px" 
              fit="cover"
              preview-teleported
            />
            <span v-else class="no-image">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="author" label="发布者" width="120" />
        <el-table-column prop="createTime" label="发布时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
        </el-table-column>
        
        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" class="view-btn" @click="viewDetail(scope.row.id)">查看</el-button>
            <el-button type="danger" size="small" class="delete-btn" @click="confirmDel(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-box">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="currentPage"
          @current-change="loadPage"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchText = ref('')
const loading = ref(false)

const loadPage = async (page = 1) => {
  loading.value = true
  currentPage.value = page
  const res = await request.get('/forum/page', {
    params: {
      current: currentPage.value,
      size: pageSize.value,
      title: searchText.value
    }
  })
  if (res.data.success) {
    tableData.value = res.data.data.records
    total.value = res.data.data.total
  }
  loading.value = false
}

const confirmDel = (id) => {
  // 从 sessionStorage 获取当前管理员信息
  const adminInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  
  ElMessageBox.confirm('确定要删除违规帖子吗？', '管理警告', { type: 'error' }).then(async () => {
    const res = await request.delete(`/forum/${id}`, {
      params: {
        requesterId: adminInfo.id,
        role: 'ADMIN' // 显式声明管理员身份
      }
    })
    if (res.data.success) {
      ElMessage.success('已删除')
      loadPage(currentPage.value)
    }
  }).catch(() => {})
}

const viewDetail = (id) => {
  window.open(`http://localhost:5173/post/${id}`, '_blank')
}

const formatDate = (time) => time ? time.replace('T', ' ').substring(0, 16) : ''

onMounted(() => loadPage(1))
</script>

<style scoped>
.manage-page {
  padding: 24px;
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-container {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.custom-table :deep(.el-table) {
  --el-table-border-color: rgba(0, 0, 0, 0.06);
}

.custom-table :deep(.el-table th) {
  background: rgba(0, 0, 0, 0.02);
  color: #264653;
  font-weight: 600;
}

.custom-table :deep(.el-table tr:hover) {
  background: rgba(231, 111, 81, 0.04);
}

.no-image {
  color: #94a3b8;
  font-size: 0.8rem;
}

.view-btn {
  background: rgba(231, 111, 81, 0.1);
  border-color: transparent;
  color: #E76F51;
}

.view-btn:hover {
  background: #E76F51;
  border-color: #E76F51;
  color: white;
}

.delete-btn {
  background: rgba(239, 68, 68, 0.1);
  border-color: transparent;
  color: #ef4444;
}

.delete-btn:hover {
  background: #ef4444;
  border-color: #ef4444;
  color: white;
}

.pagination-box {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .manage-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .table-container {
    padding: 12px;
    overflow-x: auto;
  }
  
  .custom-table {
    min-width: 600px;
  }
}

@media (max-width: 480px) {
  .manage-page {
    padding: 12px;
  }
  
  .header-content h2 {
    font-size: 1.25rem;
  }
  
  .table-container {
    padding: 8px;
    border-radius: 12px;
  }
}
</style>
