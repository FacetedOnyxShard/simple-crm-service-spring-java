package ru.shift.crm.dto;

import ru.shift.crm.entity.PaymentType;
import ru.shift.crm.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long sellerId,
        BigDecimal amount,
        PaymentType paymentType,
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