<template>
  <div class="golden-container">
    <div class="golden-pattern" style="opacity: 0.5;"></div>
    <WealthParticles :count="35" style="opacity: 0.4;" />
    <el-container class="app-layout">
      <!-- 侧边栏 (桌面端) -->
      <el-aside :width="sidebarWidth" class="sidebar glass-card" :class="{ 'sidebar-collapsed': isCollapsed }">
        <div class="sidebar-header">
          <div class="logo">
            <span class="logo-icon-wrap shimmer-btn">
              <WalletIcon :size="22" class="logo-svg-icon" />
            </span>
            <span v-show="!isCollapsed" class="logo-text gold-text">智慧家庭财务助手</span>
          </div>
          <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <PanelLeftClose v-if="!isCollapsed" :size="18" />
            <PanelLeftOpen v-else :size="18" />
          </button>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          class="sidebar-menu"
          router
        >
          <el-menu-item index="/">
            <template #title><span>仪表盘</span></template>
            <LayoutDashboard :size="18" />
          </el-menu-item>

          <el-menu-item-group v-if="financeStore.reports.length > 0">
            <template #title>
              <span class="group-title">报表管理</span>
            </template>
          </el-menu-item-group>

          <template v-for="report in financeStore.reports" :key="report.id">
            <el-sub-menu :index="`report-${report.id}`">
              <template #title>
                <span class="report-type-badge" :class="report.type === 'PERSONAL' ? 'badge-personal' : 'badge-family'">
                  <component :is="report.type === 'PERSONAL' ? UserIcon : UsersIcon" :size="12" />
                  {{ report.type === "PERSONAL" ? "个人" : "家庭" }}
                </span>
                <span class="report-name">{{ report.name }}</span>
              </template>
              <el-menu-item :index="`/balance-sheet/${report.id}`">
                <FileText :size="16" />
                <span>资产负债表</span>
              </el-menu-item>
              <el-menu-item :index="`/income-expense/${report.id}`">
                <DollarSign :size="16" />
                <span>收入支出表</span>
              </el-menu-item>
              <el-menu-item :index="`/cashflow/${report.id}`">
                <TrendingUp :size="16" />
                <span>现金流表</span>
              </el-menu-item>
              <el-menu-item :index="`/charts/${report.id}`">
                <PieChart :size="16" />
                <span>财务图表</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
          <el-divider />
          <el-menu-item index="/profile">
            <UserCircle :size="18" />
            <template #title><span>{{ authStore.user?.username || "个人中心" }}</span></template>
          </el-menu-item>
          <el-menu-item @click="handleLogout" class="logout-item">
            <LogOut :size="18" />
            <template #title><span>退出登录</span></template>
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
              <span class="logo-icon-wrap"><WalletIcon :size="20" class="logo-svg-icon" /></span>
              <span class="logo-text">智慧家庭财务助手</span>
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
            <LayoutDashboard :size="18" />
            <span>仪表盘</span>
          </el-menu-item>
          <template v-for="report in financeStore.reports" :key="report.id">
            <el-sub-menu :index="`report-${report.id}`">
              <template #title>
                <span class="report-type-badge" :class="report.type === 'PERSONAL' ? 'badge-personal' : 'badge-family'">
                  <component :is="report.type === 'PERSONAL' ? UserIcon : UsersIcon" :size="12" />
                  {{ report.type === "PERSONAL" ? "个人" : "家庭" }}
                </span>
                <span>{{ report.name }}</span>
              </template>
              <el-menu-item :index="`/balance-sheet/${report.id}`">
                <FileText :size="16" /><span>资产负债表</span>
              </el-menu-item>
              <el-menu-item :index="`/income-expense/${report.id}`">
                <DollarSign :size="16" /><span>收入支出表</span>
              </el-menu-item>
              <el-menu-item :index="`/cashflow/${report.id}`">
                <TrendingUp :size="16" /><span>现金流表</span>
              </el-menu-item>
              <el-menu-item :index="`/charts/${report.id}`">
                <PieChart :size="16" /><span>财务图表</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
          <el-divider />
          <el-menu-item index="/profile">
            <UserCircle :size="18" /><span>{{ authStore.user?.username || "个人中心" }}</span>
          </el-menu-item>
          <el-menu-item @click="handleLogout" class="logout-item">
            <LogOut :size="18" /><span>退出登录</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>

      <!-- 主要内容区域 -->
      <el-container class="main-container">
        <el-header class="top-bar glass-card spotlight-card">
          <button class="menu-btn" @click="mobileMenuOpen = true">
            <Menu :size="22" />
          </button>
          <div class="top-bar-right">
            <span class="greeting">{{ greeting }}</span>
          </div>
        </el-header>
        <el-main class="content-area">
          <router-view />
        </el-main>
      </el-container>

      <!-- 悬浮词典球 (仅在桌面端或侧边展开时更明显) -->
      <router-link to="/glossary" class="floating-glossary shimmer-btn" title="金融名词解析">
        <BookOpen :size="22" class="glossary-svg-icon" />
        <span class="glossary-text">名词解析</span>
      </router-link>

      <!-- 移动端底部导航栏 -->
      <div class="bottom-nav glass-card">
        <router-link to="/" class="nav-item" :class="{ active: activeMenu === '/' }">
          <LayoutDashboard :size="20" />
          <span>首页</span>
        </router-link>
        
        <button class="nav-item" @click="mobileMenuOpen = true">
          <Layers :size="20" />
          <span>报表</span>
        </button>

        <router-link to="/glossary" class="nav-item" :class="{ active: activeMenu === '/glossary' }">
          <BookMarked :size="20" />
          <span>词典</span>
        </router-link>

        <router-link to="/profile" class="nav-item" :class="{ active: activeMenu === '/profile' }">
          <UserCircle :size="20" />
          <span>我的</span>
        </router-link>
      </div>
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
  Wallet as WalletIcon,
  LayoutDashboard,
  FileText,
  DollarSign,
  TrendingUp,
  PieChart,
  UserCircle,
  LogOut,
  PanelLeftClose,
  PanelLeftOpen,
  Menu,
  BookOpen,
  BookMarked,
  Layers,
  User as UserIcon,
  Users as UsersIcon
} from 'lucide-vue-next';

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
  if (hour >= 0 && hour < 6) return `${name}凌晨啦，记得早点休息，身体才是金钱的来源！`;
  if (hour >= 6 && hour < 12) return `${name}早上好，新的一天，愿你金钱多多！`;
  if (hour >= 12 && hour < 14) return `${name}中午好，午后的阳光和你，都像金币一样闪耀！`;
  if (hour >= 14 && hour < 18) return `${name}下午好，继续努力，愿你金钱多多！`;
  if (hour >= 18 && hour < 24) return `${name}晚上好，愿你有更多的时间来思考和规划！`;
  return `晚上好${name ? "，" + name : ""}`;
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
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-right: 1px solid rgba(212, 175, 55, 0.2);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  transition: width 0.3s ease;
  box-shadow: 4px 0 24px rgba(212, 175, 55, 0.08);
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.15);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
}

