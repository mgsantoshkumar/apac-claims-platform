package com.chubb.apac.claims.web.dto;

import com.chubb.apac.claims.common.enums.Lob;
import com.chubb.apac.claims.common.enums.Market;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(
        @NotNull Market market,
        @NotNull Lob lob,
        @NotBlank @Size(max = 200) String incidentSummary,
        @NotBlank @Size(max = 4000) String incidentDescription,
        @NotNull LocalDate incidentDate,
        @NotBlank @Size(max = 200) String location,
        @NotNull @DecimalMin("0.0") BigDecimal estimatedLoss
) {
}
