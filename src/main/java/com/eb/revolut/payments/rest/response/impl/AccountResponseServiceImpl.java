package com.eb.revolut.payments.rest.response.impl;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.model.types.Constants;
import com.eb.revolut.payments.db.repository.AccountRepository;
import com.eb.revolut.payments.db.repository.impl.UserRepositoryImpl;
import com.eb.revolut.payments.rest.response.AccountResponseService;
import com.eb.revolut.payments.utils.ResponseUtils;
import com.eb.revolut.payments.db.model.types.StatusResponse;
import com.eb.revolut.payments.service.AccountServices;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class AccountResponseServiceImpl implements AccountResponseService {

    private final UserRepositoryImpl userRepoService;
    private final AccountRepository accountRepository;
    private final AccountServices accountServices;

    @Override
    public String addAccount(int userId) {
        Optional<User> userOpt = userRepoService.findById(userId);

        if (userOpt.isPresent()) {
            Account account = accountServices.createNewAccount(userOpt.get());
            Optional<Account> accountOpt = accountRepository.add(account);

            if (accountOpt.isPresent()) {
                return ResponseUtils.getBasicResponseAsGsonString(
                        StatusResponse.SUCCESS,
                        String.format(Constants.Success.ACCOUNT_CREATED_SUCCESSFULLY,
                                userId,
                                accountOpt.get().getId()));
            } else {
                return ResponseUtils.getBasicResponseAsGsonString(
                        StatusResponse.ERROR,
                        String.format(Constants.Errors.ACCOUNT_NOT_CREATED,
                                userId));
            }
        }

        return ResponseUtils.getBasicResponseAsGsonString(
                StatusResponse.ERROR,
                String.format(Constants.Errors.USER_DOES_NOT_EXIST,
                        userId));
    }

    @Override
    public String getAccount(int accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);

        if (accountOpt.isPresent()) {
            return ResponseUtils.getObjectResultAsGsonString(
                    StatusResponse.SUCCESS,
                    accountOpt.get());
        }

        return ResponseUtils.getBasicResponseAsGsonString(
                StatusResponse.ERROR,
                String.format(Constants.Errors.CANNOT_FIND_ACCOUNT,
                        accountId));
    }

    @Override
    public String getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return ResponseUtils.getObjectListResultAsGsonString(accounts);
    }

    @Override
    public String accountTransfer(Transfer transfer) {

        Optional<Account> payerAccountOpt = accountRepository.findByIdWithLock(transfer.getPayerAccountId());
        Optional<Account> payeeAccountOpt = accountRepository.findByIdWithLock(transfer.getPayeeAccountId());

        if (payerAccountOpt.isPresent() && payeeAccountOpt.isPresent()) {

            Optional<Transfer> transferOpt = accountRepository.updateAccount(transfer);
            if (transferOpt.isPresent()) {
                return ResponseUtils.getObjectResultAsGsonString(StatusResponse.SUCCESS, transferOpt.get());
            }

        }

        return ResponseUtils.getBasicResponseAsGsonString(
                StatusResponse.ERROR,
                Constants.Errors.ACCOUNT_TRANSFER_ERROR);
    }
}
