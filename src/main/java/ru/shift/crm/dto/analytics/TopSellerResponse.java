package ru.shift.crm.dto.analytics;

import java.math.BigDecimal;

public record TopSellerResponse(Long sellerId, String sellerName, BigDecimal totalAmount) {}