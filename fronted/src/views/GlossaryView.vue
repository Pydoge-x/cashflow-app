<template>
  <div class="glossary-view">
    <div class="page-header">
      <div class="header-content">
        <h1>📖 金融名词解析</h1>
        <p class="subtitle">理解财务术语，掌握您的财富逻辑</p>
      </div>
      <div class="search-box">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索关键词 (如：资产、利息)..."
        />
      </div>
    </div>

    <div class="category-tabs">
      <button 
        v-for="cat in categories" 
        :key="cat.id"
        :class="['tab-btn', { active: activeCategory === cat.id }]"
        @click="activeCategory = cat.id"
      >
        {{ cat.icon }} {{ cat.label }}
      </button>
    </div>

    <div class="terms-grid">
      <div 
        v-for="term in filteredTerms" 
        :key="term.title" 
        class="term-card"
      >
        <div class="term-header">
          <span class="term-title">{{ term.title }}</span>
          <span :class="['category-tag', term.categoryId]">
            {{ getCategoryLabel(term.categoryId) }}
          </span>
        </div>
        <div class="term-content">
          <p class="description">{{ term.description }}</p>
          <div v-if="term.examples" class="examples">
            <span class="example-label">示例：</span>
            <p>{{ term.examples }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredTerms.length === 0" class="empty-state">
      <div class="empty-icon">🔍</div>
      <p>没有找到相关词条</p>
    </div>
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
    description: '通过付出体力和脑力劳动获得的报酬，也称“主动收入”。',
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
</script>

<style scoped>
.glossary-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  color: var(--color-text);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem;
  gap: 2rem;
}

.header-content h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #fff 0%, #94a3b8 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  color: var(--color-text-muted);
  font-size: 1.1rem;
}

.search-box input {
  width: 400px;
  padding: 0.85rem 1.4rem;
  background: rgba(30, 41, 59, 0.4);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: #fff;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-box input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.15);
  outline: none;
}

.category-tabs {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 0.6rem 1.2rem;
  background: var(--color-bg-light);
  border: 1px solid var(--color-border);
  border-radius: 99px;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.95rem;
}

.tab-btn:hover {
  border-color: var(--color-text-muted);
}

.tab-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.terms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.term-card {
  background: rgba(30, 41, 59, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1.75rem;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.term-card:hover {
  transform: translateY(-6px) scale(1.02);
  border-color: hsla(var(--h-primary), var(--s-primary), var(--l-primary), 0.4);
  background: rgba(30, 41, 59, 0.5);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2);
}

.term-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.term-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #fff;
  line-height: 1.4;
}

.category-tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 6px;
  font-weight: 600;
}

.category-tag.asset { background: rgba(34, 197, 94, 0.15); color: #4ade80; }
.category-tag.liability { background: rgba(239, 68, 68, 0.15); color: #f87171; }
.category-tag.income { background: rgba(99, 102, 241, 0.15); color: #818cf8; }
.category-tag.expense { background: rgba(245, 158, 11, 0.15); color: #fbbf24; }
.category-tag.concept { background: rgba(6, 182, 212, 0.15); color: #22d3ee; }

.description {
  color: #d1d5db;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.examples {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px dashed var(--color-border);
  font-size: 0.88rem;
}

.example-label {
  color: var(--color-text-muted);
  font-weight: 600;
}

.examples p {
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.empty-state {
  text-align: center;
  padding: 4rem;
  color: var(--color-text-muted);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .search-box input {
    width: 100%;
  }
  .terms-grid {
    grid-template-columns: 1fr;
  }
}
</style>
