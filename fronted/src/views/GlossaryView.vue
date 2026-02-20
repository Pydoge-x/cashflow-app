<template>
  <div class="glossary-view">
    <div class="page-header">
      <div class="header-content">
        <h1>📖 金融名词解析</h1>
        <p class="subtitle">理解财务术语，掌握您的财富逻辑</p>
      </div>
      <el-input
        v-model="searchQuery"
        placeholder="搜索关键词 (如：资产、利息)..."
        prefix-icon="Search"
        clearable
        style="width: 320px"
        size="large"
      />
    </div>

    <div class="category-tabs">
      <el-button
        v-for="cat in categories"
        :key="cat.id"
        :type="activeCategory === cat.id ? 'primary' : 'default'"
        round
        @click="activeCategory = cat.id"
      >
        {{ cat.icon }} {{ cat.label }}
      </el-button>
    </div>

    <div class="terms-grid">
      <el-card
        v-for="term in filteredTerms"
        :key="term.title"
        class="term-card"
        shadow="hover"
      >
        <div class="term-header">
          <span class="term-title">{{ term.title }}</span>
          <el-tag :type="getCategoryType(term.categoryId)" effect="plain" size="small">
            {{ getCategoryLabel(term.categoryId) }}
          </el-tag>
        </div>
        <div class="term-content">
          <p class="description">{{ term.description }}</p>
          <div v-if="term.examples" class="examples">
            <span class="example-label">示例：</span>
            <p>{{ term.examples }}</p>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty
      v-if="filteredTerms.length === 0"
      description="没有找到相关词条"
      :image-size="120"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const searchQuery = ref('');
const activeCategory = ref('all');

const categories = [
  { id: 'all', label: '全部', icon: '✨' },
  { id: 'asset', label: '资产', icon: '💰' },
  { id: 'liability', label: '负债', icon: '💳' },
  { id: 'income', label: '收入', icon: '📈' },
  { id: 'expense', label: '支出', icon: '📉' },
  { id: 'concept', label: '财务概念', icon: '🧠' }
];

const terms = [
  {
    categoryId: 'asset',
    title: '流动资产 (Current Asset)',
    description: '可以直接变现或在短期内（一年内）变现的资产。',
    examples: '现金、银行活期存款、余额宝等。'
  },
  {
    categoryId: 'asset',
    title: '投资性资产 (Investment Asset)',
    description: '为了获得未来收益或价值增长而持有的资产。',
    examples: '股票、基金、理财产品、用于出租的房地产。'
  },
  {
    categoryId: 'asset',
    title: '自用资产 (Personal Asset)',
    description: '供个人长期使用或消费，不以产生现金流为首要目的的资产。',
    examples: '自住住房、家庭轿车、高档家具、电子设备。'
  },
  {
    categoryId: 'liability',
    title: '消费负债 (Consumer Debt)',
    description: '为了满足当前消费需求而产生的债务，通常伴随较高的利息支出。',
    examples: '信用卡账单、消费贷款、网购分期。'
  },
  {
    categoryId: 'liability',
    title: '投资负债 (Investment Debt)',
    description: '为了获取更高回报的投资行为而筹集的资金。',
    examples: '房贷（用于投资房产）、用于购买股票的融资。'
  },
  {
    categoryId: 'liability',
    title: '自用资产负债 (Personal Debt)',
    description: '通常指来自非金融机构的债务，如亲友之间的借款。',
    examples: '向朋友借的钱、亲戚资助的周转资金。'
  },
  {
    categoryId: 'income',
    title: '劳动/主动收入 (Labor Income)',
    description: '通过付出体力和脑力劳动获得的报酬，也称"主动收入"。',
    examples: '月薪、奖金、加班费、兼职收入。'
  },
  {
    categoryId: 'income',
    title: '资产/被动收入 (Asset Income)',
    description: '通过持有资产而产生的被动收益。',
    examples: '股票派息、银行存款利息、房屋租金收益、基金分红。'
  },
  {
    categoryId: 'expense',
    title: '生活支出 (Living Expense)',
    description: '为了维持正常生活水平而产生的各项费用。',
    examples: '餐饮伙食、交通出行、租房费用、水电煤缴费。'
  },
  {
    categoryId: 'expense',
    title: '资产性支出 (Asset Expense)',
    description: '用于建立或增加资产头寸的支出，在本项目中包含购买资产的本金。',
    examples: '定投基金的本金、买入手表的支出。'
  },
  {
    categoryId: 'expense',
    title: '借款还款 (Loan Repayment)',
    description: '归还之前产生的负债本金，不包含利息。',
    examples: '房贷本金偿还、归还信用卡欠款中的消费部分。'
  },
  {
    categoryId: 'concept',
    title: '利息额 (Interest Amount)',
    description: '借用资金所付出的代价，是纯粹的支出，不增加资产也不减少负债原值。',
    examples: '信用卡利息、房贷利息、借款手续费。'
  },
  {
    categoryId: 'concept',
    title: '现金流 (Cash Flow)',
    description: '一定会计期间内企业或个人现金及现金等价物的流入和流出。',
    examples: '工资到账是流入，去超市买菜是流出。'
  },
  {
    categoryId: 'concept',
    title: '净资产 (Net Worth)',
    description: '您的总资产减去总负债后的余额，反映了您真实的财务身价。',
    examples: '房产价值100万，贷款40万，则该项净资产为60万。'
  }
];

const filteredTerms = computed(() => {
  return terms.filter(term => {
    const matchesQuery = term.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                        term.description.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesCategory = activeCategory.value === 'all' || term.categoryId === activeCategory.value;
    return matchesQuery && matchesCategory;
  });
});

function getCategoryLabel(id) {
  return categories.find(c => c.id === id)?.label || '';
}

function getCategoryType(id) {
  const typeMap = {
    asset: 'success',
    liability: 'danger',
    income: 'warning',
    expense: '',
    concept: 'info'
  };
  return typeMap[id] || '';
}
</script>

<style scoped>
.glossary-view {
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
}

.header-content h1 {
  font-size: 1.75rem;
  margin-bottom: 4px;
  font-weight: 800;
  background: linear-gradient(135deg, #333 0%, #D4AF37 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  color: #909399;
  font-size: 1rem;
}

.category-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.terms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.term-card {
  border-radius: 16px;
  transition: all 0.3s ease;
}

.term-card:hover {
  transform: translateY(-4px);
}

.term-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.term-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
  line-height: 1.4;
  flex: 1;
  margin-right: 12px;
}

.description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.examples {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px dashed #E8D5A3;
  font-size: 0.88rem;
}

.example-label {
  color: #909399;
  font-weight: 600;
}

.examples p {
  color: #909399;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .terms-grid {
    grid-template-columns: 1fr;
  }
}
</style>
