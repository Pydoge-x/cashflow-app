<template>
  <div class="financial-charts">
    <div class="page-header">
      <h1>财务分析图表</h1>
      <div v-if="financeStore.currentReport" class="shimmer-btn report-name-badge">
        <Sparkles :size="14" />
        <span>{{ financeStore.currentReport.name }}</span>
      </div>
    </div>

    <div v-if="financeStore.loading" class="loading-spinner"></div>

    <div v-else class="charts-grid">
      <!-- 收入构成 -->
      <div class="chart-card glass-card spotlight-card">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap income"><TrendingUp :size="18" /></div>
            <span>收入构成分析</span>
          </div>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="incomeOption" autoresize />
        </div>
      </div>

      <!-- 支出构成 -->
      <div class="chart-card glass-card spotlight-card">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap expense"><TrendingDown :size="18" /></div>
            <span>支出构成分析</span>
          </div>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="expenseOption" autoresize />
        </div>
      </div>

      <!-- 资产构成 -->
      <div class="chart-card glass-card spotlight-card">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap asset"><CircleDollarSign :size="18" /></div>
            <span>资产构成分析</span>
          </div>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="assetOption" autoresize />
        </div>
      </div>

      <!-- 负债构成 -->
      <div class="chart-card glass-card spotlight-card">
        <div class="card-header">
          <div class="header-title">
            <div class="icon-wrap debt"><CreditCard :size="18" /></div>
            <span>负债构成分析</span>
          </div>
        </div>
        <div class="chart-container">
          <v-chart class="chart" :option="debtOption" autoresize />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useFinanceStore } from "../stores/finance";
import { Sparkles, TrendingUp, TrendingDown, CircleDollarSign, CreditCard } from 'lucide-vue-next';
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { PieChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
} from "echarts/components";
import VChart from "vue-echarts";

use([
  CanvasRenderer,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
]);

const route = useRoute();
const financeStore = useFinanceStore();
const reportId = computed(() => route.params.reportId);
const colors = ["#D4AF37", "#52C41A", "#FF4D4F", "#FAAD14", "#722ED1", "#13C2C2"];

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

const getPieOption = (title, data) => ({
  backgroundColor: "transparent",
  tooltip: {
    trigger: "item",
    formatter: "{b}: ¥{c} ({d}%)",
    backgroundColor: "#fff",
    borderColor: "#E8D5A3",
    borderWidth: 1,
    textStyle: {
      color: "#333"
    }
  },
  legend: {
    orient: "vertical",
    left: "left",
    textStyle: { 
      color: "#666",
      fontSize: 12
    },
  },
  series: [
    {
      name: title,
      type: "pie",
      radius: ["40%", "70%"],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: "#fff",
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
          color: "#333"
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

const expenseOption = computed(() => {
  const livingTotal = financeStore.incomeExpense
    .filter((i) => i.type === "EXPENSE" && i.category === "LIVING_EXPENSE")
    .reduce((s, i) => s + i.amount, 0);

  const assetExpenseTotal = getMergedItems("EXPENSE", "ASSET_EXPENSE")
    .reduce((s, i) => s + i.amount, 0);

  const loanTotal = financeStore.incomeExpense
    .filter((i) => i.type === "EXPENSE" && i.category === "LOAN_REPAYMENT")
    .reduce((s, i) => s + i.amount, 0);

  const explicitInterestTotal = financeStore.incomeExpense
    .filter((i) => i.type === "EXPENSE" && i.isInterest)
    .reduce((s, i) => s + i.amount, 0);

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
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-top: 16px;
}

.chart-card {
  height: 480px;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  border-radius: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
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
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrap.income { background: rgba(82, 196, 26, 0.1); color: #52c41a; }
.icon-wrap.expense { background: rgba(255, 77, 79, 0.1); color: #ff4d4f; }
.icon-wrap.asset { background: rgba(82, 196, 26, 0.1); color: #52c41a; }
.icon-wrap.debt { background: rgba(255, 77, 79, 0.1); color: #ff4d4f; }

.report-name-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 0.85rem;
  border-radius: 999px;
  font-weight: 600;
}

.chart-container {
  flex: 1;
  width: 100%;
  padding: 20px;
  min-height: 0;
}

.chart {
  width: 100%;
  height: 100%;
}

@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .chart-card {
    height: 380px;
  }

  .card-header {
    padding: 16px;
  }

  .header-title {
    font-size: 1rem;
  }
}
</style>