.logo-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(212,175,55,0.35);
}

.logo-svg-icon {
  color: #fff;
}

.logo-text {
  font-size: 0.92rem;
  font-weight: 800;
  background: linear-gradient(135deg, #D4AF37, #B8860B);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.collapse-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #909399;
  padding: 6px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.collapse-btn:hover {
  color: #D4AF37;
  background: rgba(212, 175, 55, 0.1);
}

.sidebar-menu {
  border: none !important;
  background: transparent !important;
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

.group-title {
  font-size: 0.72rem;
  font-weight: 700;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.report-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.68rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
  margin-right: 8px;
  text-transform: uppercase;
}

.badge-personal {
  background: rgba(212, 175, 55, 0.15);
  color: #B8860B;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

.badge-family {
  background: rgba(82, 196, 26, 0.1);
  color: #389e0d;
  border: 1px solid rgba(82, 196, 26, 0.2);
}

.report-name {
  font-size: 0.88rem;
}

.logout-item {
  color: #ff4d4f !important;
}

.logout-item:hover {
  background: rgba(255, 77, 79, 0.08) !important;
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
  border-bottom: 1px solid rgba(212, 175, 55, 0.15);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.menu-btn {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  color: #333;
  padding: 6px;
  border-radius: 8px;
  align-items: center;
  justify-content: center;
}

.menu-btn:hover { color: #D4AF37; }

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
  border-bottom: 1px solid rgba(212, 175, 55, 0.15);
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
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  border-radius: 28px;
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
  animation: glow-pulse 3s ease-in-out infinite;
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
  width: 140px;
  border-radius: 28px;
  transform: scale(1.05) translateY(-4px);
  box-shadow: 0 12px 40px rgba(212, 175, 55, 0.55);
  animation: none;
}

.glossary-svg-icon {
  color: #fff;
  flex-shrink: 0;
}

.glossary-text {
  color: #fff;
  font-weight: 700;
  font-size: 0.88rem;
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

/* ===== 移动端底部导航栏 ===== */
.bottom-nav {
  display: none;
  position: fixed;
  bottom: 16px;
  left: 16px;
  right: 16px;
  height: 64px;
  border-radius: 20px;
  z-index: 1000;
  padding: 0 12px;
  align-items: center;
  justify-content: space-around;
  border: 1px solid rgba(212, 175, 55, 0.3) !important;
  box-shadow: 0 8px 32px rgba(212, 175, 55, 0.15) !important;
}

.bottom-nav .nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #888;
  text-decoration: none;
  background: none;
  border: none;
  padding: 8px 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
  flex: 1;
}

.bottom-nav .nav-item span {
  font-size: 0.65rem;
  font-weight: 700;
}

.bottom-nav .nav-item.active {
  color: #D4AF37;
  background: rgba(212, 175, 55, 0.1);
}

.bottom-nav .nav-item:active {
  transform: scale(0.92);
}

/* ===== 移动端适配 ===== */
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .main-container {
    margin-left: 0 !important;
    padding-bottom: 80px; /* 为底部导航留出空间 */
  }

  .menu-btn {
    display: flex;
  }

  .content-area {
    padding: 16px;
  }

  .floating-glossary {
    display: none; /* 移动端使用底部导航的词典 */
  }

  .bottom-nav {
    display: flex;
  }
}
</style>
