<template>
  <div style="padding: 20px;">
    <h3>用户管理</h3>
    <el-table :data="userList" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="权限角色">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'success'">
            {{ scope.row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="danger" size="small" @click="delUser(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const userList = ref([])

const loadUsers = async () => {
  const res = await request.get('/users/list')
  if (res.data.success) {
    // 同样改为 res.data.data
    userList.value = res.data.data 
  }
}

const delUser = (id) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '警告').then(async () => {
    await request.delete(`/users/delete/${id}`)
    ElMessage.success('删除成功')
    loadUsers()
  })
}

onMounted(() => loadUsers())
</script>