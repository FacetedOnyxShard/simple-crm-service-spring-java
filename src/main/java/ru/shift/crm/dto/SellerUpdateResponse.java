package ru.shift.crm.dto;

import ru.shift.crm.entity.Seller;

import java.time.LocalDateTime;

/**
 * DTO for {@link Seller}
 */
public record SellerUpdateResponse(Long id, String name, String contactInfo, LocalDateTime registrationDate) {
    public static SellerUpdateResponse from(Seller seller) {
        return new SellerUpdateResponse(
                seller.getId(),
                seller.getName(),
                seller.getContactInfo(),
                seller.getRegistrationDate());
    }
}
