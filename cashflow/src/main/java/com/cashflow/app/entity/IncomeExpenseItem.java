package com.cashflow.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(name = "income_expense_items")
public class IncomeExpenseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "is_interest")
    private Boolean isInterest = false; // Only relevant for debts/expenses to mark if it's interest payment (cashflow
                                        // relevant)

    private String note;

    public enum Type {
        INCOME,
        EXPENSE
    }

    public enum Category {
        LABOR_INCOME, // 劳动收入
        ASSET_INCOME, // 资产收入
        LIVING_EXPENSE, // 生活支出
        ASSET_EXPENSE, // 资产性支出 (Contains principal repayment)
        LOAN_REPAYMENT // 借款还款
    }
}
