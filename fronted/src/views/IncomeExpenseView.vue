<template>
  <div class="income-expense">
    <div class="page-header">
      <h1>💵 收入支出表</h1>
      <button class="btn btn-primary" @click="openAddModal">＋ 添加条目</button>
    </div>

    <!-- 汇总区域 -->
    <div class="summary-grid">
      <div class="summary-card" style="--accent: #22c55e">
        <div class="label">总收入</div>
        <div class="value" style="color: var(--color-success)">
          ¥{{ formatNum(totalIncome) }}
        </div>
      </div>
      <div class="summary-card" style="--accent: #ef4444">
        <div class="label">总支出</div>
        <div class="value" style="color: var(--color-danger)">
          ¥{{ formatNum(totalExpense) }}
        </div>
      </div>
      <div class="summary-card" style="--accent: #6366f1">
        <div class="label">结余</div>
        <div
          class="value"
          :style="{
            color:
              balance >= 0 ? 'var(--color-success)' : 'var(--color-danger)',
          }"
        >
          ¥{{ formatNum(balance) }}
        </div>
      </div>
    </div>

    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <template v-else>
      <!-- 收入部分 -->
      <div class="card" style="margin-bottom: 1.5rem">
        <div class="card-header">
          <h3>📥 收入</h3>
          <span class="badge badge-income">¥{{ formatNum(totalIncome) }}</span>
        </div>

        <template v-for="(cat, catKey) in incomeCategories" :key="catKey">
          <div class="section-divider">{{ cat.label }}</div>
          <table
            class="data-table"
            v-if="getItems('INCOME', catKey).length > 0"
          >
            <thead>
              <tr>
                <th>名称</th>
                <th>金额 (¥/月)</th>
                <th>备注</th>
                <th style="text-align: right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in getItems('INCOME', catKey)" :key="item.id">
                <td>{{ item.name }}</td>
                <td>
                  <span class="amount positive">{{
                    formatNum(item.amount)
                  }}</span>
                </td>
                <td style="color: var(--color-text-muted); font-size: 0.82rem">
                  {{ item.note || "-" }}
                </td>
                <td>
                  <div class="actions">
                    <button
                      class="btn btn-secondary btn-sm"
                      @click="openEditModal(item)"
                    >
                      编辑
                    </button>
                    <button
                      class="btn btn-danger btn-sm"
                      @click="handleDelete(item.id)"
                    >
                      删除
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-row">暂无数据</div>
        </template>
      </div>

      <!-- 支出部分 -->
      <div class="card">
        <div class="card-header">
          <h3>📤 支出</h3>
          <span class="badge badge-expense"
            >¥{{ formatNum(totalExpense) }}</span
          >
        </div>

        <template v-for="(cat, catKey) in expenseCategories" :key="catKey">
          <div class="section-divider">{{ cat.label }}</div>
          <table
            class="data-table"
            v-if="getItems('EXPENSE', catKey).length > 0"
          >
            <thead>
              <tr>
                <th>名称</th>
                <th>金额 (¥/月)</th>
                <th>是否利息</th>
                <th>备注</th>
                <th style="text-align: right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in getItems('EXPENSE', catKey)" :key="item.id">
                <td>{{ item.name }}</td>
                <td>
                  <span class="amount negative">{{
                    formatNum(item.amount)
                  }}</span>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="item.isInterest ? 'badge-debt' : 'badge-personal'"
                  >
                    {{ item.isInterest ? "利息" : "本金" }}
                  </span>
                </td>
                <td style="color: var(--color-text-muted); font-size: 0.82rem">
                  {{ item.note || "-" }}
                </td>
                <td>
                  <div class="actions">
                    <button
                      class="btn btn-secondary btn-sm"
                      @click="openEditModal(item)"
                    >
                      编辑
                    </button>
                    <button
                      class="btn btn-danger btn-sm"
                      @click="handleDelete(item.id)"
                    >
                      删除
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-row">暂无数据</div>
        </template>
      </div>
    </template>

    <!-- 添加/编辑弹窗 -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ editingItem ? "编辑条目" : "添加条目" }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-row">
            <div class="form-group">
              <label>类型</label>
              <select v-model="form.type" required>
                <option value="INCOME">收入</option>
                <option value="EXPENSE">支出</option>
              </select>
            </div>
            <div class="form-group">
              <label>分类</label>
              <select v-model="form.category" required>
                <template v-if="form.type === 'INCOME'">
                  <option value="LABOR_INCOME">劳动收入（主动收入）</option>
                  <option value="ASSET_INCOME">资产收入（被动收入）</option>
                </template>
                <template v-else>
                  <option value="LIVING_EXPENSE">生活支出</option>
                  <option value="ASSET_EXPENSE">资产性支出</option>
                  <option value="LOAN_REPAYMENT">借款还款</option>
                </template>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>名称</label>
            <input
              v-model="form.name"
              type="text"
              placeholder="如：工资收入"
              required
            />
          </div>
          <div class="form-group">
            <label>月金额 (¥)</label>
            <input
              v-model.number="form.amount"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
              required
            />
          </div>
          <div class="form-group" v-if="form.type === 'EXPENSE'">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.isInterest" />
              <span>标记为利息支出（计入现金流支出）</span>
            </label>
          </div>
          <div class="form-group">
            <label>备注</label>
            <input v-model="form.note" type="text" placeholder="选填" />
          </div>
          <div class="modal-actions">
            <button
              type="button"
              class="btn btn-secondary"
              @click="showModal = false"
            >
              取消
            </button>
            <button type="submit" class="btn btn-primary">
              {{ editingItem ? "保存" : "添加" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useFinanceStore } from "../stores/finance";

const route = useRoute();
const financeStore = useFinanceStore();
const reportId = computed(() => route.params.reportId);

const showModal = ref(false);
const editingItem = ref(null);
const form = ref({
  type: "INCOME",
  category: "LABOR_INCOME",
  name: "",
  amount: "",
  isInterest: false,
  note: "",
});

// 切换类型时重设分类
watch(
  () => form.value.type,
  (newType) => {
    form.value.category =
      newType === "INCOME" ? "LABOR_INCOME" : "LIVING_EXPENSE";
    if (newType === "INCOME") form.value.isInterest = false;
  },
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
  return financeStore.incomeExpense.filter(
    (i) => i.type === type && i.category === category,
  );
}

const totalIncome = computed(() => {
  return financeStore.incomeExpense
    .filter((i) => i.type === "INCOME")
    .reduce((s, i) => s + (i.amount || 0), 0);
});

const totalExpense = computed(() => {
  return financeStore.incomeExpense
    .filter((i) => i.type === "EXPENSE")
    .reduce((s, i) => s + (i.amount || 0), 0);
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
  form.value = {
    type: "INCOME",
    category: "LABOR_INCOME",
    name: "",
    amount: "",
    isInterest: false,
    note: "",
  };
  showModal.value = true;
}

function openEditModal(item) {
  editingItem.value = item;
  form.value = { ...item };
  showModal.value = true;
}

async function handleSubmit() {
  if (editingItem.value) {
    await financeStore.updateIncomeExpenseItem(
      reportId.value,
      editingItem.value.id,
      form.value,
    );
  } else {
    await financeStore.addIncomeExpenseItem(reportId.value, form.value);
  }
  showModal.value = false;
}

async function handleDelete(itemId) {
  if (confirm("确定删除此条目？")) {
    await financeStore.deleteIncomeExpenseItem(reportId.value, itemId);
  }
}

onMounted(() => {
  financeStore.fetchIncomeExpense(reportId.value);
});
</script>

<style scoped>
.empty-row {
  text-align: center;
  padding: 1rem;
  color: var(--color-text-muted);
  font-size: 0.85rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.88rem;
  color: var(--color-text);
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  accent-color: var(--color-primary);
}
</style>
