package ru.shift.crm.dto.analytics;

import java.math.BigDecimal;

public record SellerBelowDto(Long sellerId, String sellerName, BigDecimal totalAmount) {}