package com.eb.revolut.payments.db.model.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionType {

    CREDIT ("Credit"),
    DEBIT ("Debit");

    private String description;

}
