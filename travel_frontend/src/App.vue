<template>
  <div class="main-app">
    <div v-if="['/login', '/register'].includes($route.path)" class="full-screen">
      <router-view />
    </div>

    <el-container v-else class="app-container">
      
      <!-- 管理员侧边栏 -->
      <el-aside v-if="isAdmin" :width="sidebarCollapsed ? '64px' : '240px'" class="admin-sidebar">
        <div class="sidebar-noise"></div>
        <div class="sidebar-header" :class="{ 'collapsed': sidebarCollapsed }">
          <div class="sidebar-logo">
            <div class="logo-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <span class="logo-text">管理后台</span>
          </div>
          <el-button 
            class="collapse-btn" 
            :icon="sidebarCollapsed ? Expand : Fold" 
            @click="toggleSidebar"
            circle
            size="small"
          />
        </div>
        <div class="sidebar-content">
          <el-menu 
            :collapse="sidebarCollapsed"
            active-text-color="#E76F51" 
            background-color="transparent" 
            text-color="#264653" 
            router 
            :default-active="$route.path"
            class="admin-menu"
          >
            <el-menu-item index="/attractions" class="menu-item">
              <el-icon><Location /></el-icon>
              <template #title>景点管理</template>
            </el-menu-item>
            <el-menu-item index="/users" class="menu-item">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            <el-menu-item index="/forum-manage" class="menu-item">
              <el-icon><ChatDotSquare /></el-icon>
              <template #title>论坛管理</template>
            </el-menu-item>
            <el-menu-item index="/banners" class="menu-item">
              <el-icon><Picture /></el-icon>
              <template #title>轮播图管理</template>
            </el-menu-item>
            <el-menu-item index="/statistics" class="menu-item">
              <el-icon><Histogram /></el-icon>
              <template #title>数据统计</template>
            </el-menu-item>
          </el-menu>
          
          <div class="sidebar-footer">
            <div class="menu-divider" v-show="!sidebarCollapsed"></div>
            
            <div class="back-home" @click="handleBackToHome">
              <el-icon><HomeFilled /></el-icon>
              <span v-show="!sidebarCollapsed">返回前台</span>
            </div>
          </div>
        </div>
      </el-aside>

      <el-container>
        <!-- 顶部导航栏 -->
        <el-header :class="['nav-header', { 'admin-header': isAdmin }]">
          <div class="header-noise"></div>
          <div class="header-gradient"></div>
          
          <div class="header-content">
            <div class="header-left">
              <div class="brand" @click="$router.push('/home')">
                <div class="brand-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <span class="brand-text">云游推荐</span>
              </div>
              <el-tag v-if="isAdminRole" class="admin-tag">管理员模式</el-tag>
            </div>
            
            <div class="header-right">
              <!-- 用户导航菜单 -->
              <div v-if="!isAdmin" class="user-nav">
                <router-link to="/home" class="nav-item" :class="{ active: $route.path === '/home' }">
                  <el-icon><HomeFilled /></el-icon>
                  <span>首页</span>
                </router-link>
                <router-link to="/forum" class="nav-item" :class="{ active: $route.path === '/forum' }">
                  <el-icon><Compass /></el-icon>
                  <span>驴友广场</span>
                </router-link>
                <router-link to="/my-itinerary" class="nav-item" :class="{ active: $route.path === '/my-itinerary' }">
                  <el-icon><Calendar /></el-icon>
                  <span>我的行程</span>
                </router-link>
                <router-link to="/favorites" class="nav-item" :class="{ active: $route.path === '/favorites' }">
                  <el-icon><Star /></el-icon>
                  <span>我的收藏</span>
                </router-link>
                <router-link to="/profile" class="nav-item" :class="{ active: $route.path === '/profile' }">
                  <el-icon><UserFilled /></el-icon>
                  <span>个人中心</span>
                </router-link>
                <router-link v-if="isAdminRole" to="/statistics" class="nav-item admin-nav-portal" >
                  <el-icon><Monitor /></el-icon>
                   <span>管理后台</span>
                 </router-link>
              </div>
              
              <!-- 用户信息 -->
              <div class="user-panel">
                <el-avatar 
                  :size="36" 
                  :src="user.avatarUrl || user.avatar || `https://picsum.photos/seed/avatar${username}/100/100`" 
                  class="user-avatar"
                />
                <div class="user-detail">
                  <span class="user-name">{{ nickname || username || '游客' }}</span>
                  <span class="user-role">{{ isAdminRole ? '管理员' : '旅行者' }}</span>
                </div>
                <el-button class="logout-btn" @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出</span>
                </el-button>
              </div>
            </div>
          </div>
        </el-header>

        <el-main :class="['app-main-content', { 'admin-main-content': isAdmin }]">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { userState } from './utils/userBus'
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Monitor, Location, User, Histogram, 
  HomeFilled, ChatDotSquare, SwitchButton,
  Compass, Calendar, Star, UserFilled,
  Fold, Expand, Picture
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userInfo = ref(JSON.parse(sessionStorage.getItem('user_info') || '{}'))
const user = computed(() => userState.userInfo)

