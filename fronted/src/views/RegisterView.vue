<template>
  <div class="golden-container">
    <div class="golden-pattern"></div>
    <div class="aurora-layer">
      <div class="aurora-glow aurora-1"></div>
      <div class="aurora-glow aurora-2"></div>
      <div class="aurora-glow aurora-3"></div>
    </div>
    <WealthParticles :count="60" />
    <div class="auth-page">
      <div class="auth-card glass-card spotlight-card entrance-anim">
        <div class="auth-header">
          <div class="auth-logo-wrap shimmer-btn">
            <Sparkles :size="34" class="auth-logo-icon" />
          </div>
          <h1 class="gold-text">加入 智慧家庭财务助手</h1>
          <p>开启您的智慧理财之旅</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister" class="auth-form">
          <el-radio-group v-model="registrationMethod" class="registration-toggle">
            <el-radio-button value="EMAIL">邮箱注册</el-radio-button>
          </el-radio-group>

          <el-form-item prop="username">
            <el-input v-model.trim="form.username" placeholder="请输入用户名" size="large">
              <template #prefix><User :size="16" class="input-icon" /></template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="registrationMethod === 'EMAIL'" prop="email">
            <el-input v-model.trim="form.email" type="email" placeholder="请输入常用邮箱" size="large">
              <template #prefix><Mail :size="16" class="input-icon" /></template>
            </el-input>
          </el-form-item>

          <el-form-item v-else prop="phone">
            <el-input v-model.trim="form.phone" type="tel" placeholder="请输入手机号" size="large">
              <template #prefix><Phone :size="16" class="input-icon" /></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="code">
            <div class="code-input-group">
              <el-input v-model.trim="form.code" placeholder="6位验证码" size="large" maxlength="6" style="flex: 1" />
              <button
                type="button"
                :disabled="cooldown > 0 || isSendingCode"
                @click="handleSendCode"
                class="send-code-btn"
                :class="{ 'sending': isSendingCode }"
              >
                <Loader2 v-if="isSendingCode" :size="14" class="spin-icon" />
                {{ cooldown > 0 ? `${cooldown}s` : "获取验证码" }}
              </button>
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码 (至少6位)" size="large" show-password>
              <template #prefix><Lock :size="16" class="input-icon" /></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" size="large" show-password>
              <template #prefix><Lock :size="16" class="input-icon" /></template>
            </el-input>
          </el-form-item>

          <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon style="margin-bottom: 16px" />
          <el-alert v-if="success" :title="success" type="success" :closable="false" show-icon style="margin-bottom: 16px" />

          <button :disabled="loading" @click="handleRegister" class="register-btn shimmer-btn">
            <Loader2 v-if="loading" :size="18" class="spin-icon" />
            <span>{{ loading ? "注册中..." : "立即注册" }}</span>
          </button>
        </el-form>

        <div class="auth-footer">
          已有账号？<router-link to="/login">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { authApi } from "../api/auth";
import WealthParticles from "../components/WealthParticles.vue";
import { Sparkles, User, Mail, Phone, Lock, Loader2 } from 'lucide-vue-next';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref(null);

const form = reactive({ username: "", phone: "", email: "", password: "", confirmPassword: "", code: "" });
const registrationMethod = ref("EMAIL");
const loading = ref(false);
const isSendingCode = ref(false);
const cooldown = ref(0);
const error = ref("");
const success = ref("");

let timer = null;

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  email: [{ required: true, message: "请输入邮箱", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }],
  code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少6位", trigger: "blur" }
  ],
  confirmPassword: [{ required: true, message: "请确认密码", trigger: "blur" }],
};

function startCooldown() {
  cooldown.value = 60;
  if (timer) clearInterval(timer);
  timer = setInterval(() => {
    cooldown.value--;
    if (cooldown.value <= 0) clearInterval(timer);
  }, 1000);
}

async function handleSendCode() {
  error.value = "";
  const target = registrationMethod.value === "EMAIL" ? form.email : form.phone;
  if (!target) {
    error.value = `请输入${registrationMethod.value === "EMAIL" ? "邮箱" : "手机号"}`;
    return;
  }
  isSendingCode.value = true;
  try {
    await authApi.sendCode({ target, method: registrationMethod.value });
    startCooldown();
  } catch (err) {
    error.value = !err.response ? "网络错误，请检查后端服务" : err.response.data?.message || "发送失败";
  } finally {
    isSendingCode.value = false;
  }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  error.value = "";
  success.value = "";
  if (!form.phone && !form.email) { error.value = "手机号和邮箱至少填写一个"; return; }
  if (form.password !== form.confirmPassword) { error.value = "两次输入的密码不一致"; return; }
  loading.value = true;
  try {
    await authStore.register({
      username: form.username,
      email: registrationMethod.value === 'EMAIL' ? form.email : '',
      phone: registrationMethod.value === 'PHONE' ? form.phone : '',
      password: form.password,
      code: form.code,
      registrationMethod: registrationMethod.value
    });
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
}

.auth-card {
  width: 100%;
  max-width: 480px;
  border-radius: 28px;
  padding: 40px 36px;
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.auth-logo-wrap {
  width: 68px;
  height: 68px;
  border-radius: 18px;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  box-shadow: 0 8px 28px rgba(212, 175, 55, 0.4);
}

.auth-logo-icon { color: #fff; }

.auth-header h1 {
  font-size: 1.5rem;
  margin-bottom: 6px;
}

.auth-header p {
  color: #909399;
  font-size: 0.88rem;
}

.auth-form { margin-bottom: 20px; }

.input-icon { color: #909399; }

.registration-toggle {
  display: flex;
  width: 100%;
  margin-bottom: 20px;
}

.registration-toggle :deep(.el-radio-button__inner) {
  width: 100%;
}

.registration-toggle :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #D4AF37 0%, #C9A227 100%);
  border-color: #D4AF37;
  box-shadow: none;
}

.code-input-group {
  display: flex;
  gap: 12px;
  width: 100%;
  align-items: center;
}

.send-code-btn {
  min-width: 110px;
  height: 40px;
  white-space: nowrap;
  border: 1px solid #D4AF37;
  color: #D4AF37;
  background: rgba(212, 175, 55, 0.1);
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.send-code-btn:hover:not(:disabled) {
  background: rgba(212, 175, 55, 0.2);
}

.send-code-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.register-btn {
  width: 100%;
  height: 52px;
  font-size: 1rem;
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
}

.register-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.spin-icon { animation: spin 1s linear infinite; }

@keyframes spin { to { transform: rotate(360deg); } }

.auth-footer {
  text-align: center;
  font-size: 0.88rem;
  color: #909399;
}

.auth-footer a {
  font-weight: 700;
  color: #D4AF37;
  text-decoration: none;
}

.auth-footer a:hover { color: #B8860B; }
</style>
