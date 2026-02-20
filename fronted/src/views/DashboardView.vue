<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>📊 财务仪表盘</h1>
      <el-button
        type="primary"
        @click="showCreateModal = true"
        v-if="canCreate"
      >
        <el-icon><Plus /></el-icon>
        创建报表
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <!-- 空状态 -->
    <el-empty
      v-else-if="financeStore.reports.length === 0"
      description="还没有财务报表"
      :image-size="120"
    >
      <template #description>
        <p style="color: #909399; margin-bottom: 8px;">还没有财务报表</p>
        <p style="color: #c0c4cc; font-size: 0.88rem;">创建一个个人或家庭财务报表开始管理你的财务状况</p>
      </template>
      <el-button type="primary" @click="showCreateModal = true">
        创建报表
      </el-button>
    </el-empty>

    <!-- 报表列表 -->
    <div v-else class="reports-grid">
      <el-card
        v-for="report in financeStore.reports"
        :key="report.id"
        class="report-card"
        shadow="hover"
      >
        <template #header>
          <div class="report-card-header">
            <div>
              <el-tag
                :type="report.type === 'PERSONAL' ? 'warning' : 'success'"
                effect="plain"
              >
                {{ report.type === "PERSONAL" ? "👤 个人" : "👨‍👩‍👧‍👦 家庭" }}
              </el-tag>
              <h3>{{ report.name }}</h3>
            </div>
            <el-popconfirm
              title="确定要删除该报表吗？所有相关数据将被清除。"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(report.id)"
            >
              <template #reference>
                <el-button type="danger" size="small" text color="white">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </template>

        <div class="report-links">
          <router-link :to="`/balance-sheet/${report.id}`" class="report-link">
            <el-icon class="link-icon"><Document /></el-icon>
            <div class="link-content">
              <div class="link-title">资产负债表</div>
              <div class="link-desc">管理资产和负债数据</div>
            </div>
            <el-icon class="link-arrow"><ArrowRight /></el-icon>
          </router-link>
          <router-link :to="`/income-expense/${report.id}`" class="report-link">
            <el-icon class="link-icon"><Money /></el-icon>
            <div class="link-content">
              <div class="link-title">收入支出表</div>
              <div class="link-desc">记录收入和支出明细</div>
            </div>
            <el-icon class="link-arrow"><ArrowRight /></el-icon>
          </router-link>
          <router-link :to="`/cashflow/${report.id}`" class="report-link">
            <el-icon class="link-icon"><TrendCharts /></el-icon>
            <div class="link-content">
              <div class="link-title">现金流表</div>
              <div class="link-desc">自动计算每月现金流</div>
            </div>
            <el-icon class="link-arrow"><ArrowRight /></el-icon>
          </router-link>
        </div>
      </el-card>
    </div>

    <!-- 创建报表弹窗 -->
    <el-dialog
      v-model="showCreateModal"
      title="创建财务报表"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="newReport"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="报表名称" prop="name">
          <el-input
            v-model="newReport.name"
            placeholder="如：我的财务报表"
          />
        </el-form-item>
        <el-form-item label="报表类型" prop="type">
          <el-radio-group v-model="newReport.type" class="type-selector">
            <el-radio-button value="PERSONAL">
              <span class="type-option">
                <span class="type-icon">👤</span>
                <span>个人报表</span>
              </span>
            </el-radio-button>
            <el-radio-button value="FAMILY">
              <span class="type-option">
                <span class="type-icon">👨‍👩‍👧‍👦</span>
                <span>家庭报表</span>
              </span>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-alert
          v-if="createError"
          :title="createError"
          type="error"
          :closable="false"
          show-icon
        />
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
import { Plus, Document, Money, TrendCharts, ArrowRight } from '@element-plus/icons-vue';

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
.reports-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 24px;
}

.report-card {
  border-radius: 16px;
}

.report-card :deep(.el-card__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #F0E8D0;
}

.report-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.report-card-header h3 {
  font-size: 1.2rem;
  margin-top: 8px;
  font-weight: 700;
  color: #333;
}

.report-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.report-link {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fffdf5;
  border: 1px solid #F0E8D0;
  border-radius: 12px;
  color: #333;
  text-decoration: none;
  transition: all 0.3s ease;
}

.report-link:hover {
  border-color: #D4AF37;
  background: #fef9e7;
  transform: translateX(6px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.15);
}

.link-icon {
  font-size: 1.5rem;
  color: #D4AF37;
}

.link-content {
  flex: 1;
}

.link-title {
  font-weight: 600;
  font-size: 0.9rem;
  color: #333;
}

.link-desc {
  font-size: 0.78rem;
  color: #909399;
  margin-top: 2px;
}

.link-arrow {
  color: #c0c4cc;
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

.report-link:hover .link-arrow {
  color: #D4AF37;
  transform: translateX(4px);
}

/* 报表类型选择 */
.type-selector {
  display: flex;
  width: 100%;
}

.type-selector :deep(.el-radio-button) {
  flex: 1;
}

.type-selector :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 16px;
  border-radius: 12px !important;
  border: 1px solid #E8D5A3 !important;
  background: #fffdf5;
}

.type-selector :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 12px 0 0 12px !important;
}

.type-selector :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 12px 12px 0 !important;
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
}

.type-icon {
  font-size: 1.25rem;
}

@media (max-width: 768px) {
  .reports-grid {
    grid-template-columns: 1fr;
  }
}
</style>
