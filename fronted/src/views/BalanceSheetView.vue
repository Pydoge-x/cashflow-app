<template>
  <div class="balance-sheet">
    <div class="page-header">
      <h1>资产负债表</h1>
      <button @click="openAddModal" class="create-btn shimmer-btn">
        <Plus :size="16" />
        添加条目
      </button>
    </div>

    <!-- 汇总区域 -->
    <div class="summary-grid">
      <div class="summary-card glass-card spotlight-card" style="--accent: #52c41a">
        <div class="label">总资产</div>
        <div class="value" style="color: #52c41a">
          ¥{{ formatNum(totalAssets) }}
        </div>
        <div class="card-icon-bg"><CircleDollarSign :size="48" /></div>
      </div>
      <div class="summary-card glass-card spotlight-card" style="--accent: #ff4d4f">
        <div class="label">总负债</div>
        <div class="value" style="color: #ff4d4f">
          ¥{{ formatNum(totalDebts) }}
        </div>
        <div class="card-icon-bg"><CreditCard :size="48" /></div>
      </div>
      <div class="summary-card glass-card spotlight-card" style="--accent: #D4AF37">
        <div class="label">净资产</div>
        <div
          class="value"
          :style="{ color: netWorth >= 0 ? '#52c41a' : '#ff4d4f' }"
        >
          ¥{{ formatNum(netWorth) }}
        </div>
        <div class="card-icon-bg"><PiggyBank :size="48" /></div>
      </div>
    </div>

    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <template v-else>
      <!-- 资产部分 -->
      <div class="section-card glass-card spotlight-card">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap asset"><CircleDollarSign :size="18" /></div>
            <span>流动资产表</span>
          </div>
          <el-tag type="success" effect="plain" class="header-badge">¥{{ formatNum(totalAssets) }}</el-tag>
        </div>

        <template v-for="(cat, catKey) in assetCategories" :key="catKey">
          <div class="section-divider">{{ cat.label }}</div>
          <el-table
            v-if="getItems(catKey).length > 0"
            :data="getItems(catKey)"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="name" label="名称" />
            <el-table-column label="金额 (¥)">
              <template #default="{ row }">
                <span class="amount positive">{{ formatNum(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="note" label="备注">
              <template #default="{ row }">
                <span style="color: #909399">{{ row.note || "-" }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" color="#fffff" text @click="openEditModal(row)">编辑</el-button>
                <el-popconfirm
                  title="确定删除此条目？"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  @confirm="handleDelete(row.id)"
                >
                  <template #reference>
                    <el-button type="danger" size="small" color="#fffff" text>删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </template>
      </div>

      <!-- 负债部分 -->
      <div class="section-card glass-card spotlight-card" style="margin-top: 24px">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap debt"><CreditCard :size="18" /></div>
            <span>家庭负债表</span>
          </div>
          <el-tag type="danger" effect="plain" class="header-badge">¥{{ formatNum(totalDebts) }}</el-tag>
        </div>

        <template v-for="(cat, catKey) in debtCategories" :key="catKey">
          <div class="section-divider">{{ cat.label }}</div>
          <el-table
            v-if="getItems(catKey).length > 0"
            :data="getItems(catKey)"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="name" label="名称" />
            <el-table-column label="金额 (¥)">
              <template #default="{ row }">
                <span class="amount negative">{{ formatNum(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="利息 (¥/月)">
              <template #default="{ row }">
                <span v-if="row.isInterest" style="color: #faad14">{{ formatNum(row.interestAmount) }}</span>
                <span v-else style="color: #c0c4cc">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="note" label="备注">
              <template #default="{ row }">
                <span style="color: #909399">{{ row.note || "-" }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" color="#fffff" text @click="openEditModal(row)">编辑</el-button>
                <el-popconfirm
                  title="确定删除此条目？"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  @confirm="handleDelete(row.id)"
                >
                  <template #reference>
                    <el-button type="danger" size="small" color="#fffff"text>删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </template>
      </div>
    </template>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="showModal"
      :title="editingItem ? '编辑条目' : '添加条目'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option-group label="资产">
              <el-option label="流动资产" value="CURRENT_ASSET" />
              <el-option label="投资性资产" value="INVESTMENT_ASSET" />
              <el-option label="自用资产" value="PERSONAL_ASSET" />
            </el-option-group>
            <el-option-group label="负债">
              <el-option label="消费负债" value="CONSUMER_DEBT" />
              <el-option label="投资负债" value="INVESTMENT_DEBT" />
              <el-option label="自用资产负债" value="PERSONAL_DEBT" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：活期存款" />
        </el-form-item>
        <el-form-item label="金额 (¥)" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0"
            :precision="2"
            :step="1000"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="2" placeholder="补充说明..." />
        </el-form-item>

        <template v-if="isDebtCategory">
          <el-form-item>
            <el-checkbox v-model="form.isInterest">月利息支出（非本金部分）</el-checkbox>
          </el-form-item>
          <el-form-item v-if="form.isInterest" label="月利息金额 (¥/月)">
            <el-input-number
              v-model="form.interestAmount"
              :min="0"
              :precision="2"
              :step="100"
              style="width: 100%"
              controls-position="right"
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ editingItem ? "保存" : "添加" }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useFinanceStore } from "../stores/finance";
import { Plus, CircleDollarSign, CreditCard, PiggyBank } from 'lucide-vue-next';

const route = useRoute();
const financeStore = useFinanceStore();
const reportId = computed(() => route.params.reportId);

const showModal = ref(false);
const formRef = ref(null);
const submitting = ref(false);
const editingItem = ref(null);
const form = reactive({
  category: "CURRENT_ASSET",
  name: "",
  amount: 0,
  note: "",
  isInterest: false,
  interestAmount: 0,
});

const formRules = {
  category: [{ required: true, message: "请选择分类", trigger: "change" }],
  name: [{ required: true, message: "请输入名称", trigger: "blur" }],
  amount: [{ required: true, message: "请输入金额", trigger: "blur" }],
};

const isDebtCategory = computed(() => {
  return ["CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"].includes(form.category);
});

const assetCategories = {
  CURRENT_ASSET: { label: "流动资产" },
  INVESTMENT_ASSET: { label: "投资性资产" },
  PERSONAL_ASSET: { label: "自用资产" },
};

const debtCategories = {
  CONSUMER_DEBT: { label: "消费负债" },
  INVESTMENT_DEBT: { label: "投资负债" },
  PERSONAL_DEBT: { label: "自用资产负债" },
};

function getItems(category) {
  return financeStore.balanceSheet.filter((i) => i.category === category);
}

const totalAssets = computed(() => {
  return financeStore.balanceSheet
    .filter((i) => ["CURRENT_ASSET", "INVESTMENT_ASSET", "PERSONAL_ASSET"].includes(i.category))
    .reduce((s, i) => s + (i.amount || 0), 0);
});

const totalDebts = computed(() => {
  return financeStore.balanceSheet
    .filter((i) => ["CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"].includes(i.category))
    .reduce((s, i) => s + (i.amount || 0), 0);
});

const netWorth = computed(() => totalAssets.value - totalDebts.value);

function formatNum(n) {
  return (n || 0).toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function openAddModal() {
  editingItem.value = null;
  Object.assign(form, {
    category: "CURRENT_ASSET",
    name: "",
    amount: 0,
    note: "",
    isInterest: false,
    interestAmount: 0,
  });
  showModal.value = true;
}

function openEditModal(item) {
  editingItem.value = item;
  Object.assign(form, { ...item });
  showModal.value = true;
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    let savedItem;
    if (editingItem.value) {
      savedItem = await financeStore.updateBalanceSheetItem(
        reportId.value,
        editingItem.value.id,
        { ...form }
      );
    } else {
      savedItem = await financeStore.addBalanceSheetItem(reportId.value, { ...form });
    }

    const targetName = editingItem.value ? editingItem.value.name : form.name;
    const ieItem = financeStore.incomeExpense.find(i => i.name === targetName);
    if (ieItem && isDebtCategory.value) {
      await financeStore.updateIncomeExpenseItem(reportId.value, ieItem.id, {
        ...ieItem,
        name: form.name,
        amount: form.amount,
        isInterest: form.isInterest,
        interestAmount: form.interestAmount,
        note: form.note
      });
    }

    showModal.value = false;
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(itemId) {
  await financeStore.deleteBalanceSheetItem(reportId.value, itemId);
}

onMounted(async () => {
  await Promise.all([
    financeStore.fetchBalanceSheet(reportId.value),
    financeStore.fetchIncomeExpense(reportId.value)
  ]);
});
</script>

<style scoped>
.section-card {
  padding: 0;
  overflow: hidden;
  border-radius: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.12);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  font-size: 1.1rem;
}

.icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrap.asset {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.icon-wrap.debt {
  background: rgba(255, 77, 79, 0.1);
  color: #ff4d4f;
}

.header-badge {
  border-radius: 8px;
  font-weight: 700;
}

.summary-card {
  position: relative;
  overflow: hidden;
  padding: 24px;
  border-radius: 20px;
}

.card-icon-bg {
  position: absolute;
  right: -10px;
  bottom: -10px;
  opacity: 0.05;
  transform: rotate(-15deg);
}

.summary-card .label {
  font-size: 0.85rem;
  color: #909399;
  font-weight: 600;
  margin-bottom: 8px;
}

.summary-card .value {
  font-size: 1.8rem;
  font-weight: 800;
}

.amount {
  font-weight: 700;
}

.amount.positive {
  color: #52c41a;
}

.amount.negative {
  color: #ff4d4f;
}

:deep(.el-table) {
  padding: 0 16px;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 0.9rem;
  cursor: pointer;
}
</style>
