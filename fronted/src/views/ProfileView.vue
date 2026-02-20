<template>
  <div class="profile">
    <div class="page-header">
      <h1>👤 个人信息</h1>
    </div>

    <el-card class="profile-card" shadow="hover" style="max-width: 600px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-position="top"
        @submit.prevent="handleSave"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
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
                style="width: 100%"
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
          style="margin-bottom: 16px"
        />

        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ saving ? "保存中..." : "保存信息" }}
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useAuthStore } from "../stores/auth";

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
.profile-card {
  border-radius: 16px;
}

.profile-card :deep(.el-card__body) {
  padding: 32px;
}
</style>
