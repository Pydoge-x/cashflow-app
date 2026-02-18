<template>
  <div class="balance-sheet">
    <div class="page-header">
      <h1>📋 资产负债表</h1>
      <button class="btn btn-primary" @click="openAddModal">＋ 添加条目</button>
    </div>

    <!-- 汇总区域 -->
    <div class="summary-grid">
      <div class="summary-card" style="--accent: #22c55e">
        <div class="label">总资产</div>
        <div class="value" style="color: var(--color-success)">
          ¥{{ formatNum(totalAssets) }}
        </div>
      </div>
      <div class="summary-card" style="--accent: #ef4444">
        <div class="label">总负债</div>
        <div class="value" style="color: var(--color-danger)">
          ¥{{ formatNum(totalDebts) }}
        </div>
      </div>
      <div class="summary-card" style="--accent: #6366f1">
        <div class="label">净资产</div>
        <div
          class="value"
          :style="{
            color:
              netWorth >= 0 ? 'var(--color-success)' : 'var(--color-danger)',
          }"
        >
          ¥{{ formatNum(netWorth) }}
        </div>
      </div>
    </div>

    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <template v-else>
      <!-- 资产部分 -->
      <div class="card" style="margin-bottom: 1.5rem">
        <div class="card-header">
          <h3>💰 资产</h3>
          <span class="badge badge-asset">¥{{ formatNum(totalAssets) }}</span>
        </div>

        <template v-for="(cat, catKey) in assetCategories" :key="catKey">
          <div class="section-divider">{{ cat.label }}</div>
          <table class="data-table" v-if="getItems(catKey).length > 0">
            <thead>
              <tr>
                <th>名称</th>
                <th>金额 (¥)</th>
                <th>备注</th>
                <th style="text-align: right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in getItems(catKey)" :key="item.id">
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

      <!-- 负债部分 -->
      <div class="card">
        <div class="card-header">
          <h3>💳 负债</h3>
          <span class="badge badge-debt">¥{{ formatNum(totalDebts) }}</span>
        </div>

        <template v-for="(cat, catKey) in debtCategories" :key="catKey">
          <div class="section-divider">{{ cat.label }}</div>
          <table class="data-table" v-if="getItems(catKey).length > 0">
            <thead>
              <tr>
                <th>名称</th>
                <th>金额 (¥)</th>
                <th>利息 (¥/月)</th>
                <th>备注</th>
                <th style="text-align: right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in getItems(catKey)" :key="item.id">
                <td>{{ item.name }}</td>
                <td>
                  <span class="amount negative">{{
                    formatNum(item.amount)
                  }}</span>
                </td>
                <td>
                  <span v-if="item.isInterest" class="amount" style="color: var(--color-warning)">
                    {{ formatNum(item.interestAmount) }}
                  </span>
                  <span v-else style="color: var(--color-text-muted)">-</span>
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
          <div class="form-group">
            <label>分类</label>
            <select v-model="form.category" required>
              <optgroup label="资产">
                <option value="CURRENT_ASSET">流动资产</option>
                <option value="INVESTMENT_ASSET">投资性资产</option>
                <option value="PERSONAL_ASSET">自用资产</option>
              </optgroup>
              <optgroup label="负债">
                <option value="CONSUMER_DEBT">消费负债</option>
                <option value="INVESTMENT_DEBT">投资负债</option>
                <option value="PERSONAL_DEBT">自用资产负债</option>
              </optgroup>
            </select>
          </div>
          <div class="form-group">
            <label>名称</label>
            <input
              v-model="form.name"
              type="text"
              placeholder="如：活期存款"
              required
            />
          </div>
          <div class="form-group">
            <label>金额 (¥)</label>
            <input
              v-model.number="form.amount"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
              required
            />
          </div>
            <div class="form-group">
              <label>备注</label>
              <textarea v-model="form.note" placeholder="补充说明..."></textarea>
            </div>

            <template v-if="isDebtCategory">
              <div class="form-group checkbox-group">
                <input type="checkbox" id="isInterest" v-model="form.isInterest" />
                <label for="isInterest">月利息支出（非本金部分）</label>
              </div>
              <div class="form-group" v-if="form.isInterest">
                <label>月利息金额 (¥/月)</label>
                <input
                  type="number"
                  v-model.number="form.interestAmount"
                  step="0.01"
                  placeholder="请输入月利息金额"
                />
              </div>
            </template>
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
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useFinanceStore } from "../stores/finance";

const route = useRoute();
const financeStore = useFinanceStore();
const reportId = computed(() => route.params.reportId);

const showModal = ref(false);
const editingItem = ref(null);
const form = ref({
  category: "CURRENT_ASSET",
  name: "",
  amount: "",
  note: "",
  isInterest: false,
  interestAmount: 0,
});

const isDebtCategory = computed(() => {
  return ["CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"].includes(form.value.category);
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

const categories = {
  CURRENT_ASSET: "流动资产",
  INVESTMENT_ASSET: "投资性资产",
  PERSONAL_ASSET: "自用资产",
  CONSUMER_DEBT: "消费负债",
  INVESTMENT_DEBT: "投资负债",
  PERSONAL_DEBT: "自用资产负债",
};

function getItems(category) {
  return financeStore.balanceSheet.filter((i) => i.category === category);
}

const totalAssets = computed(() => {
  return financeStore.balanceSheet
    .filter((i) =>
      ["CURRENT_ASSET", "INVESTMENT_ASSET", "PERSONAL_ASSET"].includes(
        i.category,
      ),
    )
    .reduce((s, i) => s + (i.amount || 0), 0);
});

const totalDebts = computed(() => {
  return financeStore.balanceSheet
    .filter((i) =>
      ["CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"].includes(
        i.category,
      ),
    )
    .reduce((s, i) => s + (i.amount || 0), 0);
});

const netWorth = computed(() => totalAssets.value - totalDebts.value);

function formatNum(n) {
  return (n || 0).toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function openAddModal(category) {
  editingItem.value = null;
  form.value = {
    category,
    name: "",
    amount: "",
    note: "",
    isInterest: false,
    interestAmount: 0,
  };
  showModal.value = true;
}

function openEditModal(item) {
  editingItem.value = item;
  form.value = { ...item };
  showModal.value = true;
}

async function handleSubmit() {
  let savedItem;
  if (editingItem.value) {
    savedItem = await financeStore.updateBalanceSheetItem(
      reportId.value,
      editingItem.value.id,
      form.value,
    );
  } else {
    savedItem = await financeStore.addBalanceSheetItem(reportId.value, form.value);
  }

  // 金额互通逻辑：如果收入支出表中有同名项，则同步更新（仅针对负债项）
  const targetName = editingItem.value ? editingItem.value.name : form.value.name;
  const ieItem = financeStore.incomeExpense.find(i => i.name === targetName);
  if (ieItem && isDebtCategory.value) {
    await financeStore.updateIncomeExpenseItem(reportId.value, ieItem.id, {
      ...ieItem,
      name: form.value.name,
      amount: form.value.amount, // Now principal is separate from interest
      isInterest: form.value.isInterest,
      interestAmount: form.value.interestAmount,
      note: form.value.note
    });
  }

  showModal.value = false;
}

async function handleDelete(itemId) {
  if (confirm("确定删除此条目？")) {
    await financeStore.deleteBalanceSheetItem(reportId.value, itemId);
  }
}

onMounted(async () => {
  await Promise.all([
    financeStore.fetchBalanceSheet(reportId.value),
    financeStore.fetchIncomeExpense(reportId.value)
  ]);
});
</script>

<style scoped>
.empty-row {
  text-align: center;
  padding: 1rem;
  color: var(--color-text-muted);
  font-size: 0.85rem;
}
</style>
