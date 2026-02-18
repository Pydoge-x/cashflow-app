<template>
  <div class="celestial-container">
    <div class="celestial-stars"></div>
    <WealthParticles :count="60" />
    <div class="auth-page">
      <div class="auth-container">
        <div class="auth-card auth-glass-card">
          <div class="auth-header">
            <span class="auth-logo">⭐</span>
            <h1>CashFlow</h1>
            <p>星辰大海，财富领航</p>
          </div>

          <form @submit.prevent="handleLogin" class="auth-form">
            <div class="form-group">
              <label>邮箱</label>
              <input
                v-model.trim="form.account"
                type="text"
                placeholder="请输入邮箱"
                required
              />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                required
              />
            </div>

            <div v-if="error" class="auth-error">{{ error }}</div>

            <button
              type="submit"
              class="btn btn-primary btn-block"
              :disabled="loading"
            >
              {{ loading ? "正在进入星系..." : "登 录" }}
            </button>
          </form>

          <div class="auth-footer">
            还没有账号？<router-link to="/register">立即注册</router-link>
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

const form = ref({ account: "", password: "" });
const loading = ref(false);
const error = ref("");

async function handleLogin() {
  error.value = "";
  loading.value = true;
  try {
    await authStore.login(form.value);
    router.push("/");
  } catch (e) {
    error.value = "登录失败，请检查账号密码";
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
  max-width: 420px;
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

.auth-footer {
  text-align: center;
  font-size: 0.88rem;
  color: var(--color-text-muted);
}

.auth-footer a {
  font-weight: 600;
}
</style>
