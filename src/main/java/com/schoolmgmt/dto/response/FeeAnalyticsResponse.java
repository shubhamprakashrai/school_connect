package com.schoolmgmt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeAnalyticsResponse {

    private BigDecimal totalCollected;
    private BigDecimal totalPending;
    private BigDecimal totalOverdue;
    private List<MonthlyCollection> monthlyCollection;
    private Map<String, Long> paymentModeBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyCollection {
        private String month;
        private BigDecimal amount;
    }
}
