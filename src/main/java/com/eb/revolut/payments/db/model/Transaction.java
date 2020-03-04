package com.eb.revolut.payments.db.model;

import com.eb.revolut.payments.db.model.types.TransactionType;
import com.google.gson.annotations.Expose;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Expose
    private Timestamp creationDate;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    @Expose
    private TransactionType transactionType;

    @Expose
    private BigDecimal amount;

    @Expose
    private String comment;

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", comment='" + comment + '\'' +
                '}';
    }
}
