<template>
  <div class="cashflow">
    <div class="page-header">
      <h1>月度现金流表</h1>
      <button @click="refresh" class="create-btn shimmer-btn" style="background: white; color: #D4AF37; border: 1px solid #D4AF37;">
        <RefreshCw :size="16" />
        刷新计算
      </button>
    </div>

    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <template v-else-if="cashFlow">
      <!-- 核心指标 -->
      <div class="summary-grid">
        <div class="summary-card glass-card spotlight-card" style="--accent: #52c41a">
          <div class="label">月总收入</div>
          <div class="value" style="color: #52c41a">
            ¥{{ formatNum(cashFlow.totalIncome) }}
          </div>
          <div class="card-icon-bg"><TrendingUp :size="48" /></div>
        </div>
        <div class="summary-card glass-card spotlight-card" style="--accent: #ff4d4f">
          <div class="label">月总支出</div>
          <div class="value" style="color: #ff4d4f">
            ¥{{ formatNum(cashFlow.totalExpense) }}
          </div>
          <div class="card-icon-bg"><TrendingDown :size="48" /></div>
        </div>
        <div class="summary-card glass-card spotlight-card" style="--accent: #D4AF37">
          <div class="label">月现金流</div>
          <div
            class="value"
            :style="{ color: cashFlow.monthlyCashFlow >= 0 ? '#52c41a' : '#ff4d4f' }"
          >
            ¥{{ formatNum(cashFlow.monthlyCashFlow) }}
          </div>
          <div class="card-icon-bg"><Activity :size="48" /></div>
        </div>
      </div>

      <!-- 现金流状态指示 -->
      <el-alert
        :type="cashFlow.monthlyCashFlow >= 0 ? 'success' : 'error'"
        :closable="false"
        show-icon
        class="cashflow-status"
      >
        <template #title>
          <span class="status-title">
            {{ cashFlow.monthlyCashFlow >= 0 ? "现金流健康" : "现金流为负" }}
          </span>
        </template>
        <template #default>
          <span class="status-desc">
            {{
              cashFlow.monthlyCashFlow >= 0
                ? "您每月有正向现金流，可用于储蓄或投资。"
                : "您每月支出超过收入，建议检查并优化支出结构。"
            }}
          </span>
        </template>
      </el-alert>

      <!-- 收入明细 -->
      <div class="section-card glass-card spotlight-card" style="margin-top: 24px">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap income"><TrendingUp :size="18" /></div>
            <span>现金流入（明细）</span>
          </div>
        </div>

        <div class="section-divider">劳动收入（主动收入）</div>
        <el-table
          v-if="cashFlow.laborIncomeItems && cashFlow.laborIncomeItems.length > 0"
          :data="cashFlow.laborIncomeItems"
          stripe
          show-summary
          :summary-method="() => ['小计', `¥${formatNum(cashFlow.laborIncome)}`]"
        >
          <el-table-column prop="name" label="来源" />
          <el-table-column label="金额 (¥/月)" align="right">
            <template #default="{ row }">
              <span class="amount positive">{{ formatNum(row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无劳动收入" :image-size="60" />

        <div class="section-divider">资产收入（被动收入）</div>
        <el-table
          v-if="cashFlow.assetIncomeItems && cashFlow.assetIncomeItems.length > 0"
          :data="cashFlow.assetIncomeItems"
          stripe
          show-summary
          :summary-method="() => ['小计', `¥${formatNum(cashFlow.assetIncome)}`]"
        >
          <el-table-column prop="name" label="来源" />
          <el-table-column label="金额 (¥/月)" align="right">
            <template #default="{ row }">
              <span class="amount positive">{{ formatNum(row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无资产收入" :image-size="60" />
      </div>

      <!-- 支出明细 -->
      <div class="section-card glass-card spotlight-card" style="margin-top: 24px">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap expense"><TrendingDown :size="18" /></div>
            <span>现金流出（明细）</span>
          </div>
        </div>

        <div class="section-divider">生活支出</div>
        <el-table
          v-if="cashFlow.livingExpenseItems && cashFlow.livingExpenseItems.length > 0"
          :data="cashFlow.livingExpenseItems"
          stripe
          show-summary
          :summary-method="() => ['小计', `¥${formatNum(cashFlow.livingExpense)}`]"
        >
          <el-table-column prop="name" label="项目" />
          <el-table-column label="金额 (¥/月)" align="right">
            <template #default="{ row }">
              <span class="amount negative">{{ formatNum(row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无生活支出" :image-size="60" />

        <div class="section-divider">借款利息支出</div>
        <el-table
          v-if="cashFlow.interestExpenseItems && cashFlow.interestExpenseItems.length > 0"
          :data="cashFlow.interestExpenseItems"
          stripe
          show-summary
          :summary-method="() => ['小计', `¥${formatNum(cashFlow.interestExpense)}`]"
        >
          <el-table-column prop="name" label="项目" />
          <el-table-column label="金额 (¥/月)" align="right">
            <template #default="{ row }">
              <span class="amount negative">{{ formatNum(row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无利息支出" :image-size="60" />

        <div class="section-divider">资产性支出</div>
        <el-table
          v-if="cashFlow.assetExpenseItems && cashFlow.assetExpenseItems.length > 0"
          :data="cashFlow.assetExpenseItems"
          stripe
          show-summary
          :summary-method="() => ['小计', `¥${formatNum(cashFlow.assetExpense)}`]"
        >
          <el-table-column prop="name" label="项目" />
          <el-table-column label="金额 (¥/月)" align="right">
            <template #default="{ row }">
              <span class="amount negative">{{ formatNum(row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无资产性支出" :image-size="60" />
      </div>

      <!-- 不计入现金流的项目 -->
      <div
        v-if="cashFlow.excludedItems && cashFlow.excludedItems.length > 0"
        class="section-card glass-card spotlight-card" 
        style="margin-top: 24px"
      >
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap info"><Info :size="18" /></div>
            <span>资产转移项</span>
          </div>
        </div>
        <el-table :data="cashFlow.excludedItems" stripe>
          <el-table-column prop="name" label="项目" />
          <el-table-column label="金额 (¥/月)" align="right">
            <template #default="{ row }">
              <span style="color: #909399">{{ formatNum(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="原因">
            <template #default>
              <span style="color: #909399">资产转移，非现金流出</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 计算公式说明 -->
      <div class="section-card glass-card formula-card spotlight-card" style="margin-top: 24px">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap math"><Calculator :size="18" /></div>
            <span>计算分析</span>
          </div>
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
            <span>
              劳动收入 + 资产收入 = ¥{{ formatNum(cashFlow.laborIncome) }} +
              ¥{{ formatNum(cashFlow.assetIncome) }} =
              <strong>¥{{ formatNum(cashFlow.totalIncome) }}</strong>
            </span>
          </div>
          <div class="formula-line sub">
            <span class="formula-label">月总支出</span>
            <span class="formula-eq">=</span>
            <span>
              生活支出 + 借款利息 + 资产支出 = ¥{{ formatNum(cashFlow.livingExpense) }} +
              ¥{{ formatNum(cashFlow.interestExpense) }} + ¥{{ formatNum(cashFlow.assetExpense) }} =
              <strong>¥{{ formatNum(cashFlow.totalExpense) }}</strong>
            </span>
          </div>
          <div class="formula-line result">
            <span class="formula-label">月现金流</span>
            <span class="formula-eq">=</span>
            <span>
              ¥{{ formatNum(cashFlow.totalIncome) }} - ¥{{ formatNum(cashFlow.totalExpense) }}
              =
              <strong :style="{ color: cashFlow.monthlyCashFlow >= 0 ? '#52c41a' : '#ff4d4f' }">
                ¥{{ formatNum(cashFlow.monthlyCashFlow) }}
              </strong>
            </span>
          </div>
        </div>
        <el-alert type="warning" :closable="false" show-icon style="margin-top: 16px">
          <template #default>
            注意：资产性支出中的本金部分（如房贷本金、股票购买、定期存款等）不计入月支出，因为这属于资产转移而非实际现金流出。
          </template>
        </el-alert>
      </div>
    </template>

    <el-empty
      v-else
      description="暂无现金流数据"
      :image-size="120"
    >
      <template #description>
        <p style="color: #909399">暂无现金流数据</p>
        <p style="color: #c0c4cc; font-size: 0.88rem;">请先在收入支出表中添加数据，系统将自动计算现金流</p>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useFinanceStore } from "../stores/finance";
import { RefreshCw, TrendingUp, TrendingDown, Activity, Info, Calculator } from 'lucide-vue-next';

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

.icon-wrap.info {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.icon-wrap.math {
  background: rgba(212, 175, 55, 0.1);
  color: #D4AF37;
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

.cashflow-status {
  margin-top: 24px;
  border-radius: 12px;
}

.status-title {
  font-weight: 700;
  font-size: 1rem;
}

.status-desc {
  font-size: 0.88rem;
  color: #666;
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

.formula {
  padding: 24px;
}

.formula-line {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 8px 0;
  font-size: 0.95rem;
}

.formula-line.sub {
  padding-left: 24px;
  font-size: 0.9rem;
  color: #666;
}

.formula-line.result {
  padding-top: 16px;
  border-top: 1px solid rgba(212, 175, 55, 0.12);
  margin-top: 12px;
  font-size: 1.1rem;
}

.formula-label {
  font-weight: 700;
  min-width: 90px;
  color: #333;
}

.formula-eq {
  color: #909399;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 0.9rem;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s;
}

:deep(.el-table) {
  padding: 0 16px;
}
</style>
