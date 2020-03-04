package com.eb.revolut.payments.service;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.model.types.TransactionType;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountServices {

    Account createNewAccount(User user);

    Account updateAccountBalance(Account account, BigDecimal amount, TransactionType type);

    boolean accountBalanceSuffcient(Account account, Transfer transfer);

}
