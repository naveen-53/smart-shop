package com.smartshop.category.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryStatusRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {}