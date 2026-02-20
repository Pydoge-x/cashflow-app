<template>
  <div class="golden-container">
    <div class="golden-pattern" style="opacity: 0.5;"></div>
    <WealthParticles :count="35" style="opacity: 0.4;" />
    <el-container class="app-layout">
      <!-- 侧边栏 (桌面端) -->
      <el-aside :width="sidebarWidth" class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
        <div class="sidebar-header">
          <div class="logo">
            <span class="logo-icon">💰</span>
            <span v-show="!isCollapsed" class="logo-text">财富现金流</span>
          </div>
          <el-button
            class="collapse-btn"
            text
            @click="isCollapsed = !isCollapsed"
          >
            <el-icon><component :is="isCollapsed ? Expand : Fold" /></el-icon>
          </el-button>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          class="sidebar-menu"
          router
        >
          <el-menu-item index="/">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>

          <el-menu-item-group v-if="financeStore.reports.length > 0">
            <template #title>
              <span class="group-title">报表管理</span>
            </template>
          </el-menu-item-group>

          <template v-for="report in financeStore.reports" :key="report.id">
            <el-sub-menu :index="`report-${report.id}`">
              <template #title>
                <el-tag
                  :type="report.type === 'PERSONAL' ? 'warning' : 'success'"
                  size="small"
                  effect="plain"
                  style="margin-right: 8px"
                >
                  {{ report.type === "PERSONAL" ? "个人" : "家庭" }}
                </el-tag>
                <span>{{ report.name }}</span>
              </template>
              <el-menu-item :index="`/balance-sheet/${report.id}`">
                <el-icon><Document /></el-icon>
                <span>资产负债表</span>
              </el-menu-item>
              <el-menu-item :index="`/income-expense/${report.id}`">
                <el-icon><Money /></el-icon>
                <span>收入支出表</span>
              </el-menu-item>
              <el-menu-item :index="`/cashflow/${report.id}`">
                <el-icon><TrendCharts /></el-icon>
                <span>现金流表</span>
              </el-menu-item>
              <el-menu-item :index="`/charts/${report.id}`">
                <el-icon><PieChart /></el-icon>
                <span>财务图表</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
          <el-divider />
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>{{ authStore.user?.username || "个人中心" }}</span>
          </el-menu-item>
          <el-menu-item @click="handleLogout" class="logout-item">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 移动端抽屉菜单 -->
      <el-drawer
        v-model="mobileMenuOpen"
        direction="ltr"
        :size="280"
        :show-close="false"
        class="mobile-drawer"
      >
        <template #header>
          <div class="drawer-header">
            <div class="logo">
              <span class="logo-icon">💰</span>
              <span class="logo-text">CashFlow</span>
            </div>
          </div>
        </template>
        <el-menu
          :default-active="activeMenu"
          class="mobile-menu"
          router
          @select="mobileMenuOpen = false"
        >
          <el-menu-item index="/">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>

          <template v-for="report in financeStore.reports" :key="report.id">
            <el-sub-menu :index="`report-${report.id}`">
              <template #title>
                <el-tag
                  :type="report.type === 'PERSONAL' ? 'warning' : 'success'"
                  size="small"
                  effect="plain"
                  style="margin-right: 8px"
                >
                  {{ report.type === "PERSONAL" ? "个人" : "家庭" }}
                </el-tag>
                <span>{{ report.name }}</span>
              </template>
              <el-menu-item :index="`/balance-sheet/${report.id}`">
                <el-icon><Document /></el-icon>
                <span>资产负债表</span>
              </el-menu-item>
              <el-menu-item :index="`/income-expense/${report.id}`">
                <el-icon><Money /></el-icon>
                <span>收入支出表</span>
              </el-menu-item>
              <el-menu-item :index="`/cashflow/${report.id}`">
                <el-icon><TrendCharts /></el-icon>
                <span>现金流表</span>
              </el-menu-item>
              <el-menu-item :index="`/charts/${report.id}`">
                <el-icon><PieChart /></el-icon>
                <span>财务图表</span>
              </el-menu-item>
            </el-sub-menu>
          </template>

          <el-divider />
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>{{ authStore.user?.username || "个人中心" }}</span>
          </el-menu-item>
          <el-menu-item @click="handleLogout" class="logout-item">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>

      <!-- 主要内容区域 -->
      <el-container class="main-container">
        <el-header class="top-bar">
          <el-button
            class="menu-btn"
            text
            @click="mobileMenuOpen = true"
          >
            <el-icon><Menu /></el-icon>
          </el-button>
          <div class="top-bar-right">
            <span class="greeting">{{ greeting }}</span>
          </div>
        </el-header>
        <el-main class="content-area">
          <router-view />
        </el-main>
      </el-container>

      <!-- 悬浮词典球 -->
      <router-link to="/glossary" class="floating-glossary" title="金融名词解析">
        <span class="glossary-icon">📖</span>
        <span class="glossary-text">名词解析</span>
      </router-link>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useFinanceStore } from "../stores/finance";
