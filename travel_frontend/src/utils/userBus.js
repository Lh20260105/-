import { reactive } from 'vue'

// 创建一个全局共享的响应式对象
export const userState = reactive({
  userInfo: JSON.parse(sessionStorage.getItem('user_info') || '{}')
})

// 提供一个方法来更新这个对象
export const updateGlobalUser = (newInfo) => {
  userState.userInfo = { ...newInfo }
  // 同时同步到缓存，防止刷新页面丢失
  sessionStorage.setItem('user_info', JSON.stringify(newInfo))
}