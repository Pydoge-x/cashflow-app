package com.cashflow.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "balance_sheet_items")
public class BalanceSheetItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double amount;

    private String note;

    public enum Category {
        CURRENT_ASSET, // 流动资产
        INVESTMENT_ASSET, // 投资性资产
        PERSONAL_ASSET, // 自用资产
        CONSUMER_DEBT, // 消费负债
        INVESTMENT_DEBT, // 投资负债
        PERSONAL_DEBT // 自用资产负债
    }
}
