<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>📊 财务仪表盘</h1>
      <button
        class="btn btn-primary"
        @click="showCreateModal = true"
        v-if="canCreate"
      >
        ＋ 创建报表
      </button>
    </div>

    <!-- 报表列表 -->
    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <div v-else-if="financeStore.reports.length === 0" class="empty-state">
      <div class="icon">📋</div>
      <p>还没有财务报表</p>
      <p>创建一个个人或家庭财务报表开始管理你的财务状况</p>
      <button class="btn btn-primary" @click="showCreateModal = true">
        创建报表
      </button>
    </div>

    <div v-else class="reports-grid">
      <div
        v-for="report in financeStore.reports"
        :key="report.id"
        class="report-card card"
      >
        <div class="report-card-header">
          <div>
            <span
              class="badge"
              :class="
                report.type === 'PERSONAL' ? 'badge-personal' : 'badge-family'
              "
            >
              {{ report.type === "PERSONAL" ? "👤 个人" : "👨‍👩‍👧‍👦 家庭" }}
            </span>
            <h3>{{ report.name }}</h3>
          </div>
          <button
            class="btn btn-danger btn-sm"
            @click="handleDelete(report.id)"
          >
            删除
          </button>
        </div>

        <div class="report-links">
          <router-link :to="`/balance-sheet/${report.id}`" class="report-link">
            <span class="link-icon">📋</span>
            <div>
              <div class="link-title">资产负债表</div>
              <div class="link-desc">管理资产和负债数据</div>
            </div>
            <span class="link-arrow">→</span>
          </router-link>
          <router-link :to="`/income-expense/${report.id}`" class="report-link">
            <span class="link-icon">💵</span>
            <div>
              <div class="link-title">收入支出表</div>
              <div class="link-desc">记录收入和支出明细</div>
            </div>
            <span class="link-arrow">→</span>
          </router-link>
          <router-link :to="`/cashflow/${report.id}`" class="report-link">
            <span class="link-icon">📈</span>
            <div>
              <div class="link-title">现金流表</div>
              <div class="link-desc">自动计算每月现金流</div>
            </div>
            <span class="link-arrow">→</span>
          </router-link>
        </div>
      </div>
    </div>

    <!-- 创建报表弹窗 -->
    <div
      class="modal-overlay"
      v-if="showCreateModal"
      @click.self="showCreateModal = false"
    >
      <div class="modal">
        <h3>创建财务报表</h3>
        <form @submit.prevent="handleCreate">
          <div class="form-group">
            <label>报表名称</label>
            <input
              v-model="newReport.name"
              type="text"
              placeholder="如：我的财务报表"
              required
            />
          </div>
          <div class="form-group">
            <label>报表类型</label>
            <div class="type-selector">
              <label
                class="type-option"
                :class="{ active: newReport.type === 'PERSONAL' }"
              >
                <input type="radio" v-model="newReport.type" value="PERSONAL" />
                <span class="type-icon">👤</span>
                <span>个人报表</span>
              </label>
              <label
                class="type-option"
                :class="{ active: newReport.type === 'FAMILY' }"
              >
                <input type="radio" v-model="newReport.type" value="FAMILY" />
                <span class="type-icon">👨‍👩‍👧‍👦</span>
                <span>家庭报表</span>
              </label>
            </div>
          </div>
          <div v-if="createError" class="auth-error">{{ createError }}</div>
          <div class="modal-actions">
            <button
              type="button"
              class="btn btn-secondary"
              @click="showCreateModal = false"
            >
              取消
            </button>
            <button type="submit" class="btn btn-primary">创建</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useFinanceStore } from "../stores/finance";

const financeStore = useFinanceStore();
const showCreateModal = ref(false);
const newReport = ref({ name: "", type: "PERSONAL" });
const createError = ref("");

// 限制：个人和家庭各一个
const canCreate = computed(() => {
  const reports = financeStore.reports;
  const hasPersonal = reports.some((r) => r.type === "PERSONAL");
  const hasFamily = reports.some((r) => r.type === "FAMILY");
  return !hasPersonal || !hasFamily;
});

async function handleCreate() {
  createError.value = "";
  const reports = financeStore.reports;
  const hasType = reports.some((r) => r.type === newReport.value.type);
  if (hasType) {
    createError.value = `已存在${newReport.value.type === "PERSONAL" ? "个人" : "家庭"}报表，每种类型仅限创建一个`;
    return;
  }
  try {
    await financeStore.createReport(newReport.value);
    showCreateModal.value = false;
    newReport.value = { name: "", type: "PERSONAL" };
  } catch (e) {
    createError.value = e.response?.data?.message || "创建失败";
  }
}

async function handleDelete(id) {
  if (confirm("确定要删除该报表吗？所有相关数据将被清除。")) {
    await financeStore.deleteReport(id);
  }
}

onMounted(() => {
  financeStore.fetchReports();
});
</script>

<style scoped>
.reports-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 1.5rem;
}

.report-card {
  overflow: hidden;
}

.report-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 1.2rem;
}

.report-card-header h3 {
  font-size: 1.3rem;
  margin-top: 0.6rem;
  font-weight: 700;
  color: #fff;
}

.report-links {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.report-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  background: var(--color-bg-input);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text);
  text-decoration: none;
  transition: var(--transition);
  position: relative;
}

.report-link:hover {
  border-color: hsla(var(--h-primary), var(--s-primary), var(--l-primary), 0.4);
  background: var(--color-bg-card-hover);
  transform: translateX(6px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.link-icon {
  font-size: 1.5rem;
}

.link-title {
  font-weight: 600;
  font-size: 0.9rem;
}

.link-desc {
  font-size: 0.78rem;
  color: var(--color-text-muted);
}

.link-arrow {
  margin-left: auto;
  color: var(--color-text-muted);
  font-size: 1.2rem;
  transition: var(--transition);
}

.report-link:hover .link-arrow {
  color: var(--color-primary);
  transform: translateX(4px);
}

/* 报表类型选择 */
.type-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.8rem;
}

.type-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  background: var(--color-bg);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition);
  font-size: 0.9rem;
  font-weight: 500;
}

.type-option input {
  display: none;
}

.type-option.active {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}

.type-icon {
  font-size: 1.3rem;
}

.auth-error {
  background: var(--color-danger-bg);
  color: var(--color-danger);
  padding: 0.6rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.82rem;
  margin-bottom: 1rem;
}

@media (max-width: 768px) {
  .reports-grid {
    grid-template-columns: 1fr;
  }
}
</style>
