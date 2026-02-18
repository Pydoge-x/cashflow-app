<template>
  <div class="financial-charts">
    <div class="page-header">
      <h1>📊 财务分析图表</h1>
      <div class="header-actions">
        <span class="report-name" v-if="financeStore.currentReport">{{ financeStore.currentReport.name }}</span>
      </div>
    </div>

    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <div v-else class="charts-grid">
      <!-- 收入构成 -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>📥 收入构成分析</h3>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="incomeOption" autoresize />
        </div>
      </div>

      <!-- 支出构成 -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>📤 支出构成分析</h3>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="expenseOption" autoresize />
        </div>
      </div>

      <!-- 资产构成 -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>💰 资产构成分析</h3>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="assetOption" autoresize />
        </div>
      </div>

      <!-- 负债构成 -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>💳 负债构成分析</h3>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="debtOption" autoresize />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, provide } from "vue";
import { useRoute } from "vue-router";
import { useFinanceStore } from "../stores/finance";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { PieChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
} from "echarts/components";
import VChart, { THEME_KEY } from "vue-echarts";

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
]);

// 设置深色主题（与应用风格匹配）
provide(THEME_KEY, "dark");

const route = useRoute();
const financeStore = useFinanceStore();
const reportId = computed(() => route.params.reportId);

const colors = ["#6366f1", "#22c55e", "#ef4444", "#f59e0b", "#ec4899", "#06b6d4"];

// 同步 logic 辅助函数
function getMergedItems(type, category) {
  const originalItems = financeStore.incomeExpense.filter(
    (i) => i.type === type && i.category === category
  );


  if (type === "EXPENSE" && category === "ASSET_EXPENSE") {
    const debts = financeStore.balanceSheet.filter((i) =>
      ["CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"].includes(i.category)
    );
    const syncedItems = debts.map((debt) => {
      const existing = originalItems.find((oi) => oi.name === debt.name);
      // Return interestAmount if it's a sync item
      return existing || { 
        name: debt.name, 
        amount: debt.isInterest ? (debt.interestAmount || 0) : 0 
      };
    });
    const debtNames = new Set(debts.map(d => d.name));
    return [...syncedItems, ...originalItems.filter(oi => !debtNames.has(oi.name))];
  }

  return originalItems;
}

// 通用饼图配置模板
const getPieOption = (title, data) => ({
  backgroundColor: "transparent",
  tooltip: {
    trigger: "item",
    formatter: "{b}: ¥{c} ({d}%)",
  },
  legend: {
    orient: "vertical",
    left: "left",
    textStyle: { color: "#9ca3af" },
  },
  series: [
    {
      name: title,
      type: "pie",
      radius: ["40%", "70%"],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: "#111827",
        borderWidth: 2,
      },
      label: {
        show: false,
        position: "center",
      },
      emphasis: {
        label: {
          show: true,
          fontSize: "16",
          fontWeight: "bold",
        },
      },
      labelLine: {
        show: false,
      },
      data: data,
      color: colors,
    },
  ],
});

// 收入数据
const incomeOption = computed(() => {
  const laborTotal = financeStore.incomeExpense
    .filter((i) => i.type === "INCOME" && i.category === "LABOR_INCOME")
    .reduce((s, i) => s + i.amount, 0);
  
  const assetTotal = getMergedItems("INCOME", "ASSET_INCOME")
    .reduce((s, i) => s + i.amount, 0);

  const data = [
    { name: "劳动收入", value: laborTotal },
    { name: "资产收入", value: assetTotal },
  ].filter((i) => i.value > 0);
  return getPieOption("收入构成", data);
});

// 支出数据
const expenseOption = computed(() => {
  const livingTotal = financeStore.incomeExpense
    .filter((i) => i.type === "EXPENSE" && i.category === "LIVING_EXPENSE")
    .reduce((s, i) => s + i.amount, 0);

  const assetExpenseTotal = getMergedItems("EXPENSE", "ASSET_EXPENSE")
    .reduce((s, i) => s + i.amount, 0);

  const loanTotal = financeStore.incomeExpense
    .filter((i) => i.type === "EXPENSE" && i.category === "LOAN_REPAYMENT")
    .reduce((s, i) => s + i.amount, 0);

  // 借款利息支出 (显式记录)
  const explicitInterestTotal = financeStore.incomeExpense
    .filter((i) => i.type === "EXPENSE" && i.isInterest)
    .reduce((s, i) => s + i.amount, 0);

  // 资产负债表负债中的利息
  const debtInterestTotal = financeStore.balanceSheet
    .filter((i) => ["CONSUMER_DEBT", "INVESTMENT_DEBT", "PERSONAL_DEBT"].includes(i.category) && i.isInterest)
    .reduce((s, i) => s + (i.interestAmount || 0), 0);

  const interestTotal = explicitInterestTotal + debtInterestTotal;

  const data = [
    { name: "生活支出", value: livingTotal },
    { name: "利息支出", value: interestTotal },
    { name: "资产支出", value: assetExpenseTotal },
    { name: "借款还款", value: loanTotal },
  ].filter((i) => i.value > 0);
  return getPieOption("支出构成", data);
});

// 资产数据
const assetOption = computed(() => {
  const data = [
    {
      name: "流动资产",
      value: financeStore.balanceSheet
        .filter((i) => i.category === "CURRENT_ASSET")
        .reduce((s, i) => s + i.amount, 0),
    },
    {
      name: "投资性资产",
      value: financeStore.balanceSheet
        .filter((i) => i.category === "INVESTMENT_ASSET")
        .reduce((s, i) => s + i.amount, 0),
    },
    {
      name: "自用资产",
      value: financeStore.balanceSheet
        .filter((i) => i.category === "PERSONAL_ASSET")
        .reduce((s, i) => s + i.amount, 0),
    },
  ].filter((i) => i.value > 0);
  return getPieOption("资产构成", data);
});

// 负债数据
const debtOption = computed(() => {
  const data = [
    {
      name: "消费负债",
      value: financeStore.balanceSheet
        .filter((i) => i.category === "CONSUMER_DEBT")
        .reduce((s, i) => s + i.amount, 0),
    },
    {
      name: "投资负债",
      value: financeStore.balanceSheet
        .filter((i) => i.category === "INVESTMENT_DEBT")
        .reduce((s, i) => s + i.amount, 0),
    },
    {
      name: "自用资产负债",
      value: financeStore.balanceSheet
        .filter((i) => i.category === "PERSONAL_DEBT")
        .reduce((s, i) => s + i.amount, 0),
    },
  ].filter((i) => i.value > 0);
  return getPieOption("负债构成", data);
});

onMounted(() => {
  financeStore.fetchIncomeExpense(reportId.value);
  financeStore.fetchBalanceSheet(reportId.value);
});
</script>

<style scoped>
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 1.5rem;
  margin-top: 1rem;
}

.chart-card {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  width: 100%;
  min-height: 0;
}

.chart {
  width: 100%;
  height: 100%;
}

.report-name {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  background: var(--color-bg-secondary);
  padding: 0.4rem 0.8rem;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border);
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .chart-card {
    height: 350px;
  }
}
</style>
