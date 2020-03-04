package com.eb.revolut.payments.service.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.Transaction;
import com.eb.revolut.payments.db.model.types.TransactionType;
import com.eb.revolut.payments.service.TransactionServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionServicesImplTest {

    private static final String COMMENT = "COMMENT";

    @Mock
    private Account account;
    @Mock
    private BigDecimal amount;

    @Test
    void testCreateCreditTransaction() {
        TransactionServices transactionServices = new TransactionServicesImpl();
        Transaction transaction = transactionServices.createCreditTransaction(account, amount, COMMENT);
        assertNotNull(transaction);
        assertNotNull(transaction.getAccount());
        assertNotNull(transaction.getAmount());
        assertThat(transaction.getComment(), equalTo(COMMENT));
        assertThat(transaction.getTransactionType(), equalTo(TransactionType.CREDIT));
    }

    @Test
    void testCreateDebitTransaction() {
        TransactionServices transactionServices = new TransactionServicesImpl();
        Transaction transaction = transactionServices.createDebitTransaction(account, amount, COMMENT);
        assertNotNull(transaction);
        assertNotNull(transaction.getAccount());
        assertNotNull(transaction.getAmount());
        assertThat(transaction.getComment(), equalTo(COMMENT));
        assertThat(transaction.getTransactionType(), equalTo(TransactionType.DEBIT));
    }

}