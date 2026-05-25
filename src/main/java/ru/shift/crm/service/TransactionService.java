package ru.shift.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shift.crm.dto.TransactionRequest;
import ru.shift.crm.dto.TransactionResponse;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.entity.Transaction;
import ru.shift.crm.exception.ResourceNotFoundException;
import ru.shift.crm.repository.SellerRepository;
import ru.shift.crm.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    public TransactionResponse save(Transaction transaction) {
        Transaction repositoryResponse = transactionRepository.save(transaction);
        return TransactionResponse.from(repositoryResponse);
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Seller seller = sellerRepository.findById(request.sellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + request.sellerId()));

        Transaction createdTransaction = request.toEntity(seller);
        Transaction repositoryResponse = transactionRepository.save(createdTransaction);
        return TransactionResponse.from(repositoryResponse);
    }

    public List<TransactionResponse> getTransactionsBySellerId(Long sellerId) {
        return transactionRepository.findBySellerId(sellerId).stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
