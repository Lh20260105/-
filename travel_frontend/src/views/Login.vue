<template>
  <div class="login-page">
    <!-- 噪点纹理背景 -->
    <div class="noise-overlay"></div>
    
    <!-- 不规则渐变背景 -->
    <div class="gradient-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <!-- 主容器 - 与注册页镜像的不对称布局 -->
    <div class="main-container">
      <!-- 左侧视觉区 - 占 58%，更宽 -->
      <div class="visual-section">
        <div class="visual-content">
          <div class="tag">欢迎回来</div>
          <h1 class="main-title">
            <span class="line">继续</span>
            <span class="line accent">探索之旅</span>
          </h1>
          <p class="subtitle">thousands of adventures await you</p>
          
          <!-- 装饰性图片网格 -->
          <div class="image-grid">
            <div class="grid-item tall" style="background-image: url('https://picsum.photos/300/400?random=7')"></div>
            <div class="grid-item wide" style="background-image: url('https://picsum.photos/400/200?random=8')"></div>
            <div class="grid-item square" style="background-image: url('https://picsum.photos/200/200?random=9')"></div>
          </div>
          
          <!-- 统计数据 -->
          <div class="stats">
            <div class="stat-item">
              <span class="stat-number">10K+</span>
              <span class="stat-label">探索者</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">500+</span>
              <span class="stat-label">目的地</span>
            </div>
          </div>
        </div>
        
        <!-- 斜切分割线 -->
        <div class="diagonal-line"></div>
      </div>

      <!-- 右侧表单区 - 占 42% -->
      <div class="form-section">
        <div class="form-container">
          <div class="form-body">
            <h2 class="form-title">登录账号</h2>
            <p class="form-desc">输入信息，继续旅程</p>

            <el-form 
              :model="loginForm" 
              :rules="rules" 
              ref="loginRef"
              label-width="0" 
              class="login-form"
              @submit.prevent="handleLogin"
            >
              <el-form-item prop="username">
                <div class="custom-input">
                  <label>用户名</label>
                  <input 
                    v-model="loginForm.username" 
                    type="text" 
                    placeholder="输入用户名"
                    @focus="handleFocus"
                    @blur="handleBlur"
                  />
                  <span class="input-line"></span>
                </div>
              </el-form-item>
              
              <el-form-item prop="password">
                <div class="custom-input">
                  <label>密码</label>
                  <input 
                    v-model="loginForm.password" 
                    type="password" 
                    placeholder="输入密码"
                    @focus="handleFocus"
                    @blur="handleBlur"
                    @keyup.enter="handleLogin"
                  />
                  <span class="input-line"></span>
                </div>
              </el-form-item>
              
              <div class="form-options">
                <label class="remember-me">
                  <input type="checkbox" v-model="rememberMe" />
                  <span class="checkmark"></span>
                  记住我
                </label>
                <a class="forgot-link">忘记密码？</a>
              </div>
              
              <button 
                type="button" 
                class="submit-btn"
                @click="handleLogin"
                :disabled="loading"
              >
                <span class="btn-text">{{ loading ? '登录中...' : '立即登录' }}</span>
                <span class="btn-arrow">→</span>
              </button>
            </el-form>

            <div class="form-footer">
              <span class="footer-text">还没有账号？</span>
              <a class="footer-link" @click="$router.push('/register')">立即注册</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { updateGlobalUser } from '../utils/userBus'

const router = useRouter()
const loading = ref(false)
const rememberMe = ref(false)
const loginRef = ref(null)

