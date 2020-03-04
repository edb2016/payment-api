package com.eb.revolut.payments.db.repository.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.repository.AccountRepository;
import com.eb.revolut.payments.db.repository.RetryStrategy;
import com.revolut.payments.rest.utls.TestUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.persistence.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountRepositoryImplTest {

    private static final long RETRY_STRATEGY_SLEEP_TIME = 1000;
    private static final int RETRY_STRATEGY_COUNT = 5;

    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;
    private static RetryStrategy retryStrategy;
    private static List<User> users;
    private static Account account;

    @BeforeAll
    static void beforeAll() throws IOException {
        entityManagerFactory = Persistence.createEntityManagerFactory("jpaPersistenceUnitTest");
        retryStrategy = new RetryStrategyImpl(RETRY_STRATEGY_COUNT, RETRY_STRATEGY_SLEEP_TIME);
        entityManager = entityManagerFactory.createEntityManager();
        users = TestUtils.Db.setupUsers(entityManagerFactory);
        account = Account.builder()
                .balance(BigDecimal.ZERO)
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .user(users.get(0))
                .build();
    }

    @BeforeEach
    void setUp() {
        TestUtils.Db.resetAccountsDB(entityManagerFactory);
    }

    @Test
    void testFindById() {
        AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, retryStrategy);
        Optional<Account> addAccount = accountRepository.add(account);
        assertTrue(addAccount.isPresent());

        int accountId = addAccount.get().getId();
        Optional<Account> result = accountRepository.findById(accountId);
        assertTrue(result.isPresent());
    }

    @Test
    void testFindByIdWithLock() {
        AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, retryStrategy);
        Optional<Account> addAccount = accountRepository.add(account);
        assertTrue(addAccount.isPresent());

        int accountId = addAccount.get().getId();
        Optional<Account> result = accountRepository.findByIdWithLock(accountId);
        assertTrue(result.isPresent());
    }

    @Test
    void testFindByIdNonExistingId() {
        AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, retryStrategy);
        Optional<Account> result = accountRepository.findById(300);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdEmptyOnException() {
        EntityManager em = Mockito.mock(EntityManager.class);
        EntityTransaction et = Mockito.mock(EntityTransaction.class);

        int accountId = 1;
        when(em.getTransaction()).thenReturn(et);
        doThrow(new IllegalArgumentException()).when(em).find(Account.class, accountId);

        AccountRepository accountRepository = new AccountRepositoryImpl(em, retryStrategy);
        Optional<Account> account = accountRepository.findById(accountId);

        assertFalse(account.isPresent());
    }

    @Test
    void testFindAll() {
        AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, retryStrategy);
        Optional<Account> addAccount = accountRepository.add(account);
        assertTrue(addAccount.isPresent());

        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts.size(), equalTo(1));
        assertThat(accounts.get(0), equalTo(addAccount.get()));
    }

    @Test
    void testFindAllEmptyOnException() {
        EntityManager em = Mockito.mock(EntityManager.class);
        EntityTransaction et = Mockito.mock(EntityTransaction.class);

        when(em.getTransaction()).thenReturn(et);
        doThrow(new IllegalArgumentException()).when(em).createQuery("from Account");

        AccountRepository accountRepository = new AccountRepositoryImpl(em, retryStrategy);
        List<Account> accounts = accountRepository.findAll();

        assertThat(accounts, empty());
    }

    @Test
    void testAddAccountEmptyOnException() {
        EntityManager em = Mockito.mock(EntityManager.class);
        EntityTransaction et = Mockito.mock(EntityTransaction.class);

        Account account = null;
        when(em.getTransaction()).thenReturn(et);
        doThrow(new IllegalArgumentException()).when(em).persist(any());

        AccountRepository accountRepository = new AccountRepositoryImpl(em, retryStrategy);
        Optional<Account> resultOpt = accountRepository.add(account);

        assertFalse(resultOpt.isPresent());
    }

    @Test
    void testFindAllEmpty() {
        AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, retryStrategy);
        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts, empty());
    }

    @Test
    void testTransfer() {
        EntityManager em = Mockito.mock(EntityManager.class);
        EntityTransaction et = Mockito.mock(EntityTransaction.class);

        Account accountA = getPayerAccount();
        Account accountB = getPayeeAccount();

        Transfer transfer = Transfer.builder()
                .payerAccountId(accountA.getId())
                .payeeAccountId(accountB.getId())
                .amount(new BigDecimal(10))
                .build();

        when(em.getTransaction()).thenReturn(et);

        when(em.find(Account.class, accountA.getId(),
                LockModeType.OPTIMISTIC_FORCE_INCREMENT)).thenReturn(accountA);

        when(em.find(Account.class, accountB.getId(),
                LockModeType.OPTIMISTIC_FORCE_INCREMENT)).thenReturn(accountB);

        AccountRepository accountRepository = new AccountRepositoryImpl(em, retryStrategy);
        Optional<Transfer> transferOptional = accountRepository.updateAccount(transfer);

        verify(et).begin();
        verify(em).merge(accountA);
        verify(em).merge(accountB);
        verify(et).commit();

        assertTrue(transferOptional.isPresent());
        assertThat(transferOptional.get(), equalTo(transfer));
    }

    @Test
    void testTransferAccountBalanceNotSuffcient() {
        EntityManager em = Mockito.mock(EntityManager.class);
        EntityTransaction et = Mockito.mock(EntityTransaction.class);

        Account accountA = getPayerAccount();
        Account accountB = getPayeeAccount();

        Transfer transfer = Transfer.builder()
                .payerAccountId(accountA.getId())
                .payeeAccountId(accountB.getId())
                .amount(new BigDecimal(1000))
                .build();

        when(em.getTransaction()).thenReturn(et);

        when(em.find(Account.class, accountA.getId(),
                LockModeType.OPTIMISTIC_FORCE_INCREMENT)).thenReturn(accountA);

        when(em.find(Account.class, accountB.getId(),
                LockModeType.OPTIMISTIC_FORCE_INCREMENT)).thenReturn(accountB);

        AccountRepository accountRepository = new AccountRepositoryImpl(em, retryStrategy);
        Optional<Transfer> transferOptional = accountRepository.updateAccount(transfer);

        verify(et).begin();
        verify(em, never()).merge(accountA);
        verify(em, never()).merge(accountB);
        verify(et, never()).commit();
        verify(et).rollback();

        assertFalse(transferOptional.isPresent());
    }

    @Test
    void testTransferThrowsIllegalArgumentException() {
        EntityManager em = Mockito.mock(EntityManager.class);
        EntityTransaction et = Mockito.mock(EntityTransaction.class);

        Account accountA = getPayerAccount();
        Account accountB = getPayeeAccount();

        Transfer transfer = Transfer.builder()
                .payerAccountId(accountA.getId())
                .payeeAccountId(accountB.getId())
                .amount(new BigDecimal(50))
                .build();

        when(em.getTransaction()).thenReturn(et);

        doThrow(new IllegalArgumentException()).when(em)
                .find(Account.class, accountA.getId(),LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        AccountRepository accountRepository = new AccountRepositoryImpl(em, retryStrategy);
        Optional<Transfer> transferOptional = accountRepository.updateAccount(transfer);

        assertFalse(transferOptional.isPresent());
    }

    @Test
    void testTransferRetryOnOptimisticLockException() {
        EntityManager em = Mockito.mock(EntityManager.class);
        EntityTransaction et = Mockito.mock(EntityTransaction.class);

        Account accountA = getPayerAccount();
        Account accountB = getPayeeAccount();

        Transfer transfer = Transfer.builder()
                .payerAccountId(accountA.getId())
                .payeeAccountId(accountB.getId())
                .amount(new BigDecimal(50))
                .build();

        when(em.getTransaction()).thenReturn(et);

        when(em.find(Account.class, accountA.getId(),
                LockModeType.OPTIMISTIC_FORCE_INCREMENT)).thenReturn(accountA);

        when(em.find(Account.class, accountB.getId(),
                LockModeType.OPTIMISTIC_FORCE_INCREMENT)).thenReturn(accountB);

        doThrow(new OptimisticLockException()).when(et).commit();

        AccountRepository accountRepository = new AccountRepositoryImpl(em, retryStrategy);
        Optional<Transfer> transferOptional = accountRepository.updateAccount(transfer);

        assertFalse(transferOptional.isPresent());

        verify(et, times(3)).begin();
        verify(em, times(2)).merge(accountA);
        verify(em, times(2)).merge(accountB);
        verify(et, times(2)).commit();
    }

    private Account getPayerAccount() {
        return Account.builder()
            .balance(new BigDecimal(100))
            .creationDate(new Timestamp(System.currentTimeMillis()))
            .user(users.get(1))
            .id(1)
            .build();
    }

    private Account getPayeeAccount() {
        return Account.builder()
            .balance(new BigDecimal(120))
            .creationDate(new Timestamp(System.currentTimeMillis()))
            .user(users.get(2))
            .id(2)
            .build();
    }

    @AfterAll
    public static void afterAll() {
        entityManager.close();
        entityManagerFactory.close();
    }
}