<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>财务仪表盘</h1>
      <button v-if="canCreate" @click="showCreateModal = true" class="create-btn shimmer-btn">
        <Plus :size="16" />
        创建报表
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <!-- 空状态 -->
    <div v-else-if="financeStore.reports.length === 0" class="empty-state glass-card">
      <div class="empty-icon-wrap">
        <FolderOpen :size="48" class="empty-icon" />
      </div>
      <p class="empty-title">还没有财务报表</p>
      <p class="empty-desc">创建一个个人或家庭财务报表开始管理你的财务状况</p>
      <button @click="showCreateModal = true" class="create-btn shimmer-btn">
        <Plus :size="16" /> 创建报表
      </button>
    </div>

    <!-- 报表列表 -->
    <div v-else class="reports-grid bento-grid">
      <div
        v-for="(report, index) in financeStore.reports"
        :key="report.id"
        class="report-card bento-item spotlight-card"
        :class="{'bento-large': index === 0}"
      >
        <div class="report-card-header">
          <div class="header-left">
            <span class="report-badge shimmer-btn" :class="report.type === 'PERSONAL' ? 'badge-personal' : 'badge-family'">
              <component :is="report.type === 'PERSONAL' ? UserIcon : UsersIcon" :size="12" />
              {{ report.type === "PERSONAL" ? "个人" : "家庭" }}
            </span>
            <h3 class="report-title gold-text">{{ report.name }}</h3>
          </div>
          <el-popconfirm
            title="确定要删除该报表吗？所有相关数据将被清除。"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="handleDelete(report.id)"
          >
            <template #reference>
              <button class="delete-btn">
                <Trash2 :size="15" />
              </button>
            </template>
          </el-popconfirm>
        </div>

        <div class="report-links">
          <router-link :to="`/balance-sheet/${report.id}`" class="report-link glass-card">
            <span class="link-icon-wrap"><FileText :size="18" /></span>
            <div class="link-content">
              <div class="link-title">资产负债表</div>
              <div class="link-desc">管理资产和负债</div>
            </div>
            <ChevronRight :size="16" class="link-arrow" />
          </router-link>
          <router-link :to="`/income-expense/${report.id}`" class="report-link glass-card">
            <span class="link-icon-wrap"><DollarSign :size="18" /></span>
            <div class="link-content">
              <div class="link-title">收入支出表</div>
              <div class="link-desc">记录收支明细</div>
            </div>
            <ChevronRight :size="16" class="link-arrow" />
          </router-link>
          <router-link :to="`/cashflow/${report.id}`" class="report-link glass-card">
            <span class="link-icon-wrap"><TrendingUp :size="18" /></span>
            <div class="link-content">
              <div class="link-title">现金流表</div>
              <div class="link-desc">分析每月变动</div>
            </div>
            <ChevronRight :size="16" class="link-arrow" />
          </router-link>
        </div>
      </div>
    </div>

    <!-- 创建报表弹窗 -->
    <el-dialog
      v-model="showCreateModal"
      title="创建财务报表"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="newReport" :rules="formRules" label-position="top">
        <el-form-item label="报表名称" prop="name">
          <el-input v-model="newReport.name" placeholder="如：我的财务报表" />
        </el-form-item>
        <el-form-item label="报表类型" prop="type">
          <el-radio-group v-model="newReport.type" class="type-selector">
            <el-radio-button value="PERSONAL">
              <span class="type-option">
                <UserIcon :size="16" />
                <span>个人报表</span>
              </span>
            </el-radio-button>
            <el-radio-button value="FAMILY">
              <span class="type-option">
                <UsersIcon :size="16" />
                <span>家庭报表</span>
              </span>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-alert v-if="createError" :title="createError" type="error" :closable="false" show-icon />
      </el-form>
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useFinanceStore } from "../stores/finance";
import {
  Plus, FolderOpen, FileText, DollarSign, TrendingUp, ChevronRight,
  Trash2, User as UserIcon, Users as UsersIcon
} from 'lucide-vue-next';

const financeStore = useFinanceStore();
const showCreateModal = ref(false);
const formRef = ref(null);
const creating = ref(false);
const createError = ref("");

const newReport = reactive({ name: "", type: "PERSONAL" });