const loginForm = ref({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 输入框动画
const handleFocus = (e) => {
  e.target.parentElement.classList.add('focused')
}

const handleBlur = (e) => {
  if (!e.target.value) {
    e.target.parentElement.classList.remove('focused')
  }
}

const handleLogin = () => {
  loginRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await request.post('/users/login', loginForm.value)
      if (res.data.success) {
        ElMessage.success('登录成功！')
        const user = res.data.data
        localStorage.setItem('token', user.username)
        localStorage.setItem('user', JSON.stringify(user))
        sessionStorage.setItem('user_info', JSON.stringify(user))
        updateGlobalUser(user)
        
        const targetPath = user.role === 'ADMIN' ? '/attractions' : '/home'
        setTimeout(() => router.push(targetPath), 1000)
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (error) {
      ElMessage.error('登录失败，请检查网络连接')
    } finally {
      loading.value = false
    }
  })
}

// 入场动画
onMounted(() => {
  document.querySelector('.visual-section').classList.add('animate-in')
  document.querySelector('.form-section').classList.add('animate-in')
})
</script>

<style scoped>
/* 基础设置 - 使用背景色 #FFF5F0 */
.login-page {
  height: 100vh;
  width: 100vw;
  position: relative;
  overflow: hidden;
  background: #FFF5F0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 噪点纹理 */
.noise-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1000;
  opacity: 0.04;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

/* 不规则渐变背景 - 使用主色 #E76F51 和辅助色 #F4A261 */
.gradient-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.2;
}

.blob-1 {
  width: 550px;
  height: 550px;
  background: #F4A261;
  top: -150px;
  left: -100px;
  animation: blobMove1 22s infinite;
}

.blob-2 {
  width: 450px;
  height: 450px;
  background: #E76F51;
  bottom: -100px;
  left: 10%;
  animation: blobMove2 28s infinite;
}

.blob-3 {
  width: 350px;
  height: 350px;
  background: #E76F51;
  top: 50%;
  left: 25%;
  animation: blobMove3 20s infinite;
}

@keyframes blobMove1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(40px, -40px) scale(1.15); }
  66% { transform: translate(-30px, 30px) scale(0.9); }
}

@keyframes blobMove2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(50px, -30px) scale(0.85); }
  66% { transform: translate(-30px, 50px) scale(1.1); }
}

@keyframes blobMove3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, -20px) scale(1.2); }
}

/* 主容器 - 与注册页镜像的不对称布局 */
.main-container {
  display: flex;
  height: 100vh;
  position: relative;
  z-index: 1;
}

/* 左侧视觉区 - 58%，更宽 */
.visual-section {
  width: 58%;
  position: relative;
  display: flex;
  align-items: center;
  padding: 40px 50px;
  opacity: 0;
  transform: translateX(-30px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
}

.visual-section.animate-in {
  opacity: 1;
  transform: translateX(0);
}

.visual-content {
  max-width: 540px;
}

/* Tag 使用主色 #E76F51 */
.tag {
  display: inline-block;
  padding: 8px 16px;
  background: rgba(231, 111, 81, 0.12);
  color: #E76F51;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  text-transform: uppercase;
  border-radius: 20px;
  margin-bottom: 24px;
  border: 1px solid rgba(231, 111, 81, 0.3);
}

/* 标题使用文字色 #264653 */
.main-title {
  font-size: 42px;
  font-weight: 800;
  line-height: 1.1;
  color: #264653;
  margin: 0 0 12px 0;
  letter-spacing: -1px;
}

.main-title .line {
  display: block;
}

/* 强调色 #2A9D8F 用于 accent */
.main-title .accent {
  color: #2A9D8F;
  position: relative;
}

.main-title .accent::after {
  content: '';
  position: absolute;
  bottom: 6px;
  left: 0;
  width: 100%;
  height: 12px;
  background: rgba(42, 157, 143, 0.15);
  z-index: -1;
}

.subtitle {
  font-size: 14px;
  color: #5a6c7d;
  margin: 0 0 24px 0;
  font-weight: 400;
}

/* 图片网格 */
.image-grid {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  grid-template-rows: 1fr 1.2fr;
  gap: 10px;
  height: 200px;
  margin-bottom: 24px;
}

.grid-item {
  border-radius: 16px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
}

.grid-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(231, 111, 81, 0.08) 0%, transparent 100%);
}

.grid-item.tall {
  grid-row: span 2;
}

.grid-item.wide {
  grid-column: span 1;
}

/* 统计数据 */
.stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #264653;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #5a6c7d;
  margin-top: 4px;
}

