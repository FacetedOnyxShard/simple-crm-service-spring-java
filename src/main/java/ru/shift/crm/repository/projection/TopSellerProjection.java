package ru.shift.crm.repository.projection;

import java.math.BigDecimal;

public interface TopSellerProjection {
    Long getSellerId();
    String getSellerName();
    BigDecimal getTotalAmount();
}