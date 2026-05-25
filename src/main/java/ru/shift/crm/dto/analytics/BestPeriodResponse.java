package ru.shift.crm.dto.analytics;

import java.time.LocalDateTime;

public record BestPeriodResponse(
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        long transactionCount
) {}