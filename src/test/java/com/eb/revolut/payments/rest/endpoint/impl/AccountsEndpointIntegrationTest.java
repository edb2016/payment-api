package com.eb.revolut.payments.rest.endpoint.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.Transaction;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.model.types.TransactionType;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.db.repository.factory.ResponseServiceFactory;
import com.eb.revolut.payments.properties.ApplicationProperties;
import com.eb.revolut.payments.properties.ApplicationPropertiesImpl;
import com.eb.revolut.payments.rest.context.RestContextService;
import com.eb.revolut.payments.rest.context.impl.RestContextServiceImpl;
import com.eb.revolut.payments.db.model.types.StandardResponse;
import com.eb.revolut.payments.db.model.types.StatusResponse;
import com.google.gson.Gson;
import com.revolut.payments.rest.utls.RequestResponseUtils;
import com.revolut.payments.rest.utls.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static spark.Service.ignite;

@Slf4j
class AccountsEndpointIntegrationTest {

    private static final String APPLICATION_PROPERTIES = "src/test/resources/application.properties";

    private static ApplicationProperties properties;
    private static EntityManagerFactory entityManagerFactory;
    private static List<User> users;
    private static Service service;


    @BeforeAll
    public static void beforeAll() throws IOException {
        service = ignite()
                .threadPool(4)
                .port(8080);

        properties = new ApplicationPropertiesImpl(APPLICATION_PROPERTIES);
        entityManagerFactory = Persistence.createEntityManagerFactory("jpaPersistenceUnitTest");

        RestContextService context = new RestContextServiceImpl();
        context.addEndpoint(new AccountsEndpoint(service, entityManagerFactory, new ResponseServiceFactory(), properties));
        users = TestUtils.Db.setupUsers(entityManagerFactory);
    }


    @Test
    public void testAddAccount() {
        final User user = users.get(1);
        String url = "http://localhost:8080/api/account/add/" + user.getId();

        StandardResponse response = RequestResponseUtils.requestJsonGet(url);

        String expectedMessage = "Account for user 2 has been successfully setup";

        assertThat(StatusResponse.SUCCESS, equalTo(response.getStatus()));
        assertThat(response.getMessage(), containsString(expectedMessage));
    }