/* 斜切分割线 */
.diagonal-line {
  position: absolute;
  right: 0;
  top: 0;
  width: 120px;
  height: 100%;
  background: linear-gradient(-75deg, transparent 49%, rgba(231, 111, 81, 0.06) 50%, transparent 51%);
}

/* 右侧表单区 - 42% */
.form-section {
  width: 42%;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 40px 50px;
  opacity: 0;
  transform: translateX(30px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.15s;
  overflow: hidden;
}

.form-section.animate-in {
  opacity: 1;
  transform: translateX(0);
}

/* 玻璃态表单容器 - 与注册页一致 */
.form-container {
  width: 100%;
  max-width: 380px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px 48px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 
    0 20px 60px rgba(231, 111, 81, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

/* 表单标题使用文字色 #264653 */
.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #264653;
  margin: 0 0 6px 0;
}

.form-desc {
  font-size: 13px;
  color: #5a6c7d;
  margin: 0 0 20px 0;
}

/* 自定义输入框 */
.custom-input {
  position: relative;
  margin-bottom: 4px;
}

.custom-input label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: #5a6c7d;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 4px;
  transition: color 0.3s;
}

.custom-input input {
  width: 100%;
  height: 40px;
  background: transparent;
  border: none;
  border-bottom: 2px solid #e0d0c8;
  font-size: 15px;
  color: #264653;
  outline: none;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  padding: 0;
}

.custom-input input::placeholder {
  color: #b0a5a0;
}

.custom-input .input-line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #E76F51;
  transition: width 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.custom-input.focused label {
  color: #E76F51;
}

.custom-input.focused .input-line {
  width: 100%;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 20px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #5a6c7d;
  font-size: 14px;
  cursor: pointer;
}

.remember-me input {
  display: none;
}

.checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid #d0c5c0;
  border-radius: 4px;
  position: relative;
  transition: all 0.3s;
}

.remember-me input:checked + .checkmark {
  background: #2A9D8F;
  border-color: #2A9D8F;
}

.remember-me input:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
}

/* 忘记密码链接 - 使用强调色 #2A9D8F */
.forgot-link {
  color: #2A9D8F;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  position: relative;
}

.forgot-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #2A9D8F;
  transition: width 0.3s;
}

.forgot-link:hover::after {
  width: 100%;
}

/* 提交按钮 - 使用主色 #E76F51 */
.submit-btn {
  width: 100%;
  height: 48px;
  background: #E76F51;
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: #F4A261;
  transition: left 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 0;
}

.submit-btn:hover::before {
  left: 0;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(231, 111, 81, 0.35);
}

.submit-btn span {
  position: relative;
  z-index: 1;
}

.btn-arrow {
  transition: transform 0.3s;
}

.submit-btn:hover .btn-arrow {
  transform: translateX(4px);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.submit-btn:disabled::before {
  display: none;
}

/* 表单底部 */
.form-footer {
  margin-top: 20px;
  text-align: center;
}

.footer-text {
  color: #5a6c7d;
  font-size: 14px;
}

/* 底部链接 - 使用强调色 #2A9D8F */
.footer-link {
  color: #2A9D8F;
  font-weight: 600;
  cursor: pointer;
  margin-left: 8px;
  position: relative;
}

.footer-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #2A9D8F;
  transition: width 0.3s;
}

.footer-link:hover::after {
  width: 100%;
}

/* 响应式 */
@media (max-width: 1024px) {
  .visual-section {
    width: 52%;
    padding: 40px;
  }
  
  .form-section {
    width: 48%;
    padding: 40px;
  }
  
  .main-title {
    font-size: 42px;
  }
  
  .image-grid {
    height: 180px;
  }
}

@media (max-width: 768px) {
  .main-container {
    flex-direction: column;
  }
  
  .visual-section {
    display: none;
  }
  
  .form-section {
    width: 100%;
    padding: 24px;
    justify-content: center;
  }
  
  .form-container {
    max-width: 100%;
    padding: 32px;
  }
}
</style>