// 判断当前是否在管理员页面（根据路由路径）
const isAdmin = computed(() => {
  const adminRoutes = ['/attractions', '/users', '/forum-manage', '/banners', '/statistics']
  return adminRoutes.some(path => route.path.startsWith(path))
})

// 判断用户是否为管理员角色
const isAdminRole = computed(() => {
  const role = user.value.role ? user.value.role.toUpperCase() : ''
  return role === 'ADMIN'
})

const sidebarCollapsed = ref(false)

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const username = computed(() => user.value.username || '')
const nickname = computed(() => user.value.nickname || '')

const navItems = [
  { path: '/home', label: '首页', icon: 'HomeFilled' },
  { path: '/forum', label: '驴友广场', icon: 'Compass' },
  { path: '/my-itinerary', label: '我的行程', icon: 'Calendar' },
  { path: '/favorites', label: '我的收藏', icon: 'Star' },
  { path: '/profile', label: '个人中心', icon: 'UserFilled' }
]

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    sessionStorage.removeItem('user_info')
    ElMessage.success('已退出登录')
    window.location.href = '/login'
  }).catch(() => {})
}

const handleBackToHome = () => {
  ElMessageBox.confirm('确定要返回前台首页吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 使用 window.location.href 强制刷新页面，确保布局重新计算
    window.location.href = '/home'
  }).catch(() => {})
}
</script>

<style>

body { 
  margin: 0; 
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; 
  background: #FFF5F0;
}

.app-container {
  height: 100vh;
}

/* ========== 导航栏样式 ========== */
.nav-header {
  position: relative;
  height: 64px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 0;
  overflow: hidden;
  z-index: 100;
}

.admin-header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.header-noise {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

.header-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    rgba(231, 111, 81, 0.03) 0%, 
    transparent 30%,
    transparent 70%,
    rgba(244, 162, 97, 0.03) 100%
  );
  pointer-events: none;
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 24px;
  pointer-events: auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.brand:hover {
  transform: scale(1.02);
}

.brand-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border-radius: 10px;
  color: white;
}

.brand-icon svg {
  width: 20px;
  height: 20px;
}

.brand-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: #264653;
  letter-spacing: -0.5px;
}

.admin-tag {
  background: rgba(231, 111, 81, 0.12);
  color: #E76F51;
  border: 1px solid rgba(231, 111, 81, 0.25);
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  z-index: 10;
  pointer-events: auto;
}

/* 用户导航菜单 */
.user-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  position: relative;
  z-index: 10;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #5a6c7d;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  background: transparent;
  border: none;
  text-decoration: none;
  position: relative;
  z-index: 10;
}

.nav-item:hover {
  background: rgba(231, 111, 81, 0.08);
  color: #E76F51;
}

.nav-item.active {
  background: rgba(231, 111, 81, 0.12);
  color: #E76F51;
}

.nav-item .el-icon {
  font-size: 16px;
}

