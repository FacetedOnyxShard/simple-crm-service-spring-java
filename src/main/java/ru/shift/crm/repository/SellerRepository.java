package ru.shift.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.shift.crm.entity.Seller;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
}