import WealthParticles from "./WealthParticles.vue";
import {
  DataAnalysis,
  Document,
  Money,
  TrendCharts,
  PieChart,
  User,
  SwitchButton,
  Expand,
  Fold,
  Menu
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const financeStore = useFinanceStore();
const mobileMenuOpen = ref(false);
const isCollapsed = ref(false);

const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '260px');
const activeMenu = computed(() => route.path);

const greeting = computed(() => {
  const hour = new Date().getHours();
  const name = authStore.user?.username || "";
  if (!name) return `你好${name ? "，" + name : ""} 🌍`;
  if (hour >= 0 && hour < 6) return `${name ? "" + name : ""}凌晨啦，记得早点休息，身体才是金钱的来源！ 🌙`;
  if (hour >= 6 && hour < 12) return `${name ? "" + name : ""}早上好，新的一天，从一杯咖啡开始，愿你金钱多多！ 🌅`;
  if (hour >= 12 && hour < 14) return `${name ? "" + name : ""}中午好，午后的阳光和你，都像金币一样闪耀！ ☀️`;
  if (hour >= 14 && hour < 18) return `${name ? "" + name : ""}下午好，继续努力，愿你金钱多多！ 🌅`;
  if (hour >= 18 && hour < 24) return `${name ? "" + name : ""}晚上好，休息时间到了，愿你有更多的时间来思考和规划！ 🌕`;
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
  min-height: 100vh;
  position: relative;
  z-index: 1;
  background: transparent;
}

/* ===== 侧边栏 ===== */
.sidebar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid #E8D5A3;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  transition: width 0.3s ease;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #F0E8D0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 1.5rem;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 800;
  background: linear-gradient(135deg, #D4AF37, #B8860B);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.02em;
}

.collapse-btn {
  color: #909399;
}

.collapse-btn:hover {
  color: #D4AF37;
}

.sidebar-menu {
  border: none !important;
  background: transparent !important;
  flex: 1;
  overflow-y: auto;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

.group-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.sidebar-footer {
  border-top: 1px solid #F0E8D0;
  padding-top: 8px;
}

.logout-item {
  color: #ff4d4f !important;
}

.logout-item:hover {
  background: rgba(255, 77, 79, 0.1) !important;
}

/* ===== 主内容区域 ===== */
.main-container {
  margin-left: 260px;
  transition: margin-left 0.3s ease;
}

.sidebar-collapsed ~ .main-container {
  margin-left: 64px;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #E8D5A3;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.menu-btn {
  display: none;
  font-size: 1.25rem;
  color: #333;
}

.greeting {
  font-size: 0.88rem;
  color: #666;
}

.content-area {
  padding: 24px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  --el-main-padding: 0;
}

/* ===== 移动端抽屉 ===== */
.mobile-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #F0E8D0;
}

.mobile-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.drawer-header {
  display: flex;
  align-items: center;
}

.mobile-menu {
  border: none !important;
}

/* ===== 悬浮词典球 ===== */
.floating-glossary {
  position: fixed;
  right: 2.5rem;
  bottom: 2.5rem;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(212, 175, 55, 0.4);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  z-index: 1000;
  text-decoration: none;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.floating-glossary::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(255,255,255,0.3) 0%, transparent 70%);
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
  box-shadow: 0 12px 40px rgba(212, 175, 55, 0.5);
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
  margin-left: 8px;
}

/* ===== 移动端适配 ===== */
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .main-container {
    margin-left: 0 !important;
  }

  .menu-btn {
    display: flex;
  }

  .content-area {
    padding: 16px;
  }

  .floating-glossary {
    right: 1rem;
    bottom: 1rem;
  }
  
  .floating-glossary:hover {
    width: 60px;
  }
  
  .floating-glossary:hover .glossary-text {
    display: none;
  }
}
</style>
