package ru.shift.crm.dto;

import java.time.LocalDateTime;
import ru.shift.crm.entity.Seller;

/**
 * DTO for {@link Seller}
 */
public record SellerCreateResponse(Long id, LocalDateTime registrationDate) {
    public static SellerCreateResponse from(Seller seller) {
        SellerCreateResponse sellerCreateResponse =
                new SellerCreateResponse(seller.getId(), seller.getRegistrationDate());
        return sellerCreateResponse;
    }
}
