<template>
  <div class="income-expense">
    <div class="page-header">
      <h1>收入支出表</h1>
      <button @click="openAddModal" class="create-btn shimmer-btn">
        <Plus :size="16" />
        添加条目
      </button>
    </div>

    <!-- 汇总区域 -->
    <div class="summary-grid">
      <div class="summary-card glass-card spotlight-card" style="--accent: #52c41a">
        <div class="label">总收入</div>
        <div class="value" style="color: #52c41a">
          ¥{{ formatNum(totalIncome) }}
        </div>
        <div class="card-icon-bg"><TrendingUp :size="48" /></div>
      </div>
      <div class="summary-card glass-card spotlight-card" style="--accent: #ff4d4f">
        <div class="label">总支出</div>
        <div class="value" style="color: #ff4d4f">
          ¥{{ formatNum(totalExpense) }}
        </div>
        <div class="card-icon-bg"><TrendingDown :size="48" /></div>
      </div>
      <div class="summary-card glass-card spotlight-card" style="--accent: #D4AF37">
        <div class="label">年度结余</div>
        <div
          class="value"
          :style="{ color: balance >= 0 ? '#52c41a' : '#ff4d4f' }"
        >
          ¥{{ formatNum(balance) }}
        </div>
        <div class="card-icon-bg"><Wallet :size="48" /></div>
      </div>
    </div>

    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <template v-else>
      <div class="section-card glass-card spotlight-card">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap income"><TrendingUp :size="18" /></div>
            <span>收入表单</span>
          </div>
          <el-tag type="success" effect="plain" class="header-badge">¥{{ formatNum(totalIncome) }}</el-tag>
        </div>

        <template v-for="(cat, catKey) in incomeCategories" :key="catKey">
          <div class="section-divider">{{ cat.label }}</div>
          <el-table
            v-if="getItems('INCOME', catKey).length > 0"
            :data="getItems('INCOME', catKey)"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="name" label="名称" />
            <el-table-column label="金额 (¥/月)">
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

      <!-- 支出部分 -->
      <div class="section-card glass-card spotlight-card" style="margin-top: 24px">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap expense"><TrendingDown :size="18" /></div>
            <span>支出表单</span>
          </div>
          <el-tag type="danger" effect="plain" class="header-badge">¥{{ formatNum(totalExpense) }}</el-tag>
        </div>

        <template v-for="(cat, catKey) in expenseCategories" :key="catKey">
          <div class="section-divider">{{ cat.label }}</div>
          <el-table
            v-if="getItems('EXPENSE', catKey).length > 0"
            :data="getItems('EXPENSE', catKey)"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="name" label="名称" />
            <el-table-column label="金额 (¥)">
              <template #default="{ row }">
                <span class="amount negative">{{ formatNum(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="利息额 (¥/月)">
              <template #default="{ row }">
                <span v-if="row.isInterest" style="color: #faad14">{{ formatNum(row.interestAmount || 0) }}</span>
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
                <el-button 
                  type="primary" 
                  size="small" 
                  text 
                  @click="openEditModal(row)"
                  :disabled="row.isSync"
                >
                  编辑
                </el-button>
                <el-popconfirm
                  v-if="!row.isSync"
                  title="确定删除此条目？"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  @confirm="handleDelete(row.id)"
                >
                  <template #reference>
                    <el-button type="danger" size="small" color="#fffff" text>删除</el-button>
                  </template>
                </el-popconfirm>
                <el-tooltip v-else content="来自资产负债表的同步项，无法直接删除" placement="top">
                  <el-button type="info" size="small" text disabled>同步项</el-button>
                </el-tooltip>
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
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" style="width: 100%">
                <el-option label="收入" value="INCOME" />
                <el-option label="支出" value="EXPENSE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" style="width: 100%">
                <template v-if="form.type === 'INCOME'">
                  <el-option label="劳动收入（主动收入）" value="LABOR_INCOME" />
                  <el-option label="资产收入（被动收入）" value="ASSET_INCOME" />
                </template>
                <template v-else>
                  <el-option label="生活支出" value="LIVING_EXPENSE" />
                  <el-option label="资产性支出" value="ASSET_EXPENSE" />
                  <el-option label="借款还款" value="LOAN_REPAYMENT" />
                </template>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：工资收入" />
        </el-form-item>
        <el-form-item label="月金额 (¥)" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0"
            :precision="2"
            :step="1000"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item v-if="form.type === 'EXPENSE'">
          <el-checkbox v-model="form.isInterest">标记为利息（计入现金流）</el-checkbox>
        </el-form-item>
        <el-form-item v-if="form.isInterest" label="利息金额 (¥)">
          <el-input-number
            v-model="form.interestAmount"
            :min="0"
            :precision="2"
            :step="100"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" placeholder="选填" />
        </el-form-item>
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
import { ref, reactive, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useFinanceStore } from "../stores/finance";
import { Plus, TrendingUp, TrendingDown, Wallet, Info } from 'lucide-vue-next';

const route = useRoute();
const financeStore = useFinanceStore();
const reportId = computed(() => route.params.reportId);

const showModal = ref(false);
const formRef = ref(null);
const submitting = ref(false);
const editingItem = ref(null);
const form = reactive({
  type: "INCOME",
  category: "LABOR_INCOME",
  name: "",
  amount: 0,
  isInterest: false,
  interestAmount: 0,
  note: "",
});

const formRules = {
  type: [{ required: true, message: "请选择类型", trigger: "change" }],
  category: [{ required: true, message: "请选择分类", trigger: "change" }],
  name: [{ required: true, message: "请输入名称", trigger: "blur" }],
  amount: [{ required: true, message: "请输入金额", trigger: "blur" }],
};

watch(
  () => form.type,
  (newType) => {
    form.category = newType === "INCOME" ? "LABOR_INCOME" : "LIVING_EXPENSE";
    if (newType === "INCOME") {
      form.isInterest = false;
      form.interestAmount = 0;
    }
  }
);

const incomeCategories = {
  LABOR_INCOME: { label: "劳动收入（主动收入）" },
  ASSET_INCOME: { label: "资产收入（被动收入）" },
};

const expenseCategories = {
  LIVING_EXPENSE: { label: "生活支出" },
  ASSET_EXPENSE: { label: "资产性支出" },
  LOAN_REPAYMENT: { label: "借款还款" },
};

function getItems(type, category) {
  const originalItems = financeStore.incomeExpense.filter(
    (i) => i.type === type && i.category === category
  );

  if (type === "EXPENSE" && category === "ASSET_EXPENSE") {
    const debts = financeStore.balanceSheet.filter((i) =>
      ["CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"].includes(i.category)
    );

    const syncedItems = debts.map((debt) => {
      const existing = originalItems.find((oi) => oi.name === debt.name);
      return existing || {
        id: `sync-debt-${debt.id}`,
        name: debt.name,
        amount: debt.amount || 0,
        note: `来自负债：${debt.name}`,
        isSync: true,
        type: "EXPENSE",
        category: "ASSET_EXPENSE",
        isInterest: debt.isInterest,
        interestAmount: debt.interestAmount || 0,
      };
    });

    const debtNames = new Set(debts.map(d => d.name));
    const extraItems = originalItems.filter(oi => !debtNames.has(oi.name));

    return [...syncedItems, ...extraItems];
  }

  return originalItems;
}

const allIncomeItems = computed(() => {
  const categories = Object.keys(incomeCategories);
  let all = [];
  categories.forEach(cat => {
    all = [...all, ...getItems('INCOME', cat)];
  });
  return all;
});

const allExpenseItems = computed(() => {
  const categories = Object.keys(expenseCategories);
  let all = [];
  categories.forEach(cat => {
    all = [...all, ...getItems('EXPENSE', cat)];
  });
  return all;
});

const totalIncome = computed(() => {
  return allIncomeItems.value.reduce((s, i) => s + (i.amount || 0), 0);
});

const totalExpense = computed(() => {
  return allExpenseItems.value.reduce((s, i) => {
    if (i.category === 'ASSET_EXPENSE' || i.category === 'LOAN_REPAYMENT') {
      return s + (i.interestAmount || 0);
    }
    return s + (i.amount || 0);
  }, 0);
});

const balance = computed(() => totalIncome.value - totalExpense.value);

function formatNum(n) {
  return (n || 0).toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function openAddModal() {
  editingItem.value = null;
  Object.assign(form, {
    type: "INCOME",
    category: "LABOR_INCOME",
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
  if (typeof item.id === 'string' && item.id.startsWith('sync-')) {
    Object.assign(form, { ...item, id: undefined });
  } else {
    Object.assign(form, {
      ...item,
      isInterest: item.isInterest || false,
      interestAmount: item.interestAmount || 0,
    });
  }
  showModal.value = true;
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    let savedItem;
    if (editingItem.value && editingItem.value.id && !editingItem.value.isSync) {
      savedItem = await financeStore.updateIncomeExpenseItem(
        reportId.value,
        editingItem.value.id,
        { ...form }
      );
    } else {
      savedItem = await financeStore.addIncomeExpenseItem(reportId.value, { ...form });
    }

    const bsItem = financeStore.balanceSheet.find(i => i.name === form.name);
    const debtCategories = ["CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"];
    if (bsItem && debtCategories.includes(bsItem.category)) {
      await financeStore.updateBalanceSheetItem(reportId.value, bsItem.id, {
        ...bsItem,
        amount: form.amount,
        isInterest: form.isInterest,
        interestAmount: form.interestAmount
      });
    }

    showModal.value = false;
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(itemId) {
  if (typeof itemId === "string" && itemId.startsWith("sync-")) {
    return;
  }
  await financeStore.deleteIncomeExpenseItem(reportId.value, itemId);
}

onMounted(async () => {
  await Promise.all([
    financeStore.fetchIncomeExpense(reportId.value),
    financeStore.fetchBalanceSheet(reportId.value),
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

.icon-wrap.income {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.icon-wrap.expense {
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
