package ru.shift.crm.dto;

import jakarta.validation.constraints.NotBlank;
import ru.shift.crm.entity.Seller;

/**
 * DTO for {@link Seller}
 */
public record SellerCreateRequest(@NotBlank String name, @NotBlank String contactInfo) {
    public Seller toEntity() {
        return Seller.builder().name(name).contactInfo(contactInfo).build();
    }
}