/* 用户面板 */
.user-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 8px 6px 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.user-panel:hover {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.user-avatar {
  border: 2px solid #F4A261;
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.user-panel:hover .user-avatar {
  transform: scale(1.05);
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #264653;
  line-height: 1.2;
}

.user-role {
  font-size: 11px;
  color: #5a6c7d;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  background: rgba(231, 111, 81, 0.08);
  border: 1px solid rgba(231, 111, 81, 0.2);
  border-radius: 20px;
  color: #E76F51;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.logout-btn:hover {
  background: #E76F51;
  border-color: #E76F51;
  color: white;
  transform: translateY(-1px);
}

/* ========== 管理员侧边栏 ========== */
.admin-sidebar {
  position: relative;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: width 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  overflow-y: auto;
}

.sidebar-content::-webkit-scrollbar {
  display: none;
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.sidebar-footer {
  flex-shrink: 0;
  padding-bottom: 16px;
}

.sidebar-noise {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0.05;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

.sidebar-logo {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  background: transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  max-width: 200px;
}

.sidebar-header {
  position: relative;
  z-index: 1;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.3);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-header.collapsed {
  justify-content: center;
  padding: 0;
}

.sidebar-header.collapsed .sidebar-logo {
  max-width: 0;
  opacity: 0;
}

.sidebar-logo .logo-icon {
  min-width: 40px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-logo .logo-text {
  white-space: nowrap;
  opacity: 1;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.collapse-btn {
  background: rgba(38, 70, 83, 0.1);
  border: none;
  color: #264653;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.collapse-btn:hover {
  background: rgba(231, 111, 81, 0.2);
  color: #E76F51;
  transform: scale(1.1);
}

.logo-icon {
  min-width: 40px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E76F51, #F4A261);
  border-radius: 12px;
  color: white;
  font-size: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-logo .logo-text {
  color: #264653;
  font-size: 1.1rem;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.admin-menu {
  position: relative;
  z-index: 1;
  border-right: none;
  padding: 16px 12px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  flex: 1;
  overflow-y: auto;
}

.admin-menu::-webkit-scrollbar {
  display: none;
}

.admin-menu:not(.el-menu--collapse) {
  width: 216px;
}

/* 菜单收起时的样式 */
.admin-menu.el-menu--collapse {
  width: 64px;
}

.admin-menu.el-menu--collapse .el-menu-item {
  padding: 0 20px !important;
}

.admin-menu.el-menu--collapse .el-icon {
  margin-right: 0;
}

.menu-item {
  margin-bottom: 4px;
  border-radius: 10px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(231, 111, 81, 0.1), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.menu-item:hover::before {
  transform: translateX(100%);
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(4px);
}

.menu-item.is-active {
  background: linear-gradient(90deg, rgba(231, 111, 81, 0.2) 0%, rgba(231, 111, 81, 0.08) 100%);
}

.menu-item.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: linear-gradient(180deg, #E76F51, #F4A261);
  border-radius: 0 4px 4px 0;
}

.menu-item.is-active .el-icon {
  color: #E76F51;
}

.menu-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 16px 8px;
}

.back-home {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 4px 12px;
  border-radius: 10px;
  color: #264653;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  height: 44px;
  box-sizing: border-box;
}

.back-home::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(231, 111, 81, 0.1), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.back-home:hover::before {
  transform: translateX(100%);
}

.back-home:hover {
  background: rgba(231, 111, 81, 0.15);
  color: #E76F51;
  transform: translateX(4px);
}

.back-home .el-icon {
  font-size: 18px;
  min-width: 18px;
  width: 18px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-home span {
  font-size: 14px;
  white-space: nowrap;
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ========== 主内容区 ========== */
.app-main-content {
  position: relative;
  background: #FFF5F0;
  padding: 0;
  overflow-y: auto;
}

.admin-main-content {
  position: relative;
  background: #FFF5F0;
  padding: 0;
  overflow-y: auto;
}

.admin-main-content::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0.025;
  z-index: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

.admin-main-content .page-header h2,
.admin-main-content .header-content h2 {
  color: #264653;
}

.admin-main-content .header-content p {
  color: #5a6c7d;
}

.admin-main-content .table-container,
.admin-main-content .chart-card,
.admin-main-content .stat-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.admin-main-content .page-header,
.admin-main-content .header-content h2,
.admin-main-content .header-content p {
  color: #264653;
}

.admin-main-content .stat-card .stat-label {
  color: #5a6c7d;
}

.admin-main-content .stat-card .stat-number {
  color: #264653;
}

.full-screen { 
  height: 100vh; 
  width: 100vw; 
}

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .user-nav {
    display: none;
  }
  
  .user-detail {
    display: none;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .brand-text {
    display: none;
  }
}
/* 管理后台入口的特殊样式 */
.admin-nav-portal {
  margin-left: 12px;
  background: rgba(231, 111, 81, 0.1) !important;
  color: #E76F51 !important;
  border: 1px solid rgba(231, 111, 81, 0.2) !important;
  font-weight: 600 !important;
}

.admin-nav-portal:hover {
  background: #E76F51 !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(231, 111, 81, 0.2);
}

/* 确保在窄屏幕下也隐藏，防止导航栏挤爆 */
@media (max-width: 1200px) {
  .admin-nav-portal span {
    display: none; /* 屏幕稍小时只显示图标 */
  }
}
</style>
