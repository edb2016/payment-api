package com.eb.revolut.payments.rest.response.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.model.types.Constants;
import com.eb.revolut.payments.db.repository.impl.AccountRepositoryImpl;
import com.eb.revolut.payments.db.repository.impl.UserRepositoryImpl;
import com.eb.revolut.payments.rest.response.AccountResponseService;
import com.eb.revolut.payments.db.model.types.StandardResponse;
import com.eb.revolut.payments.db.model.types.StatusResponse;
import com.eb.revolut.payments.service.AccountServices;
import com.google.gson.Gson;
import com.revolut.payments.rest.utls.TestUtils;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountResponseServiceImplTest {

    @Mock
    private UserRepositoryImpl userRepoService;
    @Mock
    private AccountRepositoryImpl accountRepository;
    @Mock
    private AccountServices accountServices;

    private Transfer transfer;

    private AccountResponseService accountResponseService;

    private Gson gson;

    private Optional<User> user1Optional;

    private Optional<User> user2Optional;

    private Account account1;

    private Account account2;

    @BeforeEach
    void beforeEach() {
        gson = new Gson();
        accountResponseService = new AccountResponseServiceImpl(userRepoService, accountRepository, accountServices);
        user1Optional = Optional.of(User.builder()
                .firstName("firstName1")
                .lastName("lastName1")
                .username("username1")
                .email("email1")
                .id(1)
                .build());
        user2Optional = Optional.of(User.builder()
                .firstName("firstName2")
                .lastName("lastName2")
                .username("username2")
                .email("email2")
                .id(2)
                .build());
        account1 = Account.builder()
                .balance(new BigDecimal(100))
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .user(user1Optional.get())
                .id(1)
                .build();
        account2 = Account.builder()
                .balance(BigDecimal.ZERO)
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .user(user2Optional.get())
                .id(2)
                .build();
        transfer = Transfer.builder()
                .payerAccountId(1)
                .payeeAccountId(2)
                .payerComment("String")
                .amount(new BigDecimal(100))
                .build();

        gson = new Gson();
    }

    @Test
    void testAddAccount() {
        int userId = 1;

        when(userRepoService.findById(userId)).thenReturn(user1Optional);
        when(accountServices.createNewAccount(user1Optional.get())).thenReturn(account1);
        when(accountRepository.add(account1)).thenReturn(Optional.of(account1));

        String jsonResult = accountResponseService.addAccount(userId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "Account for user 1 has been successfully setup, accoundId: 1";

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testAddAccountUserCannotBeFound() {
        int userId = 1;

        when(userRepoService.findById(userId)).thenReturn(Optional.empty());

        String jsonResult = accountResponseService.addAccount(userId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "User with id 1 does not exist";

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.ERROR));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testAccountCouldNotBeCreated() {
        int userId = 1;

        when(userRepoService.findById(userId)).thenReturn(user1Optional);
        when(accountServices.createNewAccount(user1Optional.get())).thenReturn(account1);
        when(accountRepository.add(account1)).thenReturn(Optional.empty());

        String jsonResult = accountResponseService.addAccount(userId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "Unable to create account for user: 1";

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.ERROR));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testGetAccount() {
        int accountId = 1;
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account1));

        String jsonResult = accountResponseService.getAccount(accountId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        assertNotNull(response);
        assertThat(StatusResponse.SUCCESS, equalTo(response.getStatus()));

        Account result = TestUtils.buildAccountFromJson(response.getMessage());
        assertThat(1, equalTo(result.getId()));
        assertTrue(result.getBalance().compareTo(new BigDecimal(100))==0);
    }

    @Test
    void testGetAccountNotFound() {
        int accountId = 1;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        String jsonResult = accountResponseService.getAccount(accountId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "Unable to find account with id: 1";

        assertNotNull(response);
        assertThat(StatusResponse.ERROR, equalTo(response.getStatus()));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testAccountTransfer() {
        int payerAccountId = 1;
        int payeeAccountId = 2;

        when(accountRepository.findByIdWithLock(payerAccountId)).thenReturn(Optional.of(account1));
        when(accountRepository.findByIdWithLock(payeeAccountId)).thenReturn(Optional.of(account2));
        when(accountRepository.updateAccount(transfer)).thenReturn(Optional.of(transfer));

        String jsonResult = accountResponseService.accountTransfer(transfer);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));

        Transfer transferObj = gson.fromJson(response.getMessage(), Transfer.class);
        assertThat(transferObj, equalTo(transfer));
    }

    @Test
    void testAccountTransferFails() {
        int payerAccountId = 1;

        when(accountRepository.findByIdWithLock(payerAccountId)).thenReturn(Optional.empty());

        String jsonResult = accountResponseService.accountTransfer(transfer);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        assertThat(response.getStatus(), equalTo(StatusResponse.ERROR));
        assertThat(response.getMessage(), equalTo(Constants.Errors.ACCOUNT_TRANSFER_ERROR));
    }


}