package ru.shift.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shift.crm.dto.analytics.BestPeriodResponse;
import ru.shift.crm.dto.analytics.SellerBelowDto;
import ru.shift.crm.dto.analytics.TopSellerResponse;
import ru.shift.crm.entity.Transaction;
import ru.shift.crm.exception.ResourceNotFoundException;
import ru.shift.crm.repository.SellerRepository;
import ru.shift.crm.repository.TransactionRepository;
import ru.shift.crm.repository.projection.TopSellerProjection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;

    public TopSellerResponse getTopSellerByPeriod(LocalDateTime start, LocalDateTime end) {
        List<TopSellerProjection> topList = transactionRepository.findTopSellerByPeriod(start, end);
        if (topList.isEmpty()) {
            throw new ResourceNotFoundException("Нет транзакций за указанный период");
        }
        TopSellerProjection top = topList.getFirst();
        return new TopSellerResponse(top.getSellerId(), top.getSellerName(), top.getTotalAmount());
    }

    public List<SellerBelowDto> getSellersBelowAmount(LocalDateTime start, LocalDateTime end, BigDecimal threshold) {
        return transactionRepository.findSellersBelowAmount(start, end, threshold).stream()
                .map(p -> new SellerBelowDto(p.getSellerId(), p.getSellerName(), p.getTotalAmount()))
                .toList();
    }

    public BestPeriodResponse getBestPeriod(Long sellerId, String periodSize) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("Продавец с id " + sellerId + " не найден");
        }

        List<Transaction> transactions = transactionRepository.findBySellerId(sellerId)
                .stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .toList();

        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("Нет транзакций у продавца");
        }

        ChronoUnit unit = switch (periodSize.toUpperCase()) {
            case "DAY" -> ChronoUnit.DAYS;
            case "WEEK" -> ChronoUnit.WEEKS;
            case "MONTH" -> ChronoUnit.MONTHS;
            default -> throw new IllegalArgumentException("Допустимые периоды: DAY, WEEK, MONTH");
        };

        BestPeriodResponse best = null;
        long maxCount = 0;

        for (int i = 0; i < transactions.size(); i++) {
            LocalDateTime windowStart = transactions.get(i).getTransactionDate();
            LocalDateTime windowEnd = windowStart.plus(1, unit);
            long count = 1;

            for (int j = i + 1; j < transactions.size(); j++) {
                if (transactions.get(j).getTransactionDate().isBefore(windowEnd)) {
                    count++;
                } else {
                    break;
                }
            }

            if (count > maxCount) {
                maxCount = count;
                best = new BestPeriodResponse(windowStart, windowEnd, count);
            }
        }

        return best;
    }
}