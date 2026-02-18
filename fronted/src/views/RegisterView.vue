<template>
  <div class="celestial-container">
    <div class="celestial-stars"></div>
    <WealthParticles :count="60" />
    <div class="auth-page">
      <div class="auth-container">
        <div class="auth-card auth-glass-card">
          <div class="auth-header">
            <span class="auth-logo">✨</span>
            <h1>加入星际财富</h1>
            <p>开启你的星辰财富之旅</p>
          </div>

          <form @submit.prevent="handleRegister" class="auth-form">
            <div class="form-group">
              <label>用户名</label>
              <input
                v-model="form.username"
                type="text"
                placeholder="请输入用户名"
                required
              />
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>手机号</label>
                <input v-model="form.phone" type="tel" placeholder="选填" />
              </div>
              <div class="form-group">
                <label>邮箱</label>
                <input v-model="form.email" type="email" placeholder="选填" />
              </div>
            </div>
            <div class="form-group">
              <label>密码</label>
              <input
                v-model="form.password"
                type="password"
                placeholder="请输入密码 (至少6位)"
                required
                minlength="6"
              />
            </div>
            <div class="form-group">
              <label>确认密码</label>
              <input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                required
              />
            </div>

            <div v-if="error" class="auth-error">{{ error }}</div>
            <div v-if="success" class="auth-success">{{ success }}</div>

            <button
              type="submit"
              class="btn btn-primary btn-block"
              :disabled="loading"
            >
              {{ loading ? "正在同步星图..." : "即 刻 起 航" }}
            </button>
          </form>

          <div class="auth-footer">
            已有账号？<router-link to="/login">返回登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import WealthParticles from "../components/WealthParticles.vue";

const router = useRouter();
const authStore = useAuthStore();

const form = ref({
  username: "",
  phone: "",
  email: "",
  password: "",
  confirmPassword: "",
});
const loading = ref(false);
const error = ref("");
const success = ref("");

async function handleRegister() {
  error.value = "";
  success.value = "";

  if (!form.value.phone && !form.value.email) {
    error.value = "手机号和邮箱至少填写一个";
    return;
  }
  if (form.value.password !== form.value.confirmPassword) {
    error.value = "两次输入的密码不一致";
    return;
  }

  loading.value = true;
  try {
    await authStore.register(form.value);
    success.value = "注册成功！即将跳转到登录...";
    setTimeout(() => router.push("/login"), 1500);
  } catch (e) {
    error.value = e.response?.data?.message || "注册失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  position: relative;
  z-index: 2;
  background: transparent;
}

.auth-container {
  width: 100%;
  max-width: 480px;
}

.auth-card {
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-xl);
  padding: 3rem 2.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 1;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.auth-logo {
  font-size: 3rem;
  display: block;
  margin-bottom: 0.8rem;
}

.auth-header h1 {
  font-size: 2.25rem;
  font-weight: 800;
  background: linear-gradient(135deg, #fff 0%, #94a3b8 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
  letter-spacing: -0.02em;
}

.auth-header p {
  color: var(--color-text-muted);
  font-size: 0.88rem;
}

.auth-form {
  margin-bottom: 1.5rem;
}

.auth-error {
  background: var(--color-danger-bg);
  color: var(--color-danger);
  padding: 0.6rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.82rem;
  margin-bottom: 1rem;
}

.auth-success {
  background: var(--color-success-bg);
  color: var(--color-success);
  padding: 0.6rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.82rem;
  margin-bottom: 1rem;
}

.auth-footer {
  text-align: center;
  font-size: 0.88rem;
  color: var(--color-text-muted);
}

.auth-footer a {
  font-weight: 600;
}
</style>
