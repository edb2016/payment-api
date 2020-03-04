package com.eb.revolut.payments.service.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.Transaction;
import com.eb.revolut.payments.db.model.types.TransactionType;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.service.TransactionServices;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class TransactionServicesImpl implements TransactionServices {


    @Override
    public Transaction createDebitTransaction(Account account, BigDecimal amount, String comment) {
        Transaction transaction = createNewTransaction(account, amount, comment);
        transaction.setTransactionType(TransactionType.DEBIT);
        return transaction;
    }

    @Override
    public Transaction createCreditTransaction(Account account, BigDecimal amount, String comment) {
        Transaction transaction = createNewTransaction(account, amount, comment);
        transaction.setTransactionType(TransactionType.CREDIT);
        return transaction;
    }

    private Transaction createNewTransaction(Account account, BigDecimal amount, String comment) {
        return Transaction.builder()
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .amount(amount)
                .account(account)
                .comment(comment)
                .build();
    }
}
