<template>
  <div class="manage-page">
    <div class="page-header">
      <div class="header-content">
        <h2>轮播图管理</h2>
        <p>管理前台首页轮播图展示</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增轮播图
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="bannerList" v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="预览图" width="200" align="center">
          <template #default="scope">
            <el-image 
              :src="scope.row.imageUrl" 
              style="width: 160px; height: 80px; border-radius: 8px" 
              fit="cover" 
              :preview-src-list="[scope.row.imageUrl]"
              preview-teleported
            >
              <template #error>
                <div class="no-image">暂无图片</div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="linkUrl" label="跳转链接" width="200">
          <template #default="scope">
            <span class="link-text">{{ scope.row.linkUrl || '无' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" class="status-tag">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" class="action-btn" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              size="small" 
              class="action-btn"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" class="delete-btn" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="500px" class="custom-dialog">
      <el-form :model="form" label-width="100px" class="custom-form">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="点击后跳转的链接（可选）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
          <div class="form-tip">数字越小排序越靠前</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
          <span class="status-text">{{ form.status === 1 ? '启用' : '禁用' }}</span>
        </el-form-item>
        <el-form-item label="轮播图片">
          <el-upload
            ref="uploadRef"
            class="banner-uploader"
            action="/api/files/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-progress="handleUploadProgress"
            :before-upload="beforeUpload"
            :http-request="customUpload"
            accept="image/*"
          >
            <img v-if="form.imageUrl && !isUploading" :src="form.imageUrl" class="uploaded-img" />
            <div v-else-if="isUploading" class="upload-progress">
              <el-progress 
                type="circle" 
                :percentage="uploadProgress" 
                :status="uploadProgress >= 90 ? 'success' : ''"
                :stroke-width="8"
                :width="100"
              />
              <span class="progress-text">{{ uploadProgress >= 90 ? '压缩处理中...' : `上传中 ${uploadProgress}%` }}</span>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon class="uploader-icon"><Plus /></el-icon>
              <span>点击上传图片</span>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸: 1920 x 600，支持 JPG、PNG 格式，任意大小图片会自动压缩至2MB以内</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const bannerList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const uploadProgress = ref(0)
const isUploading = ref(false)
const uploadRef = ref(null)
const form = ref({
  id: null,
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/banners')
    if (res.data.success) {
      bannerList.value = res.data.data || []
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    title: '',
    imageUrl: '',
    linkUrl: '',
    sortOrder: 0,
    status: 1
  }
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件!')
    return false
  }
  isUploading.value = true
  uploadProgress.value = 0
  return true
}

const handleUploadProgress = (event) => {
  uploadProgress.value = Math.round(event.percent)
}

const handleUploadSuccess = (response) => {
  isUploading.value = false
  uploadProgress.value = 0
  if (response.success) {
    form.value.imageUrl = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = (error) => {
  isUploading.value = false
  uploadProgress.value = 0
  console.error('上传错误:', error)
  let errorMsg = '上传失败'
  if (error.status === 413) {
    errorMsg = '文件太大，请联系管理员'
  } else if (error.status === 0) {
    errorMsg = '网络错误，请检查连接'
  } else if (error.message) {
    errorMsg = error.message
  }
  ElMessage.error(errorMsg)
}

const customUpload = async (options) => {
  const { file, onProgress, onSuccess, onError } = options
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await request.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress({ percent })
      }
    })
    
    if (response.data.success) {
      onSuccess(response.data)
    } else {
      onError(new Error(response.data.message))
    }
  } catch (error) {
    onError(error)
  }
}

const handleSubmit = async () => {
  if (!form.value.imageUrl) {
    ElMessage.warning('请上传轮播图片')
    return
  }
  
  try {
    if (isEdit.value) {
      await request.put(`/admin/banners/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/banners', form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const toggleStatus = async (row) => {
  try {
    await request.put(`/admin/banners/${row.id}/status`)
    ElMessage.success('状态已更新')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个轮播图吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/admin/banners/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
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

.no-image {
  width: 160px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 8px;
  color: #94a3b8;
  font-size: 0.8rem;
}

.link-text {
  color: #5a6c7d;
  font-size: 0.875rem;
}

.status-tag {
  border: none;
}

.action-btn {
  margin-right: 8px;
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

.custom-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.custom-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #264653;
}

.form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.status-text {
  margin-left: 12px;
  color: #5a6c7d;
}

.banner-uploader {
  width: 100%;
}

.upload-placeholder {
  width: 100%;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed rgba(231, 111, 81, 0.3);
  border-radius: 8px;
  cursor: pointer;
  background: rgba(0, 0, 0, 0.02);
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #E76F51;
  background: rgba(231, 111, 81, 0.05);
}

.uploader-icon {
  font-size: 28px;
  color: #5a6c7d;
  margin-bottom: 8px;
}

.uploaded-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.upload-progress {
  width: 100%;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed rgba(231, 111, 81, 0.3);
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.02);
  gap: 8px;
}

.progress-text {
  font-size: 12px;
  color: #5a6c7d;
}

.upload-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
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
    min-width: 700px;
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
