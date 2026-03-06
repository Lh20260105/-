import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由配置说明：
 * 1. isPublic: true     -> 游客可访问
 * 2. requiresAuth: true -> 必须登录后才能访问
 * 3. requiresAdmin: true -> 必须是管理员 (ADMIN) 才能访问
 */
const routes = [
  { 
    path: '/', 
    redirect: '/login' 
  },
  
  // --- 1. 公共页面 (无需登录) ---
  { 
    path: '/login', 
    name: 'Login', 
    component: () => import('../views/Login.vue'),
    meta: { isPublic: true, title: '登录 - 云游推荐系统' }
  },
  { 
    path: '/register', 
    name: 'Register', 
    component: () => import('../views/Register.vue'),
    meta: { isPublic: true, title: '注册 - 云游推荐系统' }
  },

  // --- 2. 普通用户页面 (需要登录) ---
  { 
    path: '/home', 
    name: 'UserHome', 
    component: () => import('../views/UserHome.vue'),
    meta: { requiresAuth: true, title: '首页' } 
  },
  {
    path: '/my-itinerary',
    name: 'MyItinerary',
    component: () => import('../views/MyItinerary.vue'),
    meta: { requiresAuth: true, title: '我的行程单' }
  },
  {
    path: '/forum',
    name: 'Forum',
    component: () => import('../views/Forum.vue'),
    meta: { requiresAuth: true, title: '驴友广场' }
  },
  // 帖子详情页也属于需要登录的页面
  {
    path: '/post/:id', 
    name: 'PostDetail',
    component: () => import('../views/PostDetail.vue'),
    meta: { requiresAuth: true, title: '帖子详情' }
  },
  {
    path: '/favorites',
    name: 'MyFavorite',
    component: () => import('../views/MyFavorite.vue'),
    meta: { requiresAuth: true, title: '我的收藏' }
  },
  {
    path: '/attraction/:id',
    name: 'AttractionDetail',
    component: () => import('../views/AttractionDetail.vue'),
    meta: { requiresAuth: true, title: '景点详情' }
  },
  { 
    path: '/profile', 
    name: 'UserProfile', 
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true, title: '个人中心' }
  },

  // --- 3. 管理员页面 (需要登录 + ADMIN 权限) ---
  { 
    path: '/attractions', 
    name: 'AttractionManage', 
    component: () => import('../views/AttractionManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '景点管理' } 
  },
  { 
    path: '/users', 
    name: 'UserManage', 
    component: () => import('../views/UserManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '用户管理' } 
  },
  { 
    path: '/forum-manage', 
    name: 'ForumManage', 
    component: () => import('../views/ForumManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '论坛管理' } 
  },
  { 
    path: '/banners', 
    name: 'BannerManage', 
    component: () => import('../views/BannerManage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '轮播图管理' } 
  },
  { 
    path: '/statistics', 
    name: 'Statistics', 
    component: () => import('../views/Statistics.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: '数据统计' } 
  },

  // --- 4. 404 兜底 ---
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// --- 全量智能路由守卫 ---
router.beforeEach((to, from, next) => {
  // 1. 统一从 sessionStorage 获取用户信息
  let userInfo = {}
  try {
    userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  } catch (e) {
    userInfo = {}
  }
  
  const isLogin = !!userInfo.username // 判断是否已登录

  // 2. 动态设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }

  // 3. 公共页面直接放行
  if (to.meta.isPublic) {
    next()
    return
  }

  // 4. 未登录拦截
  if (!isLogin) {
    next('/login')
    return
  }

  // 5. 管理员权限检查
  if (to.meta.requiresAdmin && userInfo.role !== 'ADMIN') {
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.error('权限不足！该区域仅限管理员进入')
    })
    next('/home') 
    return
  }

  // 6. 验证通过
  next()
})

export default router