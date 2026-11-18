package com.chubb.apac.claims.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InfoMessageRequest(
        @NotBlank @Size(max = 4000) String message
) {
}
