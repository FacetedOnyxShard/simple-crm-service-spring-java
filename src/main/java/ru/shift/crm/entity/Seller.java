package ru.shift.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Seller {
//    ID (id): уникальный идентификатор продавца (целое число, автоинкремент).
//    Имя (name): имя продавца (строка).
//    Контактные данные (contactInfo): контактная информация продавца (строка).
//    Дата регистрации (registrationDate): дата и время регистрации продавца в системе
//            (тип LocalDateTime).

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seller_seq")
    @SequenceGenerator(
            name = "seller_seq",
            sequenceName = "seller_id_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactInfo;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime registrationDate = LocalDateTime.now();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @JsonIgnore
    private List<Transaction> transactions = new ArrayList<>();
}
