<template>
  <div class="golden-container">
    <div class="golden-pattern"></div>
    <WealthParticles :count="60" />
    <div class="auth-page">
      <el-card class="auth-card auth-glass-card" shadow="hover">
        <div class="auth-header">
          <span class="auth-logo">💰</span>
          <h1>CashFlow</h1>
          <p>智慧理财，掌控未来</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin" class="auth-form">
          <el-form-item prop="account">
            <el-input
              v-model.trim="form.account"
              placeholder="请输入邮箱"
              size="large"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-alert
            v-if="error"
            :title="error"
            type="error"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          />

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            {{ loading ? "正在登录..." : "登 录" }}
          </el-button>
        </el-form>

        <div class="auth-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import WealthParticles from "../components/WealthParticles.vue";
import { Message, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref(null);

const form = reactive({ account: "", password: "" });
const loading = ref(false);
const error = ref("");

const rules = {
  account: [{ required: true, message: "请输入邮箱", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  
  error.value = "";
  loading.value = true;
  try {
    await authStore.login(form);
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
  padding: 24px;
  position: relative;
  z-index: 2;
  background: transparent;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  border-radius: 24px;
}

.auth-card :deep(.el-card__body) {
  padding: 40px 32px;
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo {
  font-size: 3rem;
  display: block;
  margin-bottom: 12px;
}

.auth-header h1 {
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.auth-header p {
  color: #909399;
  font-size: 0.9rem;
}

.auth-form {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
}

.auth-footer {
  text-align: center;
  font-size: 0.88rem;
  color: #909399;
}

.auth-footer a {
  font-weight: 600;
  color: #D4AF37;
}
</style>
