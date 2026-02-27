import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue')
  },
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/DashboardView.vue')
      },
      {
        path: 'balance-sheet/:reportId',
        name: 'BalanceSheet',
        component: () => import('../views/BalanceSheetView.vue'),
        props: true
      },
      {
        path: 'income-expense/:reportId',
        name: 'IncomeExpense',
        component: () => import('../views/IncomeExpenseView.vue'),
        props: true
      },
      {
        path: 'cashflow/:reportId',
        name: 'CashFlow',
        component: () => import('../views/CashFlowView.vue'),
        props: true
      },
      {
        path: 'charts/:reportId',
        name: 'FinancialCharts',
        component: () => import('../views/FinancialChartsView.vue'),
        props: true
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue')
      },
      {
        path: 'glossary',
        name: 'Glossary',
        component: () => import('../views/GlossaryView.vue')
      },
      {
        path: 'ai-assistant',
        name: 'AiAssistant',
        component: () => import('../views/AiAssistantView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫：未登录跳转到登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.name === 'Login' || to.name === 'Register') && token) {
    next('/')
  } else {
    next()
  }
})

export default router
