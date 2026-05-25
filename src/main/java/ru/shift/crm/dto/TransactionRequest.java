package ru.shift.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.shift.crm.entity.PaymentType;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link Transaction}
 */
public record TransactionRequest(
        @NotNull(message = "Seller ID is required")
        Long sellerId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Payment Type is required")
        PaymentType paymentType,

        LocalDateTime transactionDate
) {
    public Transaction toEntity(Seller seller) {
        return Transaction.builder()
                .seller(seller)
                .amount(amount)
                .paymentType(paymentType)
                .transactionDate(transactionDate == null ? LocalDateTime.now() : transactionDate)
                .build();
    }
}