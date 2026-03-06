<template>
  <div class="manage-page">
    <div class="page-header">
      <div class="header-content">
        <h2>用户管理</h2>
        <p>管理系统中的用户数据</p>
      </div>
      <el-button @click="loadUsers">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <div class="table-container">
      <el-table :data="userList" v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="权限角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'success'" class="role-tag">
              {{ scope.row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="danger" size="small" class="delete-btn" @click="delUser(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const userList = ref([])
const loading = ref(false)

const loadUsers = async () => {
  loading.value = true
  const res = await request.get('/users/list')
  if (res.data.success) {
    userList.value = res.data.data 
  }
  loading.value = false
}

const delUser = (id) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '警告').then(async () => {
    await request.delete(`/users/delete/${id}`)
    ElMessage.success('删除成功')
    loadUsers()
  }).catch(() => {})
}

onMounted(() => loadUsers())
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

.role-tag {
  background: rgba(42, 157, 143, 0.1);
  color: #2A9D8F;
  border: none;
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

@media (max-width: 768px) {
  .manage-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .table-container {
    padding: 12px;
    overflow-x: auto;
  }
  
  .custom-table {
    min-width: 500px;
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
