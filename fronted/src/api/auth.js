import api from './index'

export const authApi = {
  // 用户注册
  register(data) {
    return api.post('/auth/register', data)
  },
  // 用户登录
  login(data) {
    return api.post('/auth/login', data)
  },
  // 获取用户信息
  getProfile() {
    return api.get('/user/profile')
  },
  // 更新用户信息
  updateProfile(data) {
    return api.put('/user/profile', data)
  },
  // 发送验证码
  sendCode(data) {
    return api.post('/auth/send-code', data)
  }
}
