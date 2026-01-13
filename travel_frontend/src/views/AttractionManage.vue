<template>
  <div style="padding: 20px;">
    <div style="margin-bottom: 20px; display: flex; justify-content: space-between;">
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon> 新增景点
      </el-button>
      <el-button @click="loadData">刷新列表</el-button>
    </div>

    <el-table :data="attractionList" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column label="预览图" width="120" align="center">
        <template #default="scope">
          <el-image :src="scope.row.imageUrl" style="width: 80px; height: 50px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="景点名称" />
      <el-table-column prop="location" label="地域" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="ticketPrice" label="门票(元)" width="100" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增景点信息" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="景点名称">
          <el-input v-model="form.name" />
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
            action="http://localhost:8080/api/attractions/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="uploaded-img" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="景点描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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

const attractionList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)

// 表单数据模型
const form = ref({
  name: '',
  location: '',
  category: '',
  ticketPrice: 0,
  openTime: '',
  imageUrl: '',
  description: ''
})

// 加载数据
const loadData = async () => {
  loading.value = true
  const res = await request.get('/attractions/list')
  if (res.data.success) {
    attractionList.value = res.data.data
  }
  loading.value = false
}

// 图片上传成功的回调
const handleUploadSuccess = (res) => {
  if (res.success) {
    form.value.imageUrl = res.data // 获取后端返回的 URL
    ElMessage.success('照片上传成功')
  }
}

// 打开弹窗并重置表单
const openAddDialog = () => {
  form.value = { name: '', location: '', category: '', ticketPrice: 0, openTime: '', imageUrl: '', description: '' }
  dialogVisible.value = true
}

// 提交新增数据
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
  ElMessageBox.confirm('确定删除吗？', '警告').then(async () => {
    const res = await request.delete(`/attractions/delete/${id}`)
    if (res.data.success) { ElMessage.success('已删除'); loadData(); }
  })
}

onMounted(loadData)
</script>

<style scoped>
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  width: 178px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.uploaded-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>