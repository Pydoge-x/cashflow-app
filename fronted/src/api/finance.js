import api from './index'

export const financeApi = {
  // ===== 财务报表 =====
  getReports() {
    return api.get('/reports')
  },
  createReport(data) {
    return api.post('/reports', data)
  },
  deleteReport(id) {
    return api.delete(`/reports/${id}`)
  },

  // ===== 资产负债表 =====
  getBalanceSheet(reportId) {
    return api.get(`/reports/${reportId}/balance-sheet`)
  },
  addBalanceSheetItem(reportId, data) {
    return api.post(`/reports/${reportId}/balance-sheet`, data)
  },
  updateBalanceSheetItem(reportId, itemId, data) {
    return api.put(`/reports/${reportId}/balance-sheet/${itemId}`, data)
  },
  deleteBalanceSheetItem(reportId, itemId) {
    return api.delete(`/reports/${reportId}/balance-sheet/${itemId}`)
  },

  // ===== 收入支出表 =====
  getIncomeExpense(reportId) {
    return api.get(`/reports/${reportId}/income-expense`)
  },
  addIncomeExpenseItem(reportId, data) {
    return api.post(`/reports/${reportId}/income-expense`, data)
  },
  updateIncomeExpenseItem(reportId, itemId, data) {
    return api.put(`/reports/${reportId}/income-expense/${itemId}`, data)
  },
  deleteIncomeExpenseItem(reportId, itemId) {
    return api.delete(`/reports/${reportId}/income-expense/${itemId}`)
  },

  // ===== 现金流表 =====
  getCashFlow(reportId) {
    return api.get(`/reports/${reportId}/cashflow`)
  }
}
