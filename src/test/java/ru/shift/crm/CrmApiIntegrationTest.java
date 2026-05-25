package ru.shift.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.shift.crm.dto.SellerCreateRequest;
import ru.shift.crm.dto.SellerUpdateRequest;
import ru.shift.crm.dto.TransactionRequest;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.entity.Transaction;
import ru.shift.crm.repository.SellerRepository;
import ru.shift.crm.repository.TransactionRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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
                new Seller(null, "Bob", "bob@mail.com", java.time.LocalDateTime.now(), null));

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
                new Seller(null, "Old", "old@mail.com", java.time.LocalDateTime.now(), null));

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
                new Seller(null, "DeleteMe", "del@mail.com", java.time.LocalDateTime.now(), null));

        mockMvc.perform(delete("/api/sellers/{id}", seller.getId()))
                .andExpect(status().isNoContent());

        assertThat(sellerRepository.findById(seller.getId())).isEmpty();
    }

    @Test
    void deleteSeller_NotFound_ShouldStillReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/sellers/999"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSellerTransactions_Empty() throws Exception {
        Seller seller = sellerRepository.save(
                new Seller(null, "Seller", "s@mail.com", java.time.LocalDateTime.now(), null));

        mockMvc.perform(get("/api/sellers/{id}/transactions", seller.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getSellerTransactions_WithData() throws Exception {
        Seller seller = sellerRepository.save(
                new Seller(null, "Seller", "s@mail.com", java.time.LocalDateTime.now(), null));
        Transaction tx = transactionRepository.save(
                new Transaction(null, seller, BigDecimal.valueOf(50), "CASH", java.time.LocalDateTime.now()));

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
                new Seller(null, "Seller", "s@mail.com", java.time.LocalDateTime.now(), null));

        TransactionRequest txReq = new TransactionRequest(seller.getId(),
                BigDecimal.valueOf(99.99), "CARD", null);

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
        TransactionRequest txReq = new TransactionRequest(999L, BigDecimal.ONE, "CASH", null);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createTransaction_ValidationError_NegativeAmount() throws Exception {
        TransactionRequest txReq =
                new TransactionRequest(1L, BigDecimal.valueOf(-10),
                        "CASH", null);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionById_Found() throws Exception {
        Seller seller = sellerRepository.save(
                new Seller(null, "Seller", "s@mail.com",
                        java.time.LocalDateTime.now(), null));
        Transaction tx = transactionRepository.save(
                new Transaction(null, seller, BigDecimal.valueOf(30),
                        "TRANSFER", java.time.LocalDateTime.now()));

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
}