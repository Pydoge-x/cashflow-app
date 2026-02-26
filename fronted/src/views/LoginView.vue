<template>
  <div class="golden-container">
    <div class="golden-pattern"></div>
    <!-- Aurora background layer -->
    <div class="aurora-layer">
      <div class="aurora-glow aurora-1"></div>
      <div class="aurora-glow aurora-2"></div>
      <div class="aurora-glow aurora-3"></div>
    </div>
    <WealthParticles :count="60" />
    <div class="auth-page">
      <div class="auth-card glass-card spotlight-card entrance-anim">
        <div class="auth-header">
          <!-- Lucide Wallet icon instead of emoji -->
          <div class="auth-logo-wrap shimmer-btn">
            <Wallet :size="36" class="auth-logo-icon" />
          </div>
          <h1 class="gold-text">智慧家庭财务助手</h1>
          <p>智慧理财，最懂您的家庭助手</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin" class="auth-form">
          <el-form-item prop="account">
            <el-input
              v-model.trim="form.account"
              placeholder="请输入邮箱"
              size="large"
            >
              <template #prefix>
                <Mail :size="16" class="input-icon" />
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
                <Lock :size="16" class="input-icon" />
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

          <button
            :disabled="loading"
            @click="handleLogin"
            class="login-btn shimmer-btn"
          >
            <Loader2 v-if="loading" :size="18" class="spin-icon" />
            <span>{{ loading ? "正在登录..." : "登 录" }}</span>
          </button>
        </el-form>

        <div class="auth-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import WealthParticles from "../components/WealthParticles.vue";
import { Wallet, Mail, Lock, Loader2 } from 'lucide-vue-next';

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
.aurora-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
  background: radial-gradient(circle at 50% 50%, #fff 0%, #fdfdfd 100%);
}

.aurora-glow {
  position: absolute;
  filter: blur(80px);
  opacity: 0.4;
  border-radius: 50%;
  mix-blend-mode: multiply;
}

.aurora-1 {
  width: 600px;
  height: 600px;
  background: #D4AF37;
  top: -200px;
  left: -150px;
  animation: aurora-move-1 20s infinite alternate;
}

.aurora-2 {
  width: 500px;
  height: 500px;
  background: #E8D5A3;
  bottom: -150px;
  right: -100px;
  animation: aurora-move-2 25s infinite alternate;
}

.aurora-3 {
  width: 400px;
  height: 400px;
  background: #D4AF37;
  top: 40%;
  right: 10%;
  animation: aurora-move-3 18s infinite alternate;
}

@keyframes aurora-move-1 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(100px, 50px) scale(1.1); }
}

@keyframes aurora-move-2 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(-80px, -30px) scale(1.2); }
}

@keyframes aurora-move-3 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(-50px, 40px) scale(1.1); }
}

.entrance-anim {
  animation: auth-entrance 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes auth-entrance {
  from { opacity: 0; transform: translateY(30px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

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
  max-width: 440px;
  border-radius: 28px;
  padding: 44px 36px;
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo-wrap {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 32px rgba(212, 175, 55, 0.4);
  animation: glow-pulse 3s ease-in-out infinite;
}

.auth-logo-icon {
  color: #fff;
}

.auth-header h1 {
  font-size: 1.75rem;
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

.input-icon {
  color: #909399;
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spin-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  text-align: center;
  font-size: 0.88rem;
  color: #909399;
}

.auth-footer a {
  font-weight: 700;
  color: #D4AF37;
  text-decoration: none;
  transition: color 0.2s;
}

.auth-footer a:hover {
  color: #B8860B;
}
</style>
