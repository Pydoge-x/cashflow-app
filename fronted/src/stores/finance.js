import { defineStore } from 'pinia'
import { ref } from 'vue'
import { financeApi } from '../api/finance'

export const useFinanceStore = defineStore('finance', () => {
  const reports = ref([])
  const currentReport = ref(null)
  const balanceSheet = ref([])
  const incomeExpense = ref([])
  const cashFlow = ref(null)
  const loading = ref(false)

  // ===== 报表管理 =====
  async function fetchReports() {
    loading.value = true
    try {
      const res = await financeApi.getReports()
      reports.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function createReport(data) {
    const res = await financeApi.createReport(data)
    reports.value.push(res.data)
    return res.data
  }

  async function deleteReport(id) {
    await financeApi.deleteReport(id)
    reports.value = reports.value.filter(r => r.id !== id)
  }

  // ===== 资产负债表 =====
  async function fetchBalanceSheet(reportId) {
    loading.value = true
    try {
      const res = await financeApi.getBalanceSheet(reportId)
      balanceSheet.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function addBalanceSheetItem(reportId, data) {
    const res = await financeApi.addBalanceSheetItem(reportId, data)
    balanceSheet.value.push(res.data)
    return res.data
  }

  async function updateBalanceSheetItem(reportId, itemId, data) {
    const res = await financeApi.updateBalanceSheetItem(reportId, itemId, data)
    const idx = balanceSheet.value.findIndex(i => i.id === itemId)
    if (idx !== -1) balanceSheet.value[idx] = res.data
    return res.data
  }

  async function deleteBalanceSheetItem(reportId, itemId) {
    await financeApi.deleteBalanceSheetItem(reportId, itemId)
    balanceSheet.value = balanceSheet.value.filter(i => i.id !== itemId)
  }

  // ===== 收入支出表 =====
  async function fetchIncomeExpense(reportId) {
    loading.value = true
    try {
      const res = await financeApi.getIncomeExpense(reportId)
      incomeExpense.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function addIncomeExpenseItem(reportId, data) {
    const res = await financeApi.addIncomeExpenseItem(reportId, data)
    incomeExpense.value.push(res.data)
    return res.data
  }

  async function updateIncomeExpenseItem(reportId, itemId, data) {
    const res = await financeApi.updateIncomeExpenseItem(reportId, itemId, data)
    const idx = incomeExpense.value.findIndex(i => i.id === itemId)
    if (idx !== -1) incomeExpense.value[idx] = res.data
    return res.data
  }

  async function deleteIncomeExpenseItem(reportId, itemId) {
    await financeApi.deleteIncomeExpenseItem(reportId, itemId)
    incomeExpense.value = incomeExpense.value.filter(i => i.id !== itemId)
  }

  // ===== 现金流 =====
  async function fetchCashFlow(reportId) {
    loading.value = true
    try {
      const res = await financeApi.getCashFlow(reportId)
      cashFlow.value = res.data
    } finally {
      loading.value = false
    }
  }

  return {
    reports, currentReport, balanceSheet, incomeExpense, cashFlow, loading,
    fetchReports, createReport, deleteReport,
    fetchBalanceSheet, addBalanceSheetItem, updateBalanceSheetItem, deleteBalanceSheetItem,
    fetchIncomeExpense, addIncomeExpenseItem, updateIncomeExpenseItem, deleteIncomeExpenseItem,
    fetchCashFlow
  }
})
