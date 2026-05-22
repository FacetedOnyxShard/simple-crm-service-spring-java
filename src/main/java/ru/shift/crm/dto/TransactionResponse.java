package ru.shift.crm.dto;

import ru.shift.crm.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link Transaction}
 */
public record TransactionResponse(
        Long id,
        Long sellerId,
        BigDecimal amount,
        String paymentType,
        LocalDateTime transactionDate
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getSeller().getId(),
                transaction.getAmount(),
                transaction.getPaymentType(),
                transaction.getTransactionDate()
        );
    }
}
