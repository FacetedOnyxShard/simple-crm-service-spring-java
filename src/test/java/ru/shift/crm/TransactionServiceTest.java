package ru.shift.crm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.shift.crm.dto.TransactionRequest;
import ru.shift.crm.dto.TransactionResponse;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.entity.Transaction;
import ru.shift.crm.repository.SellerRepository;
import ru.shift.crm.repository.TransactionRepository;
import ru.shift.crm.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private TransactionService transactionService;

    private final Seller seller = new Seller(1L, "Test Seller", "test@mail.com", LocalDateTime.now(), null);
    private final Transaction transaction = new Transaction(10L, seller, BigDecimal.valueOf(100),
            "CASH", LocalDateTime.now());

    @Test
    void findAll_ShouldReturnTransactionList() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        List<Transaction> result = transactionService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_ExistingId_ShouldReturnTransaction() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));

        Optional<Transaction> result = transactionService.findById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("100");
    }

    @Test
    void findById_NonExistingId_ShouldReturnEmpty() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void createTransaction_ValidRequest_ShouldSaveAndReturnResponse() {
        TransactionRequest request = new TransactionRequest(1L, BigDecimal.valueOf(200), "CARD", null);
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        Transaction saved = new Transaction(20L, seller, BigDecimal.valueOf(200), "CARD", LocalDateTime.now());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.amount()).isEqualByComparingTo("200");
        assertThat(response.paymentType()).isEqualTo("CARD");
        verify(sellerRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_SellerNotFound_ShouldThrowException() {
        TransactionRequest request = new TransactionRequest(99L, BigDecimal.TEN, "CASH", null);
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Seller not found");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionsBySellerId_ShouldReturnList() {
        when(transactionRepository.findBySellerId(1L)).thenReturn(List.of(transaction));

        List<TransactionResponse> result = transactionService.getTransactionsBySellerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().sellerId()).isEqualTo(1L);
    }
}