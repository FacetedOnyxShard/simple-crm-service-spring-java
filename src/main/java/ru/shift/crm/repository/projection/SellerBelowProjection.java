package ru.shift.crm.repository.projection;

import java.math.BigDecimal;

public interface SellerBelowProjection {
    Long getSellerId();
    String getSellerName();
    BigDecimal getTotalAmount();
}