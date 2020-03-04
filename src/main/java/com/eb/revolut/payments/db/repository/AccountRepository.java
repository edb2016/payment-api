package com.eb.revolut.payments.db.repository;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.types.Transfer;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findById(int id);

    Optional<Account> findByIdWithLock(int id);

    List<Account> findAll();

    Optional<Transfer> updateAccount(Transfer transfer);

    Optional<Account> add(Account account);
}
