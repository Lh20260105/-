import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由配置说明：
 * 1. isPublic: true  -> 游客可访问，App.vue 不会显示导航栏
 * 2. requiresAuth: true -> 必须登录后才能访问
 * 3. requiresAdmin: true -> 必须是管理员 (ADMIN) 才能访问
 */
const routes = [
  { 
    path: '/', 
    redirect: '/login' 
  },
  
  // --- 公共页面 (无需登录) ---
  { 
    path: '/login', 
    name: 'Login', 
    component: () => import('../views/Login.vue'),
    meta: { isPublic: true }
  },
  { 
    path: '/register', 
    name: 'Register', 
    component: () => import('../views/Register.vue'),
    meta: { isPublic: true }
  },

  // --- 普通用户页面 (需要登录) ---
  { 
    path: '/home', 
    name: 'UserHome', 
    component: () => import('../views/UserHome.vue'),
    meta: { requiresAuth: true } 
  },
  {
    path: '/my-itinerary',
    name: 'MyItinerary',
    component: () => import('../views/MyItinerary.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/profile', 
    name: 'Profile', 
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true }
  },

  // --- 管理员页面 (需要登录 + ADMIN 权限) ---
  { 
    path: '/attractions', 
    name: 'Attractions', 
    component: () => import('../views/AttractionManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true } 
  },
  { 
    path: '/users', 
    name: 'Users', 
    component: () => import('../views/UserManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true } 
  },
  { 
    path: '/statistics', 
    name: 'Statistics', 
    component: () => import('../views/Statistics.vue'),
    meta: { requiresAuth: true, requiresAdmin: true } 
  },

  // --- 404 兜底 (如果用户输入了不存在的地址，跳转回首页) ---
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  },// ... 其他导入
{
  path: '/attraction/:id', // :id 是一个占位符
  name: 'AttractionDetail',
  component: () => import('../views/AttractionDetail.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/favorites',
  name: 'MyFavorite',
  component: () => import('../views/MyFavorite.vue'),
  meta: { requiresAuth: true }
},
{
  path: '/profile',
  name: 'UserProfile',
  component: () => import('../views/UserProfile.vue'),
  meta: { requiresAuth: true }
}
// ...
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// --- 全量智能路由守卫 ---
router.beforeEach((to, from, next) => {
  // 1. 获取用户信息并防御性处理 (防止 JSON 解析报错)
  let userInfo = {}
  try {
    userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
  } catch (e) {
    userInfo = {}
  }
  
  const isLogin = !!userInfo.username // 判断是否已登录

  // 2. 如果去的是公共页面 (登录/注册)，直接放行
  if (to.meta.isPublic) {
    next()
    return
  }

  // 3. 如果没登录，却想去需要权限的页面，直接踢回登录页
  if (!isLogin) {
    next('/login')
    return
  }

  // 4. 如果是管理员专区，检查角色权限
  if (to.meta.requiresAdmin && userInfo.role !== 'ADMIN') {
    // 这里使用 alert 是最直接的提示方式
    alert('权限不足！该区域仅限管理员进入')
    next('/home') // 踢回到普通用户首页
    return
  }

  // 5. 所有检查通过，放行
  next()
})

export default router