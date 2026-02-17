package com.cashflow.app.repository;

import com.cashflow.app.entity.IncomeExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncomeExpenseItemRepository extends JpaRepository<IncomeExpenseItem, Long> {
    List<IncomeExpenseItem> findByReportId(Long reportId);
}
