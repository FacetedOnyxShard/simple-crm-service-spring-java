package ru.shift.crm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.shift.crm.dto.SellerCreateResponse;
import ru.shift.crm.dto.SellerUpdateResponse;
import ru.shift.crm.entity.Seller;
import ru.shift.crm.exception.ResourceNotFoundException;
import ru.shift.crm.repository.SellerRepository;
import ru.shift.crm.service.SellerService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerService sellerService;

    @Test
    void findAll_ShouldReturnList() {
        String sellerName = "Hikaruvi";
        String sellerContactInfo = "hikaruvi@gmail.com";

        Seller seller = Seller.builder().name(sellerName).contactInfo(sellerContactInfo).build();
        when(sellerRepository.findAll()).thenReturn(List.of(seller));

        List<Seller> result = sellerService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo(sellerName);
        assertThat(result.getFirst().getContactInfo()).isEqualTo(sellerContactInfo);
    }

    @Test
    void findById_ExistingId_ShouldReturnSeller() {
        Seller seller = new Seller(1L, "John", "john@mail.com", LocalDateTime.now(), null);
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));

        Optional<Seller> result = sellerService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void findById_NonExistingId_ShouldReturnEmpty() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Seller> result = sellerService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void save_ValidSeller_ShouldReturnResponse() {
        Seller seller = Seller.builder().name("Alice").contactInfo("alice@mail.com").build();
        Seller saved = new Seller(1L, "Alice", "alice@mail.com", LocalDateTime.now(), null);
        when(sellerRepository.save(seller)).thenReturn(saved);

        SellerCreateResponse response = sellerService.save(seller);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.registrationDate()).isNotNull();
        verify(sellerRepository).save(seller);
    }

    @Test
    void update_ExistingSeller_ShouldUpdateAndReturnResponse() {
        Long id = 1L;
        Seller existing = new Seller(id, "Old", "old@mail.com", LocalDateTime.now().minusDays(1), null);
        Seller newData = Seller.builder().name("New").contactInfo("new@mail.com").build();
        Seller updated = new Seller(id, "New", "new@mail.com", existing.getRegistrationDate(), null);

        when(sellerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sellerRepository.save(any(Seller.class))).thenReturn(updated);

        SellerUpdateResponse response = sellerService.update(id, newData);

        assertThat(response.name()).isEqualTo("New");
        assertThat(response.contactInfo()).isEqualTo("new@mail.com");
        assertThat(response.registrationDate()).isEqualTo(existing.getRegistrationDate());
        verify(sellerRepository).findById(id);
        verify(sellerRepository).save(existing);
    }

    @Test
    void update_NonExistingSeller_ShouldThrowException() {
        Long id = 99L;
        when(sellerRepository.findById(id)).thenReturn(Optional.empty());

        Seller newData = Seller.builder().name("X").contactInfo("x@mail.com").build();

        assertThatThrownBy(() -> sellerService.update(id, newData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Seller not found");
    }

    @Test
    void delete_ShouldCallRepositoryWhenExists() {
        Long id = 1L;
        when(sellerRepository.existsById(id)).thenReturn(true);
        doNothing().when(sellerRepository).deleteById(id);

        sellerService.delete(id);

        verify(sellerRepository).existsById(id);
        verify(sellerRepository).deleteById(id);
    }

    @Test
    void delete_ShouldNotFailWhenNotExists() {
        Long id = 99L;
        when(sellerRepository.existsById(id)).thenReturn(false);

        sellerService.delete(id);

        verify(sellerRepository).existsById(id);
        verify(sellerRepository, never()).deleteById(any());
    }
}