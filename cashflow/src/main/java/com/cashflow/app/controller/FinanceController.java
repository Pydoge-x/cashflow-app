package com.cashflow.app.controller;

import com.cashflow.app.entity.BalanceSheetItem;
import com.cashflow.app.entity.IncomeExpenseItem;
import com.cashflow.app.entity.Report;
import com.cashflow.app.entity.User;
import com.cashflow.app.repository.BalanceSheetItemRepository;
import com.cashflow.app.repository.IncomeExpenseItemRepository;
import com.cashflow.app.repository.ReportRepository;
import com.cashflow.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
public class FinanceController {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    BalanceSheetItemRepository balanceSheetItemRepository;

    @Autowired
    IncomeExpenseItemRepository incomeExpenseItemRepository;

    @Autowired
    UserRepository userRepository;

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Report validateReportOwnership(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        if (!report.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized: You do not own this report");
        }
        return report;
    }

    // ===== Reports =====

    @GetMapping
    public List<Report> getReports() {
        return reportRepository.findByUserId(getCurrentUser().getId());
    }

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Report report) {
        User currentUser = getCurrentUser();
        // Enforce limit: 1 Personal, 1 Family per user
        if (reportRepository.existsByUserIdAndType(currentUser.getId(), report.getType())) {
            return ResponseEntity.badRequest().body("Error: You already have a report of type " + report.getType());
        }

        report.setUser(currentUser);
        return ResponseEntity.ok(reportRepository.save(report));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        // Validate ownership
        Report report = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
        if (!report.getUser().getId().equals(getCurrentUser().getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        reportRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ===== Balance Sheet =====

    @GetMapping("/{reportId}/balance-sheet")
    public List<BalanceSheetItem> getBalanceSheet(@PathVariable Long reportId) {
        return balanceSheetItemRepository.findByReportId(reportId);
    }

    @PostMapping("/{reportId}/balance-sheet")
    public BalanceSheetItem addBalanceSheetItem(@PathVariable Long reportId, @RequestBody BalanceSheetItem item) {
        Report report = validateReportOwnership(reportId);
        item.setReport(report);
        return balanceSheetItemRepository.save(item);
    }

    @PutMapping("/{reportId}/balance-sheet/{itemId}")
    public BalanceSheetItem updateBalanceSheetItem(@PathVariable Long reportId, @PathVariable Long itemId,
            @RequestBody BalanceSheetItem itemDetails) {
        validateReportOwnership(reportId);
        BalanceSheetItem item = balanceSheetItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        // Double check item belongs to report
        if (!item.getReport().getId().equals(reportId)) {
            throw new RuntimeException("Item does not belong to this report");
        }
        item.setName(itemDetails.getName());
        item.setAmount(itemDetails.getAmount());
        item.setCategory(itemDetails.getCategory());
        item.setNote(itemDetails.getNote());
        return balanceSheetItemRepository.save(item);
    }

    @DeleteMapping("/{reportId}/balance-sheet/{itemId}")
    public ResponseEntity<?> deleteBalanceSheetItem(@PathVariable Long reportId, @PathVariable Long itemId) {
        validateReportOwnership(reportId);
        BalanceSheetItem item = balanceSheetItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getReport().getId().equals(reportId)) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        balanceSheetItemRepository.deleteById(itemId);
        return ResponseEntity.ok().build();
    }

    // ===== Income / Expense =====

    @GetMapping("/{reportId}/income-expense")
    public List<IncomeExpenseItem> getIncomeExpense(@PathVariable Long reportId) {
        return incomeExpenseItemRepository.findByReportId(reportId);
    }

    @PostMapping("/{reportId}/income-expense")
    public IncomeExpenseItem addIncomeExpenseItem(@PathVariable Long reportId, @RequestBody IncomeExpenseItem item) {
        Report report = validateReportOwnership(reportId);
        item.setReport(report);
        return incomeExpenseItemRepository.save(item);
    }

    @PutMapping("/{reportId}/income-expense/{itemId}")
    public IncomeExpenseItem updateIncomeExpenseItem(@PathVariable Long reportId, @PathVariable Long itemId,
            @RequestBody IncomeExpenseItem itemDetails) {
        validateReportOwnership(reportId);
        IncomeExpenseItem item = incomeExpenseItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getReport().getId().equals(reportId)) {
            throw new RuntimeException("Item does not belong to this report");
        }
        item.setName(itemDetails.getName());
        item.setAmount(itemDetails.getAmount());
        item.setCategory(itemDetails.getCategory());
        item.setType(itemDetails.getType());
        item.setIsInterest(itemDetails.getIsInterest());
        item.setNote(itemDetails.getNote());
        return incomeExpenseItemRepository.save(item);
    }

    @DeleteMapping("/{reportId}/income-expense/{itemId}")
    public ResponseEntity<?> deleteIncomeExpenseItem(@PathVariable Long reportId, @PathVariable Long itemId) {
        validateReportOwnership(reportId);
        IncomeExpenseItem item = incomeExpenseItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getReport().getId().equals(reportId)) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        incomeExpenseItemRepository.deleteById(itemId);
        return ResponseEntity.ok().build();
    }

    // ===== Cash Flow Calculation =====

    @GetMapping("/{reportId}/cashflow")
    public ResponseEntity<?> getCashFlow(@PathVariable Long reportId) {
        validateReportOwnership(reportId);
        List<IncomeExpenseItem> items = incomeExpenseItemRepository.findByReportId(reportId);

        List<IncomeExpenseItem> laborIncomeItems = items.stream()
                .filter(i -> i.getType() == IncomeExpenseItem.Type.INCOME
                        && i.getCategory() == IncomeExpenseItem.Category.LABOR_INCOME)
                .toList();
        List<IncomeExpenseItem> assetIncomeItems = items.stream()
                .filter(i -> i.getType() == IncomeExpenseItem.Type.INCOME
                        && i.getCategory() == IncomeExpenseItem.Category.ASSET_INCOME)
                .toList();
        List<IncomeExpenseItem> livingExpenseItems = items.stream()
                .filter(i -> i.getType() == IncomeExpenseItem.Type.EXPENSE
                        && i.getCategory() == IncomeExpenseItem.Category.LIVING_EXPENSE)
                .toList();
        List<IncomeExpenseItem> interestExpenseItems = items.stream()
                .filter(i -> i.getType() == IncomeExpenseItem.Type.EXPENSE && Boolean.TRUE.equals(i.getIsInterest()))
                .toList();
        List<IncomeExpenseItem> excludedItems = items.stream()
                .filter(i -> i.getType() == IncomeExpenseItem.Type.EXPENSE &&
                        i.getCategory() != IncomeExpenseItem.Category.LIVING_EXPENSE &&
                        !Boolean.TRUE.equals(i.getIsInterest()))
                .toList();

        double laborIncome = laborIncomeItems.stream().mapToDouble(IncomeExpenseItem::getAmount).sum();
        double assetIncome = assetIncomeItems.stream().mapToDouble(IncomeExpenseItem::getAmount).sum();
        double livingExpense = livingExpenseItems.stream().mapToDouble(IncomeExpenseItem::getAmount).sum();
        double interestExpense = interestExpenseItems.stream().mapToDouble(IncomeExpenseItem::getAmount).sum();

        double totalIncome = laborIncome + assetIncome;
        double totalExpense = livingExpense + interestExpense;
        double netCashFlow = totalIncome - totalExpense;

        Map<String, Object> result = new HashMap<>();
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense); // Frontend uses totalExpense
        result.put("monthlyCashFlow", netCashFlow); // Frontend uses monthlyCashFlow

        result.put("laborIncome", laborIncome);
        result.put("assetIncome", assetIncome);
        result.put("livingExpense", livingExpense);
        result.put("interestExpense", interestExpense);

        result.put("laborIncomeItems", laborIncomeItems);
        result.put("assetIncomeItems", assetIncomeItems);
        result.put("livingExpenseItems", livingExpenseItems);
        result.put("interestExpenseItems", interestExpenseItems);
        result.put("excludedItems", excludedItems);

        return ResponseEntity.ok(result);
    }
}
