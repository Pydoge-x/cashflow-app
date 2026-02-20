<template>
  <div class="profile">
    <div class="page-header">
      <h1>👤 个人信息</h1>
    </div>

    <div class="profile-container">
      <!-- 头像和基本信息卡片 -->
      <el-card class="avatar-card" shadow="hover">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <div class="avatar-placeholder">
              <span class="avatar-text">{{ avatarText }}</span>
            </div>
            <div class="avatar-glow"></div>
          </div>
          <div class="user-info">
            <h2 class="user-name">{{ form.username || "未设置用户名" }}</h2>
            <p class="user-bio">管理您的个人信息，让理财更个性化</p>
          </div>
        </div>
      </el-card>

      <!-- 表单卡片 -->
      <el-card class="profile-card" shadow="hover">
        <template #header>
          <div class="card-header-section">
            <span class="header-title">编辑资料</span>
            <el-tag type="warning" effect="plain" size="small">个人信息</el-tag>
          </div>
        </template>
        
        <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          label-position="top"
          class="profile-form"
          @submit.prevent="handleSave"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
            />
          </el-form-item>
          
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入邮箱" size="large" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="form.gender" placeholder="请选择" style="width: 100%" size="large">
                  <el-option label="未设置" value="" />
                  <el-option label="男" value="MALE" />
                  <el-option label="女" value="FEMALE" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="年龄">
                <el-input-number
                  v-model="form.age"
                  :min="1"
                  :max="150"
                  placeholder="年龄"
                  style="width: 100%"
                  size="large"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-alert
            v-if="message"
            :title="message"
            :type="messageType"
            :closable="false"
            show-icon
            class="message-alert"
          />

          <div class="form-actions">
            <el-button type="primary" size="large" :loading="saving" @click="handleSave" class="save-btn">
              <template #icon>
                <Check />
              </template>
              {{ saving ? "保存中..." : "保存信息" }}
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useAuthStore } from "../stores/auth";
import { Check } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const formRef = ref(null);
const form = reactive({
  username: "",
  phone: "",
  email: "",
  gender: "",
  age: null
});
const saving = ref(false);
const message = ref("");
const messageType = ref("");

const avatarText = computed(() => {
  if (form.username) {
    return form.username.charAt(0).toUpperCase();
  }
  return "U";
});

const formRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
};

onMounted(async () => {
  try {
    const profile = await authStore.fetchProfile();
    Object.assign(form, {
      username: profile.username || "",
      phone: profile.phone || "",
      email: profile.email || "",
      gender: profile.gender || "",
      age: profile.age || null,
    });
  } catch {
    if (authStore.user) {
      Object.assign(form, { ...authStore.user });
    }
  }
});

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  message.value = "";
  try {
    await authStore.updateProfile({ ...form });
    message.value = "保存成功！";
    messageType.value = "success";
  } catch (e) {
    message.value = e.response?.data?.message || "保存失败";
    messageType.value = "error";
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 700px;
}

/* ===== 头像卡片 ===== */
.avatar-card {
  border-radius: 20px;
  overflow: visible;
}

.avatar-card :deep(.el-card__body) {
  padding: 32px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-wrapper {
  position: relative;
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #D4AF37 0%, #C9A227 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.3);
  position: relative;
  z-index: 2;
}

.avatar-text {
  font-size: 2.5rem;
  font-weight: 700;
  color: #fff;
  text-transform: uppercase;
}

.avatar-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.3) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
  z-index: 1;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
  background: linear-gradient(135deg, #333 0%, #D4AF37 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-bio {
  color: #909399;
  font-size: 0.95rem;
  margin: 0;
}

/* ===== 表单卡片 ===== */
.profile-card {
  border-radius: 20px;
}

.profile-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #F0E8D0;
}

.profile-card :deep(.el-card__body) {
  padding: 28px 24px 24px;
}

.card-header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-weight: 600;
  font-size: 1.1rem;
  color: #333;
}

.profile-form {
  margin-top: 8px;
}

.form-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #F0E8D0;
}

.save-btn {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #D4AF37 0%, #C9A227 100%);
  border: none;
}

.save-btn:hover {
  background: linear-gradient(135deg, #E8C04A 0%, #D4AF37 100%);
}

.message-alert {
  margin-bottom: 0;
  border-radius: 12px;
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-card :deep(.el-card__body),
  .profile-card :deep(.el-card__body) {
    padding: 24px 20px;
  }
  
  .user-name {
    font-size: 1.25rem;
  }
}
</style>
