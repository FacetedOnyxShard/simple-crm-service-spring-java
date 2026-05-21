package ru.shift.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
