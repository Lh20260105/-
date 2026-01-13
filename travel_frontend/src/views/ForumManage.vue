<template>
  <div class="manage-container">
    <div class="search-box">
      <el-input v-model="searchText" placeholder="搜索帖子标题..." style="width: 300px; margin-right: 10px" />
      <el-button type="primary" @click="loadPage(1)">查询</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="封面" width="120">
        <template #default="scope">
          <el-image 
            v-if="scope.row.imageUrl" 
            :src="scope.row.imageUrl" 
            :preview-src-list="[scope.row.imageUrl]"
            style="width: 80px; height: 50px; border-radius: 5px" 
            preview-teleported
          />
          <span v-else>无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="author" label="发布者" width="120" />
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
      </el-table-column>
      
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="viewDetail(scope.row.id)">查看</el-button>
          <el-button type="danger" size="small" @click="confirmDel(scope.row.id)">删除</el-button>
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchText = ref('')

// 加载分页数据
const loadPage = async (page = 1) => {
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
}

// 删除逻辑（复用你之前的接口）
const confirmDel = (id) => {
  ElMessageBox.confirm('确定要删除违规帖子吗？', '管理警告', { type: 'error' }).then(async () => {
    const res = await request.delete(`/forum/${id}`)
    if (res.data.success) {
      ElMessage.success('已删除')
      loadPage(currentPage.value)
    }
  })
}

// 查看详情（可以跳转到前台的详情页，或者弹窗显示）
const viewDetail = (id) => {
  window.open(`http://localhost:5173/post/${id}`, '_blank')
}

const formatDate = (time) => time ? time.replace('T', ' ').substring(0, 16) : ''

onMounted(() => loadPage(1))
</script>

<style scoped>
.manage-container { padding: 20px; background: #fff; min-height: 500px; }
.pagination-box { margin-top: 20px; display: flex; justify-content: center; }
</style>