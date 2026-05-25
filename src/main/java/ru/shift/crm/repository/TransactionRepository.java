package ru.shift.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.shift.crm.entity.Transaction;
import ru.shift.crm.repository.projection.SellerBelowProjection;
import ru.shift.crm.repository.projection.TopSellerProjection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySellerId(Long sellerId);

    @Query("""
        SELECT s.id AS sellerId, s.name AS sellerName, SUM(t.amount) AS totalAmount
        FROM Transaction t
        JOIN t.seller s
        WHERE t.transactionDate >= :start AND t.transactionDate < :end
        GROUP BY s.id, s.name
        ORDER BY totalAmount DESC
        """)
    List<TopSellerProjection> findTopSellerByPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
        SELECT s.id AS sellerId, s.name AS sellerName, COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM Seller s
        LEFT JOIN s.transactions t
            ON t.transactionDate >= :start AND t.transactionDate < :end
        GROUP BY s.id, s.name
        HAVING COALESCE(SUM(t.amount), 0) < :threshold
        """)
    List<SellerBelowProjection> findSellersBelowAmount(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("threshold") BigDecimal threshold);
}