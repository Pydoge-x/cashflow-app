package com.cashflow.app.repository;

import com.cashflow.app.entity.BalanceSheetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BalanceSheetItemRepository extends JpaRepository<BalanceSheetItem, Long> {
    List<BalanceSheetItem> findByReportId(Long reportId);
}
