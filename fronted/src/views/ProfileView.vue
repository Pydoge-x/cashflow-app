<template>
  <div class="profile">
    <div class="page-header">
      <h1>个人信息中心</h1>
    </div>

    <div class="profile-container">
      <!-- 头像和基本信息卡片 -->
      <div class="avatar-card glass-card spotlight-card">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <div class="avatar-inner">
              <span class="avatar-text">{{ avatarText }}</span>
            </div>
            <div class="avatar-glow"></div>
          </div>
          <div class="user-info">
            <h2 class="user-name gold-text">{{ form.username || "未设置用户名" }}</h2>
            <p class="user-bio">管理您的个人资料，定制专属的财富管家</p>
            <!-- <div class="user-badges">
              <span class="badge shimmer-btn"><ShieldCheck :size="12" /> 高级会员</span>
              <span class="badge-outline"><CircleUser :size="12" /> 账户已实名</span>
            </div> -->
          </div>
        </div>
      </div>

      <!-- 表单卡片 -->
      <div class="profile-card glass-card spotlight-card">
        <div class="card-header-section">
          <div class="header-title-group">
            <div class="icon-wrap"><Settings :size="18" /></div>
            <span class="header-title">编辑资料</span>
          </div>
          <el-tag type="warning" effect="plain" size="small" class="header-badge">账号设置</el-tag>
        </div>
        
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useAuthStore } from "../stores/auth";
import { Check, User, Mail, Phone, ShieldCheck, CircleUser, Settings } from 'lucide-vue-next';

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
  max-width: 800px;
}

/* ===== 头像卡片 ===== */
.avatar-card {
  border-radius: 24px;
  padding: 40px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 32px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
}

.avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 32px;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 32px rgba(212, 175, 55, 0.25);
  position: relative;
  z-index: 2;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.avatar-text {
  font-size: 3rem;
  font-weight: 800;
  color: #fff;
}

.avatar-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.2) 0%, transparent 70%);
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.1); }
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.user-bio {
  color: #666;
  font-size: 1rem;
  margin-bottom: 20px;
}

.user-badges {
  display: flex;
  gap: 12px;
}

.badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: #D4AF37;
  color: white;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

.badge-outline {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: 1px solid rgba(212, 175, 55, 0.3);
  color: #D4AF37;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

/* ===== 表单卡片 ===== */
.profile-card {
  border-radius: 24px;
  padding: 0;
  overflow: hidden;
}

.card-header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.12);
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(212, 175, 55, 0.1);
  color: #D4AF37;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-weight: 700;
  font-size: 1.15rem;
  color: #333;
}

.profile-form {
  padding: 32px;
}

.form-actions {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid rgba(212, 175, 55, 0.12);
}

.save-btn {
  width: 100%;
  height: 54px;
  font-size: 1.1rem;
  font-weight: 700;
  border-radius: 16px;
}

/* ===== 响应式适配 ===== */
@media (max-width: 768px) {
  .avatar-section {
    flex-direction: column;
    gap: 24px;
    text-align: center;
  }
  
  .user-badges {
    justify-content: center;
  }

  .profile-form {
    padding: 24px 20px;
  }
}
</style>
