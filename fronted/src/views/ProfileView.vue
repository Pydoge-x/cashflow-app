<template>
  <div class="profile">
    <div class="page-header">
      <h1>👤 个人信息</h1>
    </div>

    <div class="card" style="max-width: 600px">
      <form @submit.prevent="handleSave">
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
          />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>手机号</label>
            <input v-model="form.phone" type="tel" placeholder="手机号" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="form.email" type="email" placeholder="邮箱" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>性别</label>
            <select v-model="form.gender">
              <option value="">未设置</option>
              <option value="MALE">男</option>
              <option value="FEMALE">女</option>
            </select>
          </div>
          <div class="form-group">
            <label>年龄</label>
            <input
              v-model.number="form.age"
              type="number"
              min="1"
              max="150"
              placeholder="年龄"
            />
          </div>
        </div>

        <div v-if="message" class="profile-message" :class="messageType">
          {{ message }}
        </div>

        <button type="submit" class="btn btn-primary" :disabled="saving">
          {{ saving ? "保存中..." : "保存信息" }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useAuthStore } from "../stores/auth";

const authStore = useAuthStore();
const form = ref({ username: "", phone: "", email: "", gender: "", age: "" });
const saving = ref(false);
const message = ref("");
const messageType = ref("");

onMounted(async () => {
  try {
    const profile = await authStore.fetchProfile();
    form.value = {
      username: profile.username || "",
      phone: profile.phone || "",
      email: profile.email || "",
      gender: profile.gender || "",
      age: profile.age || "",
    };
  } catch {
    // use existing store data
    if (authStore.user) {
      form.value = { ...authStore.user };
    }
  }
});

async function handleSave() {
  saving.value = true;
  message.value = "";
  try {
    await authStore.updateProfile(form.value);
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
.profile-message {
  padding: 0.6rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.82rem;
  margin-bottom: 1rem;
}

.profile-message.success {
  background: var(--color-success-bg);
  color: var(--color-success);
}

.profile-message.error {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}
</style>
