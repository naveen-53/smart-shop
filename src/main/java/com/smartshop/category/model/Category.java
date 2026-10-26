package com.smartshop.category.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Category(
        UUID id,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}