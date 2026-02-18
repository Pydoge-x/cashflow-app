<template>
  <div class="cashflow">
    <div class="page-header">
      <h1>📈 月度现金流表</h1>
      <button class="btn btn-secondary" @click="refresh">🔄 刷新计算</button>
    </div>

    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <template v-else-if="cashFlow">
      <!-- 核心指标 -->
      <div class="summary-grid">
        <div class="summary-card" style="--accent: #22c55e">
          <div class="label">月总收入</div>
          <div class="value" style="color: var(--color-success)">
            ¥{{ formatNum(cashFlow.totalIncome) }}
          </div>
        </div>
        <div class="summary-card" style="--accent: #ef4444">
          <div class="label">月总支出</div>
          <div class="value" style="color: var(--color-danger)">
            ¥{{ formatNum(cashFlow.totalExpense) }}
          </div>
        </div>
        <div class="summary-card" style="--accent: #6366f1">
          <div class="label">月现金流</div>
          <div
            class="value"
            :style="{
              color:
                cashFlow.monthlyCashFlow >= 0
                  ? 'var(--color-success)'
                  : 'var(--color-danger)',
            }"
          >
            ¥{{ formatNum(cashFlow.monthlyCashFlow) }}
          </div>
        </div>
      </div>

      <!-- 现金流状态指示 -->
      <div
        class="cashflow-status card"
        :class="
          cashFlow.monthlyCashFlow >= 0 ? 'status-positive' : 'status-negative'
        "
      >
        <div class="status-icon">
          {{ cashFlow.monthlyCashFlow >= 0 ? "✅" : "⚠️" }}
        </div>
        <div>
          <div class="status-title">
            {{ cashFlow.monthlyCashFlow >= 0 ? "现金流健康" : "现金流为负" }}
          </div>
          <div class="status-desc">
            {{
              cashFlow.monthlyCashFlow >= 0
                ? "您每月有正向现金流，可用于储蓄或投资。"
                : "您每月支出超过收入，建议检查并优化支出结构。"
            }}
          </div>
        </div>
      </div>

      <!-- 收入明细 -->
      <div class="card" style="margin-bottom: 1.5rem">
        <div class="card-header">
          <h3>📥 现金流入（收入）</h3>
        </div>

        <div class="section-divider">劳动收入（主动收入）</div>
        <table
          class="data-table"
          v-if="
            cashFlow.laborIncomeItems && cashFlow.laborIncomeItems.length > 0
          "
        >
          <thead>
            <tr>
              <th>来源</th>
              <th style="text-align: right">金额 (¥/月)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cashFlow.laborIncomeItems" :key="item.name">
              <td>{{ item.name }}</td>
              <td style="text-align: right">
                <span class="amount positive">{{
                  formatNum(item.amount)
                }}</span>
              </td>
            </tr>
            <tr class="subtotal-row">
              <td>小计</td>
              <td style="text-align: right">
                <span class="amount positive">{{
                  formatNum(cashFlow.laborIncome)
                }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-row">暂无劳动收入</div>

        <div class="section-divider">资产收入（被动收入）</div>
        <table
          class="data-table"
          v-if="
            cashFlow.assetIncomeItems && cashFlow.assetIncomeItems.length > 0
          "
        >
          <thead>
            <tr>
              <th>来源</th>
              <th style="text-align: right">金额 (¥/月)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cashFlow.assetIncomeItems" :key="item.name">
              <td>{{ item.name }}</td>
              <td style="text-align: right">
                <span class="amount positive">{{
                  formatNum(item.amount)
                }}</span>
              </td>
            </tr>
            <tr class="subtotal-row">
              <td>小计</td>
              <td style="text-align: right">
                <span class="amount positive">{{
                  formatNum(cashFlow.assetIncome)
                }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-row">暂无资产收入</div>
      </div>

      <!-- 支出明细 -->
      <div class="card" style="margin-bottom: 1.5rem">
        <div class="card-header">
          <h3>📤 现金流出（支出）</h3>
        </div>

        <div class="section-divider">生活支出</div>
        <table
          class="data-table"
          v-if="
            cashFlow.livingExpenseItems &&
            cashFlow.livingExpenseItems.length > 0
          "
        >
          <thead>
            <tr>
              <th>项目</th>
              <th style="text-align: right">金额 (¥/月)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cashFlow.livingExpenseItems" :key="item.name">
              <td>{{ item.name }}</td>
              <td style="text-align: right">
                <span class="amount negative">{{
                  formatNum(item.amount)
                }}</span>
              </td>
            </tr>
            <tr class="subtotal-row">
              <td>小计</td>
              <td style="text-align: right">
                <span class="amount negative">{{
                  formatNum(cashFlow.livingExpense)
                }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-row">暂无生活支出</div>

        <div class="section-divider">借款利息支出</div>
        <table
          class="data-table"
          v-if="
            cashFlow.interestExpenseItems &&
            cashFlow.interestExpenseItems.length > 0
          "
        >
          <thead>
            <tr>
              <th>项目</th>
              <th style="text-align: right">金额 (¥/月)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cashFlow.interestExpenseItems" :key="item.name">
              <td>{{ item.name }}</td>
              <td style="text-align: right">
                <span class="amount negative">{{
                  formatNum(item.amount)
                }}</span>
              </td>
            </tr>
            <tr class="subtotal-row">
              <td>小计</td>
              <td style="text-align: right">
                <span class="amount negative">{{
                  formatNum(cashFlow.interestExpense)
                }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-row">暂无利息支出</div>

        <div class="section-divider">资产性支出</div>
        <table
          class="data-table"
          v-if="
            cashFlow.assetExpenseItems &&
            cashFlow.assetExpenseItems.length > 0
          "
        >
          <thead>
            <tr>
              <th>项目</th>
              <th style="text-align: right">金额 (¥/月)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cashFlow.assetExpenseItems" :key="item.name">
              <td>{{ item.name }}</td>
              <td style="text-align: right">
                <span class="amount negative">{{
                  formatNum(item.amount)
                }}</span>
              </td>
            </tr>
            <tr class="subtotal-row">
              <td>小计</td>
              <td style="text-align: right">
                <span class="amount negative">{{
                  formatNum(cashFlow.assetExpense)
                }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-row">暂无资产性支出</div>
      </div>

      <!-- 不计入现金流的项目 -->
      <div
        class="card"
        v-if="cashFlow.excludedItems && cashFlow.excludedItems.length > 0"
      >
        <div class="card-header">
          <h3>📌 不计入现金流的项目（资产转移）</h3>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>项目</th>
              <th style="text-align: right">金额 (¥/月)</th>
              <th>原因</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cashFlow.excludedItems" :key="item.name">
              <td>{{ item.name }}</td>
              <td style="text-align: right">
                <span class="amount" style="color: var(--color-text-muted)">{{
                  formatNum(item.amount)
                }}</span>
              </td>
              <td style="color: var(--color-text-muted); font-size: 0.82rem">
                资产转移，非现金流出
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 计算公式说明 -->
      <div class="formula-card card" style="margin-top: 1.5rem">
        <div class="card-header">
          <h3>📐 计算公式</h3>
        </div>
        <div class="formula">
          <div class="formula-line">
            <span class="formula-label">月现金流</span>
            <span class="formula-eq">=</span>
            <span>月总收入 - 月总支出</span>
          </div>
          <div class="formula-line sub">
            <span class="formula-label">月总收入</span>
            <span class="formula-eq">=</span>
            <span
              >劳动收入 + 资产收入 = ¥{{ formatNum(cashFlow.laborIncome) }} +
              ¥{{ formatNum(cashFlow.assetIncome) }} =
              <strong>¥{{ formatNum(cashFlow.totalIncome) }}</strong></span
            >
          </div>
          <div class="formula-line sub">
            <span class="formula-label">月总支出</span>
            <span class="formula-eq">=</span>
            <span
              >生活支出 + 借款利息 + 资产支出 = ¥{{ formatNum(cashFlow.livingExpense) }} +
              ¥{{ formatNum(cashFlow.interestExpense) }} + ¥{{ formatNum(cashFlow.assetExpense) }} =
              <strong>¥{{ formatNum(cashFlow.totalExpense) }}</strong></span
            >
          </div>
          <div class="formula-line result">
            <span class="formula-label">月现金流</span>
            <span class="formula-eq">=</span>
            <span
              >¥{{ formatNum(cashFlow.totalIncome) }} - ¥{{
                formatNum(cashFlow.totalExpense)
              }}
              =
              <strong
                :style="{
                  color:
                    cashFlow.monthlyCashFlow >= 0
                      ? 'var(--color-success)'
                      : 'var(--color-danger)',
                }"
                >¥{{ formatNum(cashFlow.monthlyCashFlow) }}</strong
              ></span
            >
          </div>
        </div>
        <div class="formula-note">
          ⚠️
          注意：资产性支出中的本金部分（如房贷本金、股票购买、定期存款等）不计入月支出，因为这属于资产转移而非实际现金流出。
        </div>
      </div>
    </template>

    <div v-else class="empty-state">
      <div class="icon">📈</div>
      <p>暂无现金流数据</p>
      <p>请先在收入支出表中添加数据，系统将自动计算现金流</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useFinanceStore } from "../stores/finance";

const route = useRoute();
const financeStore = useFinanceStore();
const reportId = computed(() => route.params.reportId);
const cashFlow = computed(() => financeStore.cashFlow);

function formatNum(n) {
  return (n || 0).toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function refresh() {
  financeStore.fetchCashFlow(reportId.value);
}

onMounted(() => refresh());
</script>

<style scoped>
.cashflow-status {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1.2rem 1.5rem;
}

.cashflow-status.status-positive {
  border-color: rgba(34, 197, 94, 0.2);
  background: rgba(34, 197, 94, 0.06);
}

.cashflow-status.status-negative {
  border-color: rgba(239, 68, 68, 0.2);
  background: rgba(239, 68, 68, 0.06);
}

.status-icon {
  font-size: 2rem;
}

.status-title {
  font-weight: 700;
  font-size: 1rem;
  margin-bottom: 0.2rem;
}

.status-desc {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.subtotal-row td {
  font-weight: 700;
  border-top: 1px solid var(--color-border);
  background: rgba(99, 102, 241, 0.04);
}

.empty-row {
  text-align: center;
  padding: 1rem;
  color: var(--color-text-muted);
  font-size: 0.85rem;
}

.formula {
  padding: 0.5rem 0;
}

.formula-line {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  padding: 0.4rem 0;
  font-size: 0.9rem;
}

.formula-line.sub {
  padding-left: 1.5rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.formula-line.result {
  padding-top: 0.8rem;
  border-top: 1px solid var(--color-border);
  margin-top: 0.5rem;
  font-size: 1rem;
}

.formula-label {
  font-weight: 600;
  min-width: 80px;
}

.formula-eq {
  color: var(--color-text-muted);
}

.formula-note {
  margin-top: 1rem;
  padding: 0.8rem 1rem;
  background: var(--color-warning-bg);
  border-radius: var(--radius-md);
  font-size: 0.82rem;
  color: var(--color-warning);
  line-height: 1.5;
}
</style>
