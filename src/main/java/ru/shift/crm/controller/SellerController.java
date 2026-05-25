package ru.shift.crm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shift.crm.dto.*;
import ru.shift.crm.dto.analytics.BestPeriodResponse;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.service.AnalyticsService;
import ru.shift.crm.service.SellerService;
import ru.shift.crm.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;
    private final TransactionService transactionService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public List<Seller> getAllSellers() {
        return sellerService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) {
        return sellerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SellerCreateResponse createSeller(@RequestBody @Valid SellerCreateRequest request) {
        return sellerService.save(request.toEntity());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SellerUpdateResponse> updateSeller(@PathVariable Long id, @RequestBody @Valid SellerUpdateRequest request) {
        try {
            SellerUpdateResponse updatedSeller = sellerService.update(id, request.toEntity());
            return ResponseEntity.ok(updatedSeller);
        } catch (RuntimeException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) {
        sellerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getSellerTransactions(@PathVariable Long id) {
        return transactionService.getTransactionsBySellerId(id);
    }

    @GetMapping("/{id}/best-period")
    public ResponseEntity<BestPeriodResponse> getBestPeriod(
            @PathVariable Long id,
            @RequestParam(defaultValue = "MONTH") String periodSize) {
        return ResponseEntity.ok(analyticsService.getBestPeriod(id, periodSize));
    }
}