    @Test
    public void testAddDuplicateAccount() {
        final User user = users.get(1);
        String url = "http://localhost:8080/api/account/add/" + user.getId();

        StandardResponse response = RequestResponseUtils.requestJsonGet(url);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));

        String expectedMessage = "Unable to create account for user: 2";

        StandardResponse duplicateResponse = RequestResponseUtils.requestJsonGet(url);
        assertThat(StatusResponse.ERROR, equalTo(duplicateResponse.getStatus()));
        assertThat(duplicateResponse.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void testGetAccountByAccountId() {
        List<Account> accounts = TestUtils.Db.setupMutipleAccountsFromUserList(entityManagerFactory, users);

        Account account = accounts.get(0);

        String url = "http://localhost:8080/api/account/" + account.getId();
        StandardResponse response = RequestResponseUtils.requestJsonGet(url);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
    }

    @Test
    public void testGetAllAccounts() {
        TestUtils.Db.setupMutipleAccountsFromUserList(entityManagerFactory, users);
        String url = "http://localhost:8080/api/accounts";

        StandardResponse response = RequestResponseUtils.requestJsonGet(url);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
    }

    @Test
    public void testAccountTransfer() {
        double INITIAL_PAYER_BALANCE = 150.00;
        double INITIAL_PAYEE_BALANCE = 10.00;

        //Setup accounts with balance
        Account payerAccount = TestUtils.createNewAccount(
                users.get(0), new BigDecimal(INITIAL_PAYER_BALANCE));

        Account payeeAccount = TestUtils.createNewAccount(
                users.get(1), new BigDecimal(INITIAL_PAYEE_BALANCE));

        TestUtils.Db.setupMutlipleAccounts(entityManagerFactory,
                Arrays.asList(payerAccount, payeeAccount));

        double TRANSFER_AMOUNT = 100.00;

        Transfer transfer = Transfer.builder()
                .payerAccountId(payerAccount.getId())
                .payeeAccountId(payeeAccount.getId())
                .amount(new BigDecimal(TRANSFER_AMOUNT))
                .build();

        String jsonTransfer = new Gson().toJson(transfer, Transfer.class);
        String url = "http://localhost:8080/api/account/transfer/";
        StandardResponse response = RequestResponseUtils.requestJsonPost(url, jsonTransfer);

        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));

        Optional<Account> actualPayerAccountOpt = TestUtils.Db
                .getAccount(entityManagerFactory, payerAccount.getId());

        Optional<Account> actualPayeeAccountOpt = TestUtils.Db
                .getAccount(entityManagerFactory, payeeAccount.getId());

        assertTrue(actualPayerAccountOpt.isPresent());
        assertTrue(actualPayeeAccountOpt.isPresent());

        Account actualPayerAccount = actualPayerAccountOpt.get();
        Account actualPayeeAccount = actualPayeeAccountOpt.get();

        double EXPECTED_PAYER_BALANCE = 50.00;
        double EXPECTED_PAYEE_BALANCE = 110.00;

        assertBalancesAreEqual(
                actualPayerAccount, new BigDecimal(EXPECTED_PAYER_BALANCE));
        assertBalancesAreEqual(
                actualPayeeAccount, new BigDecimal(EXPECTED_PAYEE_BALANCE));

        int noPayerTransactions = actualPayerAccount.getTransactions().size();
        int noPayeeTransactions = actualPayeeAccount.getTransactions().size();

        assertThat(noPayerTransactions, equalTo(1));
        assertThat(noPayeeTransactions, equalTo(1));

        Transaction debitTransaction = actualPayerAccount.getTransactions().get(0);
        Transaction creditTransaction = actualPayeeAccount.getTransactions().get(0);

        assertThat(debitTransaction.getTransactionType(),
                equalTo(TransactionType.DEBIT));
        assertBalancesAreEqual(debitTransaction.getAmount(),
                new BigDecimal(TRANSFER_AMOUNT));

        assertThat(creditTransaction.getTransactionType(),
                equalTo(TransactionType.CREDIT));
        assertBalancesAreEqual(creditTransaction.getAmount(),
                new BigDecimal(TRANSFER_AMOUNT));
    }

    @Test
    public void testConcurrentAccountUpdate() throws InterruptedException {
        double INITIAL_BALANCE = 150.00;
        double PAYEE_BALANCE = 5.00;

        List<Account> accounts = TestUtils.Db.setupMutipleAccountsFromUserListWithInitialBalance(
                entityManagerFactory,users, INITIAL_BALANCE);

        Account payerA = accounts.get(0);
        Account payerB = accounts.get(1);
        Account payerC = accounts.get(2);
        Account payerD = accounts.get(3);
        Account payee = accounts.get(4);
        payee.setBalance(new BigDecimal(PAYEE_BALANCE));

        double PAYER_A_TRANSFER_AMT = 10.00;
        Transfer transfer1 = Transfer.builder()
                .payerAccountId(payerA.getId())
                .payeeAccountId(payee.getId())
                .amount(new BigDecimal(PAYER_A_TRANSFER_AMT))
                .build();

        double PAYER_B_TRANSFER_AMT = 40.00;
        Transfer transfer2 = Transfer.builder()
                .payerAccountId(payerB.getId())
                .payeeAccountId(payee.getId())
                .amount(new BigDecimal(PAYER_B_TRANSFER_AMT))
                .build();

        double PAYER_C_TRANSFER_AMT = 90.00;
        Transfer transfer3 = Transfer.builder()
                .payerAccountId(payerC.getId())
                .payeeAccountId(payee.getId())
                .amount(new BigDecimal(PAYER_C_TRANSFER_AMT))
                .build();

        double PAYER_D_TRANSFER_AMT = 5.00;
        Transfer transfer4 = Transfer.builder()
                .payerAccountId(payerD.getId())
                .payeeAccountId(payee.getId())
                .amount(new BigDecimal(PAYER_D_TRANSFER_AMT))
                .build();

        String url = "http://localhost:8080/api/account/transfer/";

        Thread t1 = new Thread(new Runnable() {
            public void run() {
                String jsonTransfer = new Gson().toJson(transfer1, Transfer.class);
                StandardResponse response = RequestResponseUtils.requestJsonPost(url, jsonTransfer);
                assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
            }
        });

        Thread t2 = new Thread(new Runnable() {
            public void run() {
                String jsonTransfer = new Gson().toJson(transfer2, Transfer.class);
                StandardResponse response = RequestResponseUtils.requestJsonPost(url, jsonTransfer);
                assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
            }
        });

        Thread t3 = new Thread(new Runnable() {
            public void run() {
                String jsonTransfer = new Gson().toJson(transfer3, Transfer.class);
                StandardResponse response = RequestResponseUtils.requestJsonPost(url, jsonTransfer);
                assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
            }
        });

        Thread t4 = new Thread(new Runnable() {
            public void run() {
                String jsonTransfer = new Gson().toJson(transfer4, Transfer.class);
                StandardResponse response = RequestResponseUtils.requestJsonPost(url, jsonTransfer);
                assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
            }
        });

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        int EXPECTED_NO_TRANSACTIONS = 1;
        double EXPECTED_PAYER_A_BALANCE = 140.00;
        Optional<Account> actualPayerAOptional = TestUtils.Db.getAccount(entityManagerFactory, payerA.getId());
        assertAccountBalanceCorrectAfterTransaction(actualPayerAOptional, EXPECTED_PAYER_A_BALANCE, EXPECTED_NO_TRANSACTIONS);

        double EXPECTED_PAYER_B_BALANCE = 110.00;
        Optional<Account> actualPayerBOptional = TestUtils.Db.getAccount(entityManagerFactory, payerB.getId());
        assertAccountBalanceCorrectAfterTransaction(actualPayerBOptional, EXPECTED_PAYER_B_BALANCE, EXPECTED_NO_TRANSACTIONS);

        double EXPECTED_PAYER_C_BALANCE = 50.00;
        Optional<Account> actualPayerCOptional = TestUtils.Db.getAccount(entityManagerFactory, payerC.getId());
        assertAccountBalanceCorrectAfterTransaction(actualPayerCOptional, EXPECTED_PAYER_C_BALANCE, EXPECTED_NO_TRANSACTIONS);

        double EXPECTED_PAYER_D_BALANCE = 145.00;
        Optional<Account> actualPayerDOptional = TestUtils.Db.getAccount(entityManagerFactory, payerD.getId());
        assertAccountBalanceCorrectAfterTransaction(actualPayerDOptional, EXPECTED_PAYER_D_BALANCE, EXPECTED_NO_TRANSACTIONS);

        int EXPECTED_NO_PAYEE_TRANSACTIONS = 4;
        double EXPECTED_PAYEE_BALANCE = 120.00;
        Optional<Account> actualPayeeOptional = TestUtils.Db.getAccount(entityManagerFactory, payee.getId());
        assertAccountBalanceCorrectAfterTransaction(actualPayerDOptional, EXPECTED_PAYEE_BALANCE, EXPECTED_NO_PAYEE_TRANSACTIONS);
    }

    private void assertAccountBalanceCorrectAfterTransaction(
            Optional<Account> accountOptional, double expectedBalance, int expectedNoTransactions) {
        assertTrue(accountOptional.isPresent());
        Account actualAccount = accountOptional.get();
        assertBalancesAreEqual(actualAccount, new BigDecimal(expectedBalance));
        assertThat(actualAccount.getTransactions().size(), equalTo(1));
    }

    private boolean assertBalancesAreEqual(Account account, BigDecimal balance2) {
        return account.getBalance().compareTo(balance2) == 0;
    }

    private boolean assertBalancesAreEqual(BigDecimal balance1, BigDecimal balance2) {
        return balance1.compareTo(balance2) == 0;
    }

    @AfterEach
    public void afterEach() {
        TestUtils.Db.resetAccountsDB(entityManagerFactory);
    }

    @AfterAll
    public static void afterAll() {
        service.stop();
        entityManagerFactory.close();
    }

}