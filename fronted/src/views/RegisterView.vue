<template>
  <div class="golden-container">
    <div class="golden-pattern"></div>
    <WealthParticles :count="60" />
    <div class="auth-page">
      <el-card class="auth-card auth-glass-card" shadow="hover">
        <div class="auth-header">
          <span class="auth-logo">✨</span>
          <h1>加入 CashFlow</h1>
          <p>开启您的智慧理财之旅</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister" class="auth-form">
          <el-radio-group v-model="registrationMethod" class="registration-toggle">
            <el-radio-button value="EMAIL">邮箱注册</el-radio-button>
            <!-- <el-radio-button value="PHONE">手机注册</el-radio-button> -->
          </el-radio-group>

          <el-form-item prop="username">
            <el-input
              v-model.trim="form.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item v-if="registrationMethod === 'EMAIL'" prop="email">
            <el-input
              v-model.trim="form.email"
              type="email"
              placeholder="请输入常用邮箱"
              size="large"
              prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item v-else prop="phone">
            <el-input
              v-model.trim="form.phone"
              type="tel"
              placeholder="请输入手机号"
              size="large"
              prefix-icon="Phone"
            />
          </el-form-item>

          <el-form-item prop="code">
            <div class="code-input-group">
              <el-input
                v-model.trim="form.code"
                placeholder="6位验证码"
                size="large"
                maxlength="6"
                style="flex: 1"
              />
              <el-button
                type="default"
                size="large"
                :disabled="cooldown > 0 || isSendingCode"
                :loading="isSendingCode"
                @click="handleSendCode"
                class="send-code-btn"
              >
                {{ cooldown > 0 ? `${cooldown}s` : "获取验证码" }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码 (至少6位)"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-alert
            v-if="error"
            :title="error"
            type="error"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          />
          <el-alert
            v-if="success"
            :title="success"
            type="success"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          />

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            class="register-btn"
          >
            {{ loading ? "注册中..." : "立即注册" }}
          </el-button>
        </el-form>

        <div class="auth-footer">
          已有账号？<router-link to="/login">返回登录</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { authApi } from "../api/auth";
import WealthParticles from "../components/WealthParticles.vue";

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref(null);

const form = reactive({
  username: "",
  phone: "",
  email: "",
  password: "",
  confirmPassword: "",
  code: ""
});
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
    if (cooldown.value <= 0) {
      clearInterval(timer);
    }
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
    await authApi.sendCode({
      target: target,
      method: registrationMethod.value
    });
    startCooldown();
  } catch (err) {
    if (!err.response) {
      error.value = "网络错误，请检查后端服务";
    } else {
      error.value = err.response.data?.message || `发送失败`;
    }
  } finally {
    isSendingCode.value = false;
  }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  error.value = "";
  success.value = "";

  if (!form.phone && !form.email) {
    error.value = "手机号和邮箱至少填写一个";
    return;
  }
  if (form.password !== form.confirmPassword) {
    error.value = "两次输入的密码不一致";
    return;
  }

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
  max-width: 480px;
  border-radius: 24px;
}

.auth-card :deep(.el-card__body) {
  padding: 36px 32px;
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.auth-logo {
  font-size: 3rem;
  display: block;
  margin-bottom: 12px;
}

.auth-header h1 {
  font-size: 1.75rem;
  font-weight: 800;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
}

.auth-header p {
  color: #909399;
  font-size: 0.88rem;
}

.auth-form {
  margin-bottom: 20px;
}

.registration-toggle {
  display: flex;
  width: 100%;
  margin-bottom: 20px;
}

.registration-toggle :deep(.el-radio-button__inner) {
  width: 100%;
  border-radius: 8px !important;
}

.registration-toggle :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 8px 0 0 8px !important;
}

.registration-toggle :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 8px 8px 0 !important;
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
}

.send-code-btn {
  min-width: 110px;
  white-space: nowrap;
  border: 1px solid #D4AF37 !important;
  color: #D4AF37 !important;
  background: rgba(212, 175, 55, 0.1) !important;
}

.send-code-btn:hover:not(:disabled) {
  background: rgba(212, 175, 55, 0.2) !important;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  margin-top: 8px;
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
