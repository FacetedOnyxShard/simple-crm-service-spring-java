package ru.shift.crm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.shift.crm.dto.analytics.BestPeriodResponse;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.entity.Transaction;
import ru.shift.crm.exception.ResourceNotFoundException;import ru.shift.crm.repository.SellerRepository;
import ru.shift.crm.repository.TransactionRepository;
import ru.shift.crm.service.AnalyticsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticServiceFindBestPeriodTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AnalyticsService analyticService;

    private final Long SELLER_ID = 1L;

    @BeforeEach
    void setUp() {
        when(sellerRepository.existsById(SELLER_ID)).thenReturn(true);

        Seller seller = new Seller();
        seller.setId(SELLER_ID);
        when(sellerRepository.findById(SELLER_ID)).thenReturn(Optional.of(seller));
    }

    @Test
    @DisplayName("Должен найти период с 3 транзакциями в течение 1 дня")
    void shouldFindBestPeriodWithThreeTransactionsInOneDay() {
        List<Transaction> transactions = List.of(
                createTransaction(1L, LocalDateTime.of(2024, 1, 1, 10, 0)),
                createTransaction(2L, LocalDateTime.of(2024, 1, 1, 15, 0)),
                createTransaction(3L, LocalDateTime.of(2024, 1, 1, 20, 0)),
                createTransaction(4L, LocalDateTime.of(2024, 1, 2, 10, 0))
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        BestPeriodResponse result = analyticService.getBestPeriod(SELLER_ID, "DAY");

        assertNotNull(result);
        assertEquals(3, result.transactionCount());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), result.periodStart());
        assertEquals(LocalDateTime.of(2024, 1, 2, 10, 0), result.periodEnd());
    }

    @Test
    @DisplayName("Должен найти период с максимальным количеством транзакций за неделю")
    void shouldFindBestPeriodWithMaxTransactionsInWeek() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        List<Transaction> transactions = List.of(
                createTransaction(1L, baseTime),
                createTransaction(2L, baseTime.plusDays(1)),
                createTransaction(3L, baseTime.plusDays(2)),
                createTransaction(4L, baseTime.plusDays(6)),
                createTransaction(5L, baseTime.plusDays(8)),
                createTransaction(6L, baseTime.plusDays(9))
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        BestPeriodResponse result = analyticService.getBestPeriod(SELLER_ID, "WEEK");

        assertNotNull(result);
        assertEquals(4, result.transactionCount());
        assertEquals(baseTime, result.periodStart());
        assertEquals(baseTime.plusWeeks(1), result.periodEnd());
    }

    @Test
    @DisplayName("Должен найти период с транзакциями за месяц")
    void shouldFindBestPeriodWithMonthWindow() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
        List<Transaction> transactions = List.of(
                createTransaction(1L, start),
                createTransaction(2L, start.plusDays(10)),
                createTransaction(3L, start.plusDays(20)),
                createTransaction(4L, start.plusDays(35))
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        BestPeriodResponse result = analyticService.getBestPeriod(SELLER_ID, "MONTH");

        assertNotNull(result);
        assertEquals(3, result.transactionCount());
    }

    @Test
    @DisplayName("Должен правильно обработать граничные случаи - транзакция ровно в windowEnd")
    void shouldHandleBoundaryCaseWhenTransactionAtWindowEnd() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime atWindowEnd = start.plusDays(1);
        List<Transaction> transactions = List.of(
                createTransaction(1L, start),
                createTransaction(2L, start.plusHours(12)),
                createTransaction(3L, atWindowEnd)
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        BestPeriodResponse result = analyticService.getBestPeriod(SELLER_ID, "DAY");

        assertNotNull(result);
        assertEquals(2, result.transactionCount());
        assertEquals(start, result.periodStart());
        assertEquals(start.plusDays(1), result.periodEnd());
    }

    @Test
    @DisplayName("Должен обновлять максимум только при строго большем количестве")
    void shouldUpdateMaxOnlyWhenStrictlyGreater() {
        List<Transaction> transactions = List.of(
                createTransaction(1L, LocalDateTime.of(2024, 1, 1, 10, 0)),
                createTransaction(2L, LocalDateTime.of(2024, 1, 1, 11, 0)),
                createTransaction(3L, LocalDateTime.of(2024, 1, 1, 12, 0)),
                createTransaction(4L, LocalDateTime.of(2024, 1, 2, 10, 0)),
                createTransaction(5L, LocalDateTime.of(2024, 1, 2, 11, 0)),
                createTransaction(6L, LocalDateTime.of(2024, 1, 2, 12, 0))
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        BestPeriodResponse result = analyticService.getBestPeriod(SELLER_ID, "DAY");

        assertNotNull(result);
        assertEquals(3, result.transactionCount());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), result.periodStart());
    }

    @Test
    @DisplayName("Должен корректно обработать одну транзакцию")
    void shouldHandleSingleTransaction() {
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 12, 0);
        List<Transaction> transactions = List.of(
                createTransaction(1L, date)
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        BestPeriodResponse result = analyticService.getBestPeriod(SELLER_ID, "DAY");

        assertNotNull(result);
        assertEquals(1, result.transactionCount());
        assertEquals(date, result.periodStart());
        assertEquals(date.plusDays(1), result.periodEnd());
    }

    @Test
    @DisplayName("Должен найти лучший период, когда максимум не с первого окна")
    void shouldFindBestPeriodWhenMaxNotFromFirstWindow() {
        List<Transaction> transactions = List.of(
                createTransaction(1L, LocalDateTime.of(2024, 1, 1, 10, 0)),
                createTransaction(2L, LocalDateTime.of(2024, 1, 2, 10, 0)),
                createTransaction(3L, LocalDateTime.of(2024, 1, 3, 10, 0)),
                createTransaction(4L, LocalDateTime.of(2024, 1, 3, 12, 0)),
                createTransaction(5L, LocalDateTime.of(2024, 1, 3, 14, 0))
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        BestPeriodResponse result = analyticService.getBestPeriod(SELLER_ID, "DAY");

        assertNotNull(result);
        assertEquals(3, result.transactionCount());
        assertEquals(LocalDateTime.of(2024, 1, 3, 10, 0), result.periodStart());
    }

    @ParameterizedTest
    @DisplayName("Должен корректно обработать разные единицы времени")
    @CsvSource({
            "DAY, 1",
            "WEEK, 7",
            "MONTH, 30"
    })
     void shouldHandleDifferentTimeUnits(String periodSize, long daysToAdd) {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 12, 0);
        List<Transaction> transactions = List.of(
                createTransaction(1L, start),
                createTransaction(2L, start.plusDays(1)),
                createTransaction(3L, start.plusDays(2))
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        BestPeriodResponse result = analyticService.getBestPeriod(SELLER_ID, periodSize);

        assertNotNull(result);
        assertEquals(start, result.periodStart());
        if (periodSize.equals("MONTH")) {
            assertEquals(start.plusMonths(1), result.periodEnd());
        } else if (periodSize.equals("WEEK")) {
            assertEquals(start.plusWeeks(1), result.periodEnd());
        } else {
            assertEquals(start.plusDays(1), result.periodEnd());
        }
    }


    @Test
    @DisplayName("Должен выбросить исключение при неверном параметре periodSize")
    void shouldThrowExceptionForInvalidPeriodSize() {
        List<Transaction> transactions = List.of(
                createTransaction(1L, LocalDateTime.now())
        );
        when(transactionRepository.findBySellerId(SELLER_ID)).thenReturn(transactions);

        assertThrows(IllegalArgumentException.class,
                () -> analyticService.getBestPeriod(SELLER_ID, "YEAR"));
    }


    private Transaction createTransaction(Long id, LocalDateTime date) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setTransactionDate(date);
        transaction.setSeller(
                sellerRepository.findById(SELLER_ID).orElseThrow(()
                        -> new RuntimeException("Seller not found with id: " + id)));
        return transaction;
    }
}