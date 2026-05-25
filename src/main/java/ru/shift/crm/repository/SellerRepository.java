package ru.shift.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.shift.crm.entity.Seller;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
}
