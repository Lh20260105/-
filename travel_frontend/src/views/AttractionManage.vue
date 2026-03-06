<template>
  <div class="manage-page">
    <div class="page-header">
      <div class="header-content">
        <h2>景点管理</h2>
        <p>管理系统中的景点数据</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增景点
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="attractionList" v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="预览图" width="120" align="center">
          <template #default="scope">
            <el-image 
              :src="scope.row.imageUrl || `https://picsum.photos/seed/${scope.row.id}/200/150`" 
              style="width: 80px; height: 50px; border-radius: 6px" 
              fit="cover" 
              :preview-src-list="[scope.row.imageUrl]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="景点名称" />
        <el-table-column prop="location" label="地域" />
        <el-table-column prop="category" label="分类" />
        <el-table-column prop="ticketPrice" label="门票(元)" width="100" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="danger" size="small" class="delete-btn" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" title="新增景点信息" width="500px" class="custom-dialog">
      <el-form :model="form" label-width="100px" class="custom-form">
        <el-form-item label="景点名称">
          <el-input v-model="form.name" placeholder="请输入景点名称" />
        </el-form-item>
        <el-form-item label="景点地域">
          <el-input v-model="form.location" placeholder="如：北京" />
        </el-form-item>
        <el-form-item label="景点分类">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="自然风光" value="自然风光" />
            <el-option label="人文古迹" value="人文古迹" />
            <el-option label="美食品尝" value="美食品尝" />
          </el-select>
        </el-form-item>
        <el-form-item label="门票价格">
          <el-input-number v-model="form.ticketPrice" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-input v-model="form.openTime" placeholder="如：08:00-18:00" />
        </el-form-item>
        
        <el-form-item label="景点照片">
          <el-upload
            class="avatar-uploader"
            action="/api/files/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="uploaded-img" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议图片大小不超过 2MB</div>
        </el-form-item>

        <el-form-item label="景点描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入景点描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

const attractionList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)

const form = ref({
  name: '',
  location: '',
  category: '',
  ticketPrice: 0,
  openTime: '',
  imageUrl: '',
  description: ''
})

const loadData = async () => {
  loading.value = true
  const res = await request.get('/attractions/list')
  if (res.data.success) {
    attractionList.value = res.data.data
  }
  loading.value = false
}

const handleUploadSuccess = (res) => {
  if (res.success) {
    form.value.imageUrl = res.data
    ElMessage.success('照片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const openAddDialog = () => {
  form.value = { name: '', location: '', category: '', ticketPrice: 0, openTime: '', imageUrl: '', description: '' }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name || !form.value.imageUrl) {
    return ElMessage.warning('请填写名称并上传照片')
  }
  const res = await request.post('/attractions/add', form.value)
  if (res.data.success) {
    ElMessage.success('景点添加成功！')
    dialogVisible.value = false
    loadData()
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要永久删除该景点吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await request.delete(`/attractions/${id}`)
    if (res.data.success) { 
      ElMessage.success('已成功删除景点')
      loadData()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(loadData)
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

.avatar-uploader {
  border: 1px dashed rgba(231, 111, 81, 0.3);
  border-radius: 8px;
  cursor: pointer;
  width: 160px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.02);
  transition: all 0.3s;
}

.avatar-uploader:hover {
  border-color: #E76F51;
  background: rgba(231, 111, 81, 0.05);
}

.uploaded-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.uploader-icon {
  font-size: 28px;
  color: #5a6c7d;
}

.upload-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}

.custom-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.custom-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #264653;
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
