package com.eb.revolut.payments.service;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.Transaction;

import java.math.BigDecimal;

public interface TransactionServices {

    Transaction createDebitTransaction(Account account, BigDecimal amount, String comment);

    Transaction createCreditTransaction(Account account, BigDecimal amount, String comment);



}
