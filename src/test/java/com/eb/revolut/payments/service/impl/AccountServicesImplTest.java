package com.eb.revolut.payments.service.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.model.types.TransactionType;
import com.eb.revolut.payments.db.model.types.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountServicesImplTest {

    private Account accountA;

    private Account accountB;

    @Mock
    private User userA;
    @Mock
    private User userB;

    @BeforeEach
    void beforeEach() {
        accountA = Account.builder()
                .balance(new BigDecimal(100))
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .user(userA)
                .id(1)
                .build();
        accountB = Account.builder()
                .balance(new BigDecimal(100))
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .user(userB)
                .id(2)
                .build();
    }

    @Test
    void testUpdateAccountBalanceCredit() {
        AccountServicesImpl accountServices = new AccountServicesImpl();
        Account actual = accountServices.updateAccountBalance(accountA, new BigDecimal(20.00), TransactionType.CREDIT);

        BigDecimal expectedBalance = new BigDecimal(120);
        assertNotNull(actual);
        assertThat(actual.getBalance().compareTo(expectedBalance), equalTo(0));
    }

    @Test
    void testUpdateAccountBalanceDebit() {
        AccountServicesImpl accountServices = new AccountServicesImpl();
        Account actual = accountServices.updateAccountBalance(accountA, new BigDecimal(20.00), TransactionType.DEBIT);

        BigDecimal expectedBalance = new BigDecimal(80);
        assertNotNull(actual);
        assertThat(actual.getBalance().compareTo(expectedBalance), equalTo(0));
    }

    @Test
    void testAccountBalanceSuffcient() {
        AccountServicesImpl accountServices = new AccountServicesImpl();

        Transfer transfer = Transfer.builder()
                .amount(new BigDecimal(20))
                .build();

        assertTrue(accountServices.accountBalanceSuffcient(accountA, transfer));
    }

    @Test
    void testAccountBalanceNotSuffcient() {
        AccountServicesImpl accountServices = new AccountServicesImpl();

        Transfer transfer = Transfer.builder()
                .amount(new BigDecimal(100.50))
                .build();

        assertFalse(accountServices.accountBalanceSuffcient(accountA, transfer));
    }

    @Test
    void testCreateAccount() {
        AccountServicesImpl accountServices = new AccountServicesImpl();
        assertNotNull(accountServices.createNewAccount(userA));
    }

}