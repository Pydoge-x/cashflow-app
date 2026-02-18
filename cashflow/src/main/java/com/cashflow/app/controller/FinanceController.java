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
import java.util.Set;
import java.util.stream.Collectors;

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
        BalanceSheetItem savedItem = balanceSheetItemRepository.save(item);

        // Sync with Income/Expense if a matching name exists
        incomeExpenseItemRepository.findByReportId(reportId).stream()
                .filter(ie -> ie.getName().equals(item.getName()))
                .findFirst()
                .ifPresent(ie -> {
                    ie.setAmount(item.getAmount());
                    if (item.getIsInterest() != null) {
                        ie.setIsInterest(item.getIsInterest());
                    }
                    if (item.getInterestAmount() != null) {
                        ie.setInterestAmount(item.getInterestAmount());
                    }
                    incomeExpenseItemRepository.save(ie);
                });

        return savedItem;
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
        item.setIsInterest(itemDetails.getIsInterest());
        item.setInterestAmount(itemDetails.getInterestAmount());

        BalanceSheetItem savedItem = balanceSheetItemRepository.save(item);

        // Sync with Income/Expense if a matching name exists
        incomeExpenseItemRepository.findByReportId(reportId).stream()
                .filter(ie -> ie.getName().equals(item.getName()))
                .findFirst()
                .ifPresent(ie -> {
                    ie.setAmount(item.getAmount());
                    if (item.getIsInterest() != null) {
                        ie.setIsInterest(item.getIsInterest());
                    }
                    if (item.getInterestAmount() != null) {
                        ie.setInterestAmount(item.getInterestAmount());
                    }
                    incomeExpenseItemRepository.save(ie);
                });

        return savedItem;
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
        IncomeExpenseItem savedItem = incomeExpenseItemRepository.save(item);

        // Sync with Balance Sheet if a matching name exists
        balanceSheetItemRepository.findByReportId(reportId).stream()
                .filter(bs -> bs.getName().equals(item.getName()))
                .findFirst()
                .ifPresent(bs -> {
                    bs.setAmount(item.getAmount());
                    if (item.getIsInterest() != null) {
                        bs.setIsInterest(item.getIsInterest());
                    }
                    if (item.getInterestAmount() != null) {
                        bs.setInterestAmount(item.getInterestAmount());
                    }
                    balanceSheetItemRepository.save(bs);
                });

        return savedItem;
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
        item.setInterestAmount(itemDetails.getInterestAmount());
        item.setNote(itemDetails.getNote());

        IncomeExpenseItem savedItem = incomeExpenseItemRepository.save(item);

        // Sync with Balance Sheet if a matching name exists
        balanceSheetItemRepository.findByReportId(reportId).stream()
                .filter(bs -> bs.getName().equals(item.getName()))
                .findFirst()
                .ifPresent(bs -> {
                    bs.setAmount(item.getAmount());
                    if (item.getIsInterest() != null) {
                        bs.setIsInterest(item.getIsInterest());
                    }
                    if (item.getInterestAmount() != null) {
                        bs.setInterestAmount(item.getInterestAmount());
                    }
                    balanceSheetItemRepository.save(bs);
                });

        return savedItem;
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
        List<BalanceSheetItem> bsItems = balanceSheetItemRepository.findByReportId(reportId);

        // 1. Calculate Income
        // Labor Income (Only from IE)
        List<IncomeExpenseItem> laborIncomeItems = items.stream()
                .filter(i -> i.getType() == IncomeExpenseItem.Type.INCOME
                        && i.getCategory() == IncomeExpenseItem.Category.LABOR_INCOME)
                .toList();
        double laborIncome = laborIncomeItems.stream().mapToDouble(IncomeExpenseItem::getAmount).sum();

        // Asset Income (Merged BS Assets + IE Asset Income)
        List<BalanceSheetItem> bsAssets = bsItems.stream()
                .filter(i -> i.getCategory() == BalanceSheetItem.Category.CURRENT_ASSET ||
                        i.getCategory() == BalanceSheetItem.Category.INVESTMENT_ASSET ||
                        i.getCategory() == BalanceSheetItem.Category.PERSONAL_ASSET)
                .toList();
        Map<String, Double> assetIncomeMap = new HashMap<>();
        for (BalanceSheetItem asset : bsAssets) {
            assetIncomeMap.put(asset.getName(), asset.getAmount());
        }
        for (IncomeExpenseItem ieItem : items) {
            if (ieItem.getType() == IncomeExpenseItem.Type.INCOME
                    && ieItem.getCategory() == IncomeExpenseItem.Category.ASSET_INCOME) {
                assetIncomeMap.put(ieItem.getName(), ieItem.getAmount());
            }
        }
        double assetIncome = assetIncomeMap.values().stream().mapToDouble(Double::doubleValue).sum();

        // 2. Calculate Expenses
        // Living Expense (Only from IE)
        List<IncomeExpenseItem> livingExpenseItems = items.stream()
                .filter(i -> i.getType() == IncomeExpenseItem.Type.EXPENSE
                        && i.getCategory() == IncomeExpenseItem.Category.LIVING_EXPENSE)
                .toList();
        double livingExpense = livingExpenseItems.stream().mapToDouble(IncomeExpenseItem::getAmount).sum();

        // Interest Expense (General IE Interest only, excluding BS debt interest)
        List<BalanceSheetItem> bsDebts = bsItems.stream()
                .filter(i -> i.getCategory() == BalanceSheetItem.Category.CONSUMER_DEBT ||
                        i.getCategory() == BalanceSheetItem.Category.INVESTMENT_DEBT ||
                        i.getCategory() == BalanceSheetItem.Category.PERSONAL_DEBT)
                .toList();

        Set<String> debtNames = bsDebts.stream().map(BalanceSheetItem::getName).collect(Collectors.toSet());

        Map<String, Double> interestExpenseMap = new HashMap<>();
        for (IncomeExpenseItem ieItem : items) {
            if (ieItem.getType() == IncomeExpenseItem.Type.EXPENSE && Boolean.TRUE.equals(ieItem.getIsInterest())) {
                // Only include if NOT a synced BS debt (those go to Asset Expense)
                if (!debtNames.contains(ieItem.getName())) {
                    interestExpenseMap.put(ieItem.getName(), ieItem.getAmount());
                }
            }
        }
        double interestExpense = interestExpenseMap.values().stream().mapToDouble(Double::doubleValue).sum();

        // Asset Expense (Merged BS Debt Interest + Non-Debt IE Asset Expense)
        Map<String, Double> assetExpenseMap = new HashMap<>();
        // 1. Add interest from BS debts
        for (BalanceSheetItem debt : bsDebts) {
            if (Boolean.TRUE.equals(debt.getIsInterest()) && debt.getInterestAmount() != null
                    && debt.getInterestAmount() > 0) {
                assetExpenseMap.put(debt.getName() + " (利息)", debt.getInterestAmount());
            }
        }
        // 2. Add other IE asset expenses that are NOT debt principal
        for (IncomeExpenseItem ieItem : items) {
            if (ieItem.getType() == IncomeExpenseItem.Type.EXPENSE &&
                    (ieItem.getCategory() == IncomeExpenseItem.Category.ASSET_EXPENSE ||
                            ieItem.getCategory() == IncomeExpenseItem.Category.LOAN_REPAYMENT)) {

                // If it's a debt, only include it if it's specifically marked as interest (and
                // not already added)
                if (debtNames.contains(ieItem.getName())) {
                    if (Boolean.TRUE.equals(ieItem.getIsInterest()) && ieItem.getInterestAmount() != null
                            && ieItem.getInterestAmount() > 0) {
                        assetExpenseMap.put(ieItem.getName(), ieItem.getInterestAmount());
                    }
                    // Else: It's principal repayment, exclude from cash flow as per requirement
                } else {
                    // It's a general asset purchase or loan repayment not in BS
                    assetExpenseMap.put(ieItem.getName(), ieItem.getAmount());
                }
            }
        }
        double assetExpense = assetExpenseMap.values().stream().mapToDouble(Double::doubleValue).sum();

        // 3. Totals
        double totalIncome = laborIncome + assetIncome;
        double totalExpense = livingExpense + interestExpense + assetExpense;
        double netCashFlow = totalIncome - totalExpense;

        // 4. Prepare Response
        List<Map<String, Object>> assetIncomeDisplayItems = assetIncomeMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("amount", e.getValue());
                    return m;
                }).toList();
        List<Map<String, Object>> assetExpenseDisplayItems = assetExpenseMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("amount", e.getValue());
                    return m;
                }).toList();
        List<Map<String, Object>> interestDisplayItems = interestExpenseMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("amount", e.getValue());
                    return m;
                }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense);
        result.put("monthlyCashFlow", netCashFlow);

        result.put("laborIncome", laborIncome);
        result.put("assetIncome", assetIncome);
        result.put("livingExpense", livingExpense);
        result.put("interestExpense", interestExpense);
        result.put("assetExpense", assetExpense);

        result.put("laborIncomeItems", laborIncomeItems);
        result.put("assetIncomeItems", assetIncomeDisplayItems);
        result.put("livingExpenseItems", livingExpenseItems);
        result.put("interestExpenseItems", interestDisplayItems);
        result.put("assetExpenseItems", assetExpenseDisplayItems);
        result.put("excludedItems", List.of());

        return ResponseEntity.ok(result);
    }
}
