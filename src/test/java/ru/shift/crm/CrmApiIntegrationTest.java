package ru.shift.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.shift.crm.dto.SellerCreateRequest;
import ru.shift.crm.dto.SellerUpdateRequest;
import ru.shift.crm.dto.TransactionRequest;
import ru.shift.crm.dto.analytics.BestPeriodResponse;
import ru.shift.crm.dto.analytics.SellerBelowDto;
import ru.shift.crm.dto.analytics.TopSellerResponse;
import ru.shift.crm.entity.PaymentType;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.entity.Transaction;
import ru.shift.crm.exception.ResourceNotFoundException;
import ru.shift.crm.repository.SellerRepository;
import ru.shift.crm.repository.TransactionRepository;
import ru.shift.crm.service.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestConfig.class)
class CrmApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    @Test
    void getAllSellers_Empty_ShouldReturnEmptyJsonArray() throws Exception {
        mockMvc.perform(get("/api/sellers"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createAndGetAllSellers() throws Exception {
        SellerCreateRequest request = new SellerCreateRequest("Alice", "alice@example.com");

        mockMvc.perform(post("/api/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.registrationDate").isNotEmpty());

        mockMvc.perform(get("/api/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].contactInfo").value("alice@example.com"));
    }

    @Test
    void getSellerById_Found() throws Exception {
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("Bob")
                        .contactInfo("bob@mail.com")
                        .build());

        mockMvc.perform(get("/api/sellers/{id}", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    void getSellerById_NotFound() throws Exception {
        mockMvc.perform(get("/api/sellers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSeller_ValidationError_BlankName() throws Exception {
        SellerCreateRequest badRequest = new SellerCreateRequest("", "contact");
        mockMvc.perform(post("/api/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSeller_Success() throws Exception {
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("Old")
                        .contactInfo("old@mail.com")
                        .build());

        SellerUpdateRequest update = new SellerUpdateRequest("New", "new@mail.com");

        mockMvc.perform(put("/api/sellers/{id}", seller.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.contactInfo").value("new@mail.com"));
    }

    @Test
    void updateSeller_NotFound() throws Exception {
        SellerUpdateRequest update = new SellerUpdateRequest("Name", "contact");
        mockMvc.perform(put("/api/sellers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSeller_ValidationError() throws Exception {
        SellerUpdateRequest invalid = new SellerUpdateRequest("", "contact");
        mockMvc.perform(put("/api/sellers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSeller_Success() throws Exception {
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("DeleteMe")
                        .contactInfo("del@mail.com")
                        .build());

        mockMvc.perform(delete("/api/sellers/{id}", seller.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSeller_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/sellers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.message").value("Seller not found with id: 999"));
    }

    @Test
    void getSellerTransactions_Empty() throws Exception {
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("Seller")
                        .contactInfo("s@mail.com")
                        .build());

        mockMvc.perform(get("/api/sellers/{id}/transactions", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getSellerTransactions_WithData() throws Exception {
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("Seller")
                        .contactInfo("s@mail.com")
                        .build());
        Transaction tx = transactionRepository.save(
                new Transaction(null, seller, BigDecimal.valueOf(50), PaymentType.CASH, java.time.LocalDateTime.now()));

        mockMvc.perform(get("/api/sellers/{id}/transactions", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tx.getId().intValue()))
                .andExpect(jsonPath("$[0].amount").value(50.0))
                .andExpect(jsonPath("$[0].paymentType").value("CASH"));
    }

    @Test
    void getAllTransactions_Empty() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createAndGetTransaction() throws Exception {
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("Seller")
                        .contactInfo("s@mail.com")
                        .build());

        TransactionRequest txReq = new TransactionRequest(seller.getId(),
                BigDecimal.valueOf(99.99), PaymentType.CARD, null);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sellerId").value(seller.getId().intValue()))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.paymentType").value("CARD"));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(99.99));
    }

    @Test
    void createTransaction_SellerNotFound() throws Exception {
        TransactionRequest txReq = new TransactionRequest(999L, BigDecimal.ONE, PaymentType.CASH, null);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_ValidationError_NegativeAmount() throws Exception {
        TransactionRequest txReq =
                new TransactionRequest(1L, BigDecimal.valueOf(-10),
                        PaymentType.CASH, null);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionById_Found() throws Exception {
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("Old")
                        .contactInfo("old@mail.com")
                        .build());
        Transaction tx = transactionRepository.save(
                new Transaction(null, seller, BigDecimal.valueOf(30),
                        PaymentType.TRANSFER, java.time.LocalDateTime.now()));

        mockMvc.perform(get("/api/transactions/{id}", tx.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tx.getId().intValue()))
                .andExpect(jsonPath("$.paymentType").value("TRANSFER"));
    }

    @Test
    void getTransactionById_NotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTopSeller_shouldReturnTopSeller() throws Exception {
        TopSellerResponse response = new TopSellerResponse(1L, "Alice", new BigDecimal("500.00"));
        when(analyticsService.getTopSellerByPeriod(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/analytics/top-seller")
                        .param("period", "day")
                        .param("date", "2025-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.sellerName").value("Alice"))
                .andExpect(jsonPath("$.totalAmount").value(500.0));
    }

    @Test
    void getTopSeller_invalidPeriod_shouldReturn500() throws Exception {
        mockMvc.perform(get("/api/analytics/top-seller")
                        .param("period", "week")
                        .param("date", "2025-01-15"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").value("Некорректный аргумент"))
                .andExpect(jsonPath("$.message").value("Период должен быть day, month, quarter или year"));
    }

    @Test
    void getTopSeller_noTransactions_shouldReturn404() throws Exception {
        when(analyticsService.getTopSellerByPeriod(any(), any()))
                .thenThrow(new ResourceNotFoundException("Нет транзакций за указанный период"));

        mockMvc.perform(get("/api/analytics/top-seller")
                        .param("period", "month")
                        .param("date", "2025-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.message").value("Нет транзакций за указанный период"));
    }

    @Test
    void getTopSeller_missingPeriod_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/analytics/top-seller")
                        .param("date", "2025-01-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTopSeller_invalidDateFormat_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/analytics/top-seller")
                        .param("period", "day")
                        .param("date", "15-01-2025"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getSellersBelow_shouldReturnList() throws Exception {
        List<SellerBelowDto> list = List.of(
                new SellerBelowDto(1L, "Bob", new BigDecimal("100.00")),
                new SellerBelowDto(2L, "Charlie", new BigDecimal("50.00"))
        );
        when(analyticsService.getSellersBelowAmount(any(), any(), eq(new BigDecimal("200.00"))))
                .thenReturn(list);

        mockMvc.perform(get("/api/analytics/sellers-below")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("threshold", "200.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value(1))
                .andExpect(jsonPath("$[0].sellerName").value("Bob"))
                .andExpect(jsonPath("$[0].totalAmount").value(100.0))
                .andExpect(jsonPath("$[1].sellerId").value(2))
                .andExpect(jsonPath("$[1].sellerName").value("Charlie"))
                .andExpect(jsonPath("$[1].totalAmount").value(50.0));
    }

    @Test
    void getSellersBelow_negativeThreshold_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/analytics/sellers-below")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("threshold", "-10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSellersBelow_zeroThreshold_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/analytics/sellers-below")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("threshold", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSellersBelow_missingStartDate_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/analytics/sellers-below")
                        .param("endDate", "2025-01-31")
                        .param("threshold", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSellersBelow_invalidDateFormat_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/analytics/sellers-below")
                        .param("startDate", "01-01-2025")
                        .param("endDate", "2025-01-31")
                        .param("threshold", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSellersBelow_serviceThrowsRuntimeException_shouldReturn500() throws Exception {
        when(analyticsService.getSellersBelowAmount(any(), any(), any()))
                .thenThrow(new RuntimeException("Внутренняя ошибка"));

        mockMvc.perform(get("/api/analytics/sellers-below")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("threshold", "100"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.message").value("Внутренняя ошибка"));
    }

    @Test
    void getBestPeriod_shouldReturnBestPeriod() throws Exception {
        BestPeriodResponse response = new BestPeriodResponse(
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 11, 0),
                5
        );
        when(analyticsService.getBestPeriod(1L, "MONTH")).thenReturn(response);

        mockMvc.perform(get("/api/sellers/1/best-period")
                        .param("periodSize", "MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodStart").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.periodEnd").value("2025-01-01T11:00:00"))
                .andExpect(jsonPath("$.transactionCount").value(5));
    }

    @Test
    void getBestPeriod_defaultPeriodSize_shouldReturnBestPeriod() throws Exception {
        BestPeriodResponse response = new BestPeriodResponse(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 2, 1, 0, 0),
                10
        );
        when(analyticsService.getBestPeriod(2L, "MONTH")).thenReturn(response);

        mockMvc.perform(get("/api/sellers/2/best-period"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount").value(10));
    }

    @Test
    void getBestPeriod_sellerNotFound_shouldReturn404() throws Exception {
        when(analyticsService.getBestPeriod(99L, "MONTH"))
                .thenThrow(new ResourceNotFoundException("Продавец с id 99 не найден"));

        mockMvc.perform(get("/api/sellers/99/best-period"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.message").value("Продавец с id 99 не найден"));
    }

    @Test
    void getBestPeriod_invalidPeriodSize_shouldReturn400() throws Exception {
        when(analyticsService.getBestPeriod(1L, "YEAR"))
                .thenThrow(new IllegalArgumentException("Допустимые периоды: DAY, WEEK, MONTH"));

        mockMvc.perform(get("/api/sellers/1/best-period")
                        .param("periodSize", "YEAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некорректный аргумент"))
                .andExpect(jsonPath("$.message").value("Допустимые периоды: DAY, WEEK, MONTH"));
    }

    @Test
    void getBestPeriod_noTransactions_shouldReturn404() throws Exception {
        when(analyticsService.getBestPeriod(1L, "DAY"))
                .thenThrow(new ResourceNotFoundException("Нет транзакций у продавца"));

        mockMvc.perform(get("/api/sellers/1/best-period")
                        .param("periodSize", "DAY"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.message").value("Нет транзакций у продавца"));
    }
}