const formRules = {
  name: [{ required: true, message: "请输入报表名称", trigger: "blur" }],
  type: [{ required: true, message: "请选择报表类型", trigger: "change" }],
};

const canCreate = computed(() => {
  const reports = financeStore.reports;
  const hasPersonal = reports.some((r) => r.type === "PERSONAL");
  const hasFamily = reports.some((r) => r.type === "FAMILY");
  return !hasPersonal || !hasFamily;
});

async function handleCreate() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  createError.value = "";
  const reports = financeStore.reports;
  const hasType = reports.some((r) => r.type === newReport.type);
  if (hasType) {
    createError.value = `已存在${newReport.type === "PERSONAL" ? "个人" : "家庭"}报表，每种类型仅限创建一个`;
    return;
  }
  creating.value = true;
  try {
    await financeStore.createReport({ ...newReport });
    showCreateModal.value = false;
    newReport.name = "";
    newReport.type = "PERSONAL";
  } catch (e) {
    createError.value = e.response?.data?.message || "创建失败";
  } finally {
    creating.value = false;
  }
}

async function handleDelete(id) {
  await financeStore.deleteReport(id);
}

onMounted(() => {
  financeStore.fetchReports();
});
</script>

<style scoped>
.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 0.9rem;
  cursor: pointer;
}

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  border-radius: 24px;
  text-align: center;
  gap: 12px;
}

.empty-icon-wrap {
  width: 88px;
  height: 88px;
  background: linear-gradient(135deg, rgba(212,175,55,0.15) 0%, rgba(212,175,55,0.05) 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.empty-icon { color: #D4AF37; }
.empty-title { font-size: 1.1rem; font-weight: 700; color: #333; }
.empty-desc { font-size: 0.88rem; color: #909399; margin-bottom: 8px; }

/* Responsive adapters */
@media (max-width: 1024px) {
  .reports-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .reports-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .report-card, .bento-large {
    grid-column: span 1;
  }

  .report-title {
    font-size: 1.25rem;
  }

  .report-links {
    grid-template-columns: 1fr;
    padding: 0 16px 16px;
  }

  .report-link {
    padding: 12px;
  }

  .link-icon-wrap {
    width: 36px;
    height: 36px;
  }

  .link-title {
    font-size: 0.9rem;
  }
}

/* Reports grid */
.reports-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  grid-auto-flow: dense;
}

.report-card {
  grid-column: span 2;
  border-radius: 24px;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.bento-large {
  grid-column: span 4;
  grid-row: span 1;
}

.report-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px 24px 16px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.report-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 10px;
  width: fit-content;
}

.badge-personal {
  background: linear-gradient(135deg, #D4AF37 0%, #B8860B 100%);
  color: white;
}

.badge-family {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: white;
}

.report-title {
  font-size: 1.5rem;
  margin: 0;
}

.delete-btn {
  background: rgba(0,0,0,0.05);
  border: none;
  cursor: pointer;
  color: #c0c4cc;
  padding: 8px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.delete-btn:hover {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

/* Report links */
.report-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  padding: 0 24px 24px;
  margin-top: 8px;
}

.report-link {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.report-link:hover {
  transform: translateY(-4px);
  border-color: #D4AF37 !important;
}

.link-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(212, 175, 55, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #D4AF37;
  flex-shrink: 0;
}

.link-content { flex: 1; }

.link-title {
  font-weight: 700;
  font-size: 0.95rem;
  color: #333;
}

.link-desc {
  font-size: 0.8rem;
  color: #909399;
  margin-top: 2px;
}

.link-arrow {
  color: #c0c4cc;
  transition: all 0.3s ease;
}

.report-link:hover .link-arrow {
  color: #D4AF37;
  transform: translateX(4px);
}

/* Type selector */
.type-selector {
  display: flex;
  width: 100%;
}

.type-selector :deep(.el-radio-button) { flex: 1; }

.type-selector :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 14px;
}

.type-selector :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #D4AF37 0%, #C9A227 100%);
  border-color: #D4AF37 !important;
  box-shadow: none;
}

.type-option {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

@media (max-width: 768px) {
  .reports-grid { grid-template-columns: 1fr; }
}
</style>
