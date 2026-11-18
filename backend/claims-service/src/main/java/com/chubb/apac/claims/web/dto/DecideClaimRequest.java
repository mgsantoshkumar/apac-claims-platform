package com.chubb.apac.claims.web.dto;

import com.chubb.apac.claims.common.enums.DecisionOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DecideClaimRequest(
        @NotNull DecisionOutcome outcome,
        @NotBlank @Size(max = 2000) String reason,
        BigDecimal settlementAmount,
        BigDecimal reserveAmount
) {
}
