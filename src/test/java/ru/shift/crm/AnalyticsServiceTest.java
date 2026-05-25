package ru.shift.crm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.shift.crm.dto.analytics.BestPeriodResponse;
import ru.shift.crm.dto.analytics.SellerBelowDto;
import ru.shift.crm.dto.analytics.TopSellerResponse;
import ru.shift.crm.entity.PaymentType;
import ru.shift.crm.entity.Transaction;
import ru.shift.crm.exception.ResourceNotFoundException;
import ru.shift.crm.repository.SellerRepository;
import ru.shift.crm.repository.TransactionRepository;
import ru.shift.crm.repository.projection.SellerBelowProjection;
import ru.shift.crm.repository.projection.TopSellerProjection;
import ru.shift.crm.service.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private SellerRepository sellerRepository;
    @InjectMocks
    private AnalyticsService analyticsService;

    private TopSellerProjection mockTopProjection(Long id, String name, BigDecimal amount) {
        return new TopSellerProjection() {
            @Override
            public Long getSellerId() { return id; }
            @Override
            public String getSellerName() { return name; }
            @Override
            public BigDecimal getTotalAmount() { return amount; }
        };
    }

    private SellerBelowProjection mockBelowProjection(Long id, String name, BigDecimal amount) {
        return new SellerBelowProjection() {
            @Override
            public Long getSellerId() { return id; }
            @Override
            public String getSellerName() { return name; }
            @Override
            public BigDecimal getTotalAmount() { return amount; }
        };
    }

    @Test
    void getTopSellerByPeriod_ShouldReturnTop() {
        when(transactionRepository.findTopSellerByPeriod(any(), any()))
                .thenReturn(List.of(mockTopProjection(1L, "Alice", BigDecimal.valueOf(500))));

        TopSellerResponse result = analyticsService.getTopSellerByPeriod(
                LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertThat(result.sellerId()).isEqualTo(1L);
        assertThat(result.totalAmount()).isEqualByComparingTo("500");
    }

    @Test
    void getTopSellerByPeriod_Empty_ShouldThrow() {
        when(transactionRepository.findTopSellerByPeriod(any(), any()))
                .thenReturn(List.of());
        assertThatThrownBy(() -> analyticsService.getTopSellerByPeriod(
                LocalDateTime.now().minusDays(1), LocalDateTime.now()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSellersBelowAmount_ShouldReturnList() {
        when(transactionRepository.findSellersBelowAmount(any(), any(), any()))
                .thenReturn(List.of(mockBelowProjection(2L, "Bob", BigDecimal.valueOf(100))));

        List<SellerBelowDto> result = analyticsService.getSellersBelowAmount(
                LocalDateTime.now().minusDays(1), LocalDateTime.now(), BigDecimal.valueOf(200));
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().totalAmount()).isEqualByComparingTo("100");
    }

    @Test
    void getBestPeriod_Success() {
        when(sellerRepository.existsById(1L)).thenReturn(true);
        Transaction tx1 = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.ONE)
                .paymentType(PaymentType.CASH)
                .transactionDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();
        Transaction tx2 = Transaction.builder()
                .id(2L)
                .amount(BigDecimal.TEN)
                .paymentType(PaymentType.CARD)
                .transactionDate(LocalDateTime.of(2025, 1, 1, 11, 0))
                .build();
        when(transactionRepository.findBySellerId(1L)).thenReturn(List.of(tx1, tx2));

        BestPeriodResponse result = analyticsService.getBestPeriod(1L, "DAY");
        assertThat(result.transactionCount()).isEqualTo(2);
        assertThat(result.periodStart()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
    }

    @Test
    void getBestPeriod_SellerNotFound_ShouldThrow() {
        when(sellerRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> analyticsService.getBestPeriod(99L, "MONTH"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}