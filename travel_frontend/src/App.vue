<template>
  <div class="main-app">
    <div v-if="['/login', '/register'].includes($route.path)" class="full-screen">
      <router-view />
    </div>

    <el-container v-else style="height: 100vh;">
      
      <el-aside v-if="isAdmin" width="220px" class="admin-sidebar">
        <div class="sidebar-logo">
          <el-icon><Monitor /></el-icon>
          <span>管理后台</span>
        </div>
        <el-menu 
          active-text-color="#ffd04b" 
          background-color="#304156" 
          text-color="#fff" 
          router 
          :default-active="$route.path"
        >
          <el-menu-item index="/attractions">
            <el-icon><Location /></el-icon>
            <span>景点管理</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/forum-manage">
            <el-icon><ChatDotSquare /></el-icon>
            <span>论坛管理</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <el-icon><Histogram /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-divider style="border-color: #4e5e73; margin: 10px 0;" />
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>返回前台首页</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="nav-header">
          <div class="header-left">
            <span class="logo-text">云游推荐系统</span>
            <el-tag v-if="isAdmin" type="danger" size="small" style="margin-left: 10px;">管理员模式</el-tag>
          </div>
          
          <div class="header-right">
            <div v-if="!isAdmin" class="user-menu">
              <el-button link @click="$router.push('/home')">首页</el-button>
              <el-button link @click="$router.push('/forum')">🌍 驴友广场</el-button>
              <el-button link @click="$router.push('/my-itinerary')">我的行程单</el-button>
              <el-button link @click="$router.push('/favorites')">❤️ 我的收藏</el-button>
              <el-button link @click="$router.push('/profile')">个人中心</el-button>
            </div>
            
            <div class="user-info">
              <el-avatar 
                :size="30" 
                :src="user.avatarUrl || user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" 
              />
              <span class="welcome-msg">欢迎您, {{ nickname || username || '游客' }}</span>
              <el-button link type="primary" @click="handleLogout" class="logout-btn">退出登录</el-button>
            </div>
          </div>
        </el-header>

        <el-main class="app-main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
// 1. 导入全局响应式状态
import { userState } from './utils/userBus'
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Monitor, Location, User, Histogram, 
  HomeFilled, ChatDotSquare 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 2. 【核心修复】：直接让 user 变量指向共享状态
// 这样当 UserProfile.vue 修改了 userState.userInfo 时，这里会同步自动更新
const user = computed(() => userState.userInfo)

// 3. 判断角色（基于响应式的 user）
const isAdmin = computed(() => {
  const role = user.value.role ? user.value.role.toUpperCase() : ''
  return role === 'ADMIN'
})

// 4. 获取名称显示（基于响应式的 user）
const username = computed(() => user.value.username || '')
const nickname = computed(() => user.value.nickname || '')

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning'
  }).then(() => {
    // 清空缓存
    sessionStorage.removeItem('user_info')
    ElMessage.success('已退出登录')
    // 重定向到登录页
    window.location.href = '/login'
  }).catch(() => {})
}
</script>

<style>
/* 保持你原有的样式不变 */
body { margin: 0; font-family: "Helvetica Neue", Helvetica, "PingFang SC", Arial, sans-serif; }
.nav-header { display: flex; justify-content: space-between; align-items: center; background-color: #fff; border-bottom: 1px solid #e6e6e6; box-shadow: 0 1px 4px rgba(0,21,41,.08); padding: 0 20px; }
.logo-text { font-size: 20px; font-weight: bold; color: #409EFF; }
.header-right { display: flex; align-items: center; }
.user-menu { margin-right: 30px; }
.user-info { display: flex; align-items: center; gap: 10px; border-left: 1px solid #eee; padding-left: 20px; }
.welcome-msg { font-size: 14px; color: #606266; }
.admin-sidebar { background-color: #304156; transition: width 0.3s; }
.sidebar-logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: bold; gap: 10px; background: #2b2f3a; }
.el-menu { border-right: none; }
.app-main-content { background-color: #f0f2f5; }
.full-screen { height: 100vh; width: 100vw; }
</style>