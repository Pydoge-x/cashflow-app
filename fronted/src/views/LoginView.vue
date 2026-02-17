<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <span class="auth-logo">💰</span>
          <h1>CashFlow</h1>
          <p>个人（家庭）财务管理系统</p>
        </div>

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="form-group">
            <label>手机号 / 邮箱</label>
            <input
              v-model="form.account"
              type="text"
              placeholder="请输入手机号或邮箱"
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
            {{ loading ? "登录中..." : "登 录" }}
          </button>
        </form>

        <div class="auth-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

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
    error.value = e.response?.data?.message || "登录失败，请检查账号密码";
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
  background: var(--color-bg);
  background-image:
    radial-gradient(
      ellipse at 20% 50%,
      rgba(99, 102, 241, 0.08) 0%,
      transparent 50%
    ),
    radial-gradient(
      ellipse at 80% 20%,
      rgba(139, 92, 246, 0.06) 0%,
      transparent 50%
    );
  padding: 1rem;
}

.auth-container {
  width: 100%;
  max-width: 420px;
}

.auth-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: 2.5rem 2rem;
  box-shadow: var(--shadow-lg);
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
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.3rem;
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
