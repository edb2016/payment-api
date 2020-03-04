package com.eb.revolut.payments.service.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.model.types.TransactionType;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.service.AccountServices;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@AllArgsConstructor
public class AccountServicesImpl implements AccountServices {

    @Override
    public Account updateAccountBalance(Account account, BigDecimal amount, TransactionType type) {
        if (type.equals(TransactionType.CREDIT)) {
            BigDecimal currentAccountBalance = account.getBalance();
            BigDecimal newAccountBalance = currentAccountBalance.add(amount);
            account.setBalance(newAccountBalance);
            return account;
        } else {
            BigDecimal currentAccountBalance = account.getBalance();
            BigDecimal newAccountBalance = currentAccountBalance.subtract(amount);
            account.setBalance(newAccountBalance);
            return account;
        }
    }

    @Override
    public Account createNewAccount(User user) {
        return Account.builder()
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Override
    public boolean accountBalanceSuffcient(Account account, Transfer transfer) {
        return Objects.nonNull(account) && account.getBalance().compareTo(transfer.getAmount()) >= 0;
    }

}
