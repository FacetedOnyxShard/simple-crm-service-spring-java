package ru.shift.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.shift.crm.dto.SellerCreateResponse;
import ru.shift.crm.dto.SellerUpdateRequest;
import ru.shift.crm.dto.SellerUpdateResponse;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.repository.SellerRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;

    public List<Seller> findAll() {
        return sellerRepository.findAll();
    }

    public Optional<Seller> findById(Long id) {
        return sellerRepository.findById(id);
    }

    public SellerCreateResponse save(Seller seller) {
        Seller repositoryResponse = sellerRepository.save(seller);
        return SellerCreateResponse.from(repositoryResponse);
    }

    public SellerUpdateResponse update(Long id, Seller newSellerData) {
        Seller newSeller = findById(id)
                .orElseThrow(() -> new RuntimeException("Seller not found with id: " + id));

        newSeller.setName(newSellerData.getName());
        newSeller.setContactInfo(newSellerData.getContactInfo());

        return SellerUpdateResponse.from(sellerRepository.save(newSeller));
    }

    public void delete(Long id) {
        sellerRepository.deleteById(id);
    }
}
