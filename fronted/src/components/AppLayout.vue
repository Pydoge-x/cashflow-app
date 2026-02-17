<template>
  <div class="app-layout">
    <!-- 侧边栏 (桌面端) -->
    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">💰</span>
          <span class="logo-text">CashFlow</span>
        </div>
        <button class="sidebar-close" @click="sidebarOpen = false">✕</button>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item" @click="sidebarOpen = false">
          <span class="nav-icon">📊</span>
          <span>仪表盘</span>
        </router-link>

        <div class="nav-section">报表管理</div>

        <template v-for="report in financeStore.reports" :key="report.id">
          <div class="nav-group">
            <div class="nav-group-title">
              <span
                class="badge"
                :class="
                  report.type === 'PERSONAL' ? 'badge-personal' : 'badge-family'
                "
              >
                {{ report.type === "PERSONAL" ? "个人" : "家庭" }}
              </span>
              {{ report.name }}
            </div>
            <router-link
              :to="`/balance-sheet/${report.id}`"
              class="nav-item sub"
              @click="sidebarOpen = false"
            >
              <span class="nav-icon">📋</span>
              <span>资产负债表</span>
            </router-link>
            <router-link
              :to="`/income-expense/${report.id}`"
              class="nav-item sub"
              @click="sidebarOpen = false"
            >
              <span class="nav-icon">💵</span>
              <span>收入支出表</span>
            </router-link>
            <router-link
              :to="`/cashflow/${report.id}`"
              class="nav-item sub"
              @click="sidebarOpen = false"
            >
              <span class="nav-icon">📈</span>
              <span>现金流表</span>
            </router-link>
          </div>
        </template>
      </nav>

      <div class="sidebar-footer">
        <router-link
          to="/profile"
          class="nav-item"
          @click="sidebarOpen = false"
        >
          <span class="nav-icon">👤</span>
          <span>{{ authStore.user?.username || "个人中心" }}</span>
        </router-link>
        <button class="nav-item logout-btn" @click="handleLogout">
          <span class="nav-icon">🚪</span>
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <!-- 遮罩 (移动端) -->
    <div
      class="sidebar-backdrop"
      v-if="sidebarOpen"
      @click="sidebarOpen = false"
    ></div>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <header class="top-bar">
        <button class="menu-btn" @click="sidebarOpen = !sidebarOpen">☰</button>
        <div class="top-bar-right">
          <span class="greeting">{{ greeting }}</span>
        </div>
      </header>
      <div class="content-area">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useFinanceStore } from "../stores/finance";

const router = useRouter();
const authStore = useAuthStore();
const financeStore = useFinanceStore();
const sidebarOpen = ref(false);

const greeting = computed(() => {
  const hour = new Date().getHours();
  const name = authStore.user?.username || "";
  if (hour < 12) return `早上好${name ? "，" + name : ""} 🌅`;
  if (hour < 18) return `下午好${name ? "，" + name : ""} ☀️`;
  return `晚上好${name ? "，" + name : ""} 🌙`;
});

function handleLogout() {
  authStore.logout();
  router.push("/login");
}

onMounted(() => {
  financeStore.fetchReports().catch(() => {});
});
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 260px;
  background: var(--color-bg-secondary);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  transition: transform 0.3s ease;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.2rem 1.2rem 1rem;
  border-bottom: 1px solid var(--color-border);
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.logo-icon {
  font-size: 1.5rem;
}

.logo-text {
  font-size: 1.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-close {
  display: none;
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: 1.2rem;
  cursor: pointer;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 0.8rem 0;
}

.nav-section {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-text-muted);
  padding: 1rem 1.2rem 0.4rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.6rem 1.2rem;
  color: var(--color-text-secondary);
  font-size: 0.88rem;
  font-weight: 500;
  border-radius: var(--radius-md);
  margin: 0.15rem 0.6rem;
  transition: var(--transition);
  text-decoration: none;
  cursor: pointer;
  border: none;
  background: none;
  width: calc(100% - 1.2rem);
  text-align: left;
}

.nav-item:hover {
  background: var(--color-primary-bg);
  color: var(--color-text);
}

.nav-item.router-link-active,
.nav-item.router-link-exact-active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.nav-item.sub {
  padding-left: 2.4rem;
  font-size: 0.82rem;
}

.nav-icon {
  font-size: 1.05rem;
  width: 1.4rem;
  text-align: center;
}

.nav-group {
  margin-bottom: 0.3rem;
}

.nav-group-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1.2rem;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--color-text);
}

.sidebar-footer {
  border-top: 1px solid var(--color-border);
  padding: 0.5rem 0;
}

.logout-btn {
  color: var(--color-danger) !important;
}

/* ===== 主内容区域 ===== */
.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.8rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: rgba(15, 17, 25, 0.8);
  backdrop-filter: blur(12px);
  position: sticky;
  top: 0;
  z-index: 50;
}

.menu-btn {
  display: none;
  background: none;
  border: none;
  color: var(--color-text);
  font-size: 1.4rem;
  cursor: pointer;
  padding: 0.3rem;
}

.greeting {
  font-size: 0.88rem;
  color: var(--color-text-secondary);
}

.content-area {
  flex: 1;
  padding: 1.5rem;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

.sidebar-backdrop {
  display: none;
}

/* ===== 移动端适配 ===== */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .sidebar-close {
    display: block;
  }

  .sidebar-backdrop {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 99;
  }

  .main-content {
    margin-left: 0;
  }

  .menu-btn {
    display: block;
  }

  .content-area {
    padding: 1rem;
  }
}
</style>
