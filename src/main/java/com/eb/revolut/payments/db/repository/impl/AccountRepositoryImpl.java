package com.eb.revolut.payments.db.repository.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.Transaction;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.db.model.types.TransactionType;
import com.eb.revolut.payments.db.repository.AccountRepository;
import com.eb.revolut.payments.db.repository.RetryStrategy;
import com.eb.revolut.payments.service.AccountServices;
import com.eb.revolut.payments.service.TransactionServices;
import com.eb.revolut.payments.service.impl.AccountServicesImpl;
import com.eb.revolut.payments.service.impl.TransactionServicesImpl;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class AccountRepositoryImpl implements AccountRepository {

    private final EntityManager entityManager;
    private final AccountServices accountServices;
    private final TransactionServices transactionServices;
    private final RetryStrategy retryStrategy;

    public AccountRepositoryImpl(EntityManager entityManager, RetryStrategy retryStrategy) {
        this.entityManager = entityManager;
        this.retryStrategy = retryStrategy;
        this.accountServices = new AccountServicesImpl();
        this.transactionServices = new TransactionServicesImpl();
    }

    @Override
    public Optional<Account> findById(int id) {
        Account account = null;
        try {
            entityManager.getTransaction().begin();
            account = entityManager.find(Account.class, id);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error error loading account with Id: %s",id), e);
        }
        return Objects.nonNull(account) ? Optional.of(account) : Optional.empty();
    }

    @Override
    public Optional<Account> findByIdWithLock(int id) {
        Account account = null;
        try {
            entityManager.getTransaction().begin();
            account = entityManager.find(Account.class, id, LockModeType.OPTIMISTIC);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error error loading account with Id: %s",id), e);
        }
        return Objects.nonNull(account) ? Optional.of(account) : Optional.empty();
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts = null;
        try {
            entityManager.getTransaction().begin();
            accounts = entityManager.createQuery("from Account").getResultList();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error calling findAll() "), e);
        }
        return Objects.isNull(accounts) ? Collections.emptyList() : accounts;
    }

    @Override
    public Optional<Transfer> updateAccount(Transfer transfer) {

        try {
            return getCommitTransfer(transfer);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Unable to complete transfer: %s", transfer), e);
        }

        return Optional.empty();
    }

    private Optional<Transfer> getCommitTransfer(Transfer transfer){
        do {
            try {
                entityManager.getTransaction().begin();
                Account payerAccount = findByIdToWrite(transfer.getPayerAccountId());

                Account payeeAccount = findByIdToWrite(transfer.getPayeeAccountId());

                if (accountServices.accountBalanceSuffcient(payerAccount, transfer)) {

                    payerAccount = accountServices.updateAccountBalance
                            (payerAccount, transfer.getAmount(), TransactionType.DEBIT);

                    Transaction debitTransaction = transactionServices.createDebitTransaction(
                            payerAccount, transfer.getAmount(), transfer.getPayerComment());

                    payerAccount.addTransaction(debitTransaction);

                    payeeAccount = accountServices.updateAccountBalance
                            (payeeAccount, transfer.getAmount(), TransactionType.CREDIT);

                    Transaction creditTransaction = transactionServices.createCreditTransaction(
                            payeeAccount, transfer.getAmount(), transfer.getPayerComment());

                    payeeAccount.addTransaction(creditTransaction);

                    entityManager.merge(payerAccount);
                    entityManager.merge(payeeAccount);

                    entityManager.getTransaction().commit();
                    return Optional.of(transfer);
                } else {
                    entityManager.getTransaction().rollback();
                    break;
                }
            } catch (OptimisticLockException | RollbackException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                log.warn("Retrying transfer transaction, optimistic lock exception detected ..");
            }

            retryStrategy.waitAndRetry();
        } while (retryStrategy.getRetryCount() > 0);

        log.error("Unable to perform transfer");
        return Optional.empty();
    }

    @Override
    public Optional<Account> add(Account account) {
        try {
            if (account != null) {
                entityManager.getTransaction().begin();
                entityManager.persist(account);
                entityManager.getTransaction().commit();
            }
            return Optional.of(account);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error adding account: %s",account), e);
        }
        return Optional.empty();
    }

    /*
     * Should always be called within a transaction.
     */
    private Account findByIdToWrite(int id) {
        return entityManager.find(Account.class, id, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }
}
