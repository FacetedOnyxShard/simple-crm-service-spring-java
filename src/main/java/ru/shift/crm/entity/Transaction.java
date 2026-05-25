package ru.shift.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Transaction {
//    ID (id): уникальный идентификатор транзакции (целое число, автоинкремент).
//    Продавец (seller): ссылка на продавца, к которому относится транзакция (внешний
//    ключ на сущность "Продавец").
//    Сумма (amount): сумма транзакции (десятичное число).
//    Тип оплаты (paymentType): тип оплаты (CASH, CARD, TRANSFER) (строка).
//    Дата транзакции (transactionDate): дата и время совершения транзакции (тип LocalDateTime).

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
    @SequenceGenerator(
            name = "transaction_seq",
            sequenceName = "transaction_id_seq",
            allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_seller"))
    @ToString.Exclude
    @JsonIgnore
    private Seller seller;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentType paymentType;

    @Column
    @Builder.Default
    private LocalDateTime transactionDate = LocalDateTime.now();
}
