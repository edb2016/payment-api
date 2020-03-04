package com.eb.revolut.payments.rest.response;

import com.eb.revolut.payments.db.model.types.Transfer;

public interface AccountResponseService {

    String addAccount(int userId);

    String getAccount(int accountId);

    String getAllAccounts();

    String accountTransfer(Transfer transfer);
}
