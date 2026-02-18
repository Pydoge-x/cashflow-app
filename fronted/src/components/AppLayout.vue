<template>
  <div class="celestial-container">
    <div class="celestial-stars" style="opacity: 0.15;"></div>
    <WealthParticles :count="35" style="opacity: 0.25;" />
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
            <router-link
              :to="`/charts/${report.id}`"
              class="nav-item sub"
              @click="sidebarOpen = false"
            >
              <span class="nav-icon">📊</span>
              <span>财务图表</span>
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

    <!-- 悬浮词典球 -->
    <router-link to="/glossary" class="floating-glossary" title="金融名词解析">
      <span class="glossary-icon">📖</span>
      <span class="glossary-text">名词解析</span>
    </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useFinanceStore } from "../stores/finance";
import WealthParticles from "./WealthParticles.vue";

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
  position: relative;
  z-index: 1;
  background: transparent;
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 260px;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
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
  font-size: 1.3rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), #a78bfa);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.02em;
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
  background: hsla(var(--h-primary), var(--s-primary), var(--l-primary), 0.08);
  color: #fff;
  transform: translateX(4px);
}

.nav-item.router-link-active,
.nav-item.router-link-exact-active {
  background: linear-gradient(90deg, var(--color-primary-bg) 0%, transparent 100%);
  color: var(--color-primary);
  border-left: 2px solid var(--color-primary);
  border-radius: 0 8px 8px 0;
  margin-left: 0;
  width: calc(100% - 0.6rem);
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
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
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

/* ===== 悬浮词典球 ===== */
.floating-glossary {
  position: fixed;
  right: 2.5rem;
  bottom: 2.5rem;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #818cf8 100%);
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 32px hsla(var(--h-primary), var(--s-primary), var(--l-primary), 0.4);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  z-index: 1000;
  text-decoration: none;
  overflow: hidden;
  border: 1px solid hsla(0, 0%, 100%, 0.2);
}

.floating-glossary::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, hsla(0,0%,100%,0.3) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.floating-glossary:hover::after {
  opacity: 1;
}

.floating-glossary:hover {
  width: 160px;
  border-radius: 30px;
  transform: scale(1.05) translateY(-5px);
  box-shadow: 0 12px 40px hsla(var(--h-primary), var(--s-primary), var(--l-primary), 0.5);
}

.glossary-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.glossary-text {
  color: #fff;
  font-weight: 600;
  font-size: 0.9rem;
  white-space: nowrap;
  opacity: 0;
  width: 0;
  transition: all 0.3s ease;
  margin-left: 0;
}

.floating-glossary:hover .glossary-text {
  opacity: 1;
  width: auto;
  margin-left: 0.5rem;
}

@media (max-width: 768px) {
  .floating-glossary {
    right: 1rem;
    bottom: 1rem;
  }
  .floating-glossary:hover {
    width: 56px; /* 移动端不展开，保持简洁 */
  }
  .floating-glossary:hover .glossary-text {
    display: none;
  }
}
</style>
