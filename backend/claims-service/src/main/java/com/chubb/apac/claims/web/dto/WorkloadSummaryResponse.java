package com.chubb.apac.claims.web.dto;

import java.math.BigDecimal;
import java.util.Map;

public record WorkloadSummaryResponse(
        Map<String, Long> byStatus,
        Map<String, Long> byMarket,
        BigDecimal openReserveExposure,
        long unassignedCount,
        long myAssignedCount
) {
}
