package com.eb.revolut.payments.rest.endpoint.impl;

import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.db.repository.factory.ResponseServiceFactory;
import com.eb.revolut.payments.properties.ApplicationProperties;
import com.eb.revolut.payments.rest.endpoint.Endpoint;
import com.eb.revolut.payments.rest.response.AccountResponseService;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import spark.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static spark.Spark.after;

@AllArgsConstructor
public class AccountsEndpoint implements Endpoint {

    private static final String ACCOUNT_ALL_URL = "/api/accounts";
    private static final String ACCOUNT_ID_URL = "/api/account/:accountId";
    private static final String ACCOUNT_ADD_URL = "/api/account/add/:userId";
    private static final String ACCOUNT_TRANSFER_URL = "/api/account/transfer/";

    private final Service service;
    private final EntityManagerFactory entityManagerFactory;
    private final ResponseServiceFactory responseServiceFactory;
    private final ApplicationProperties properties;

    @Override
    public void configure() {

        service.get(ACCOUNT_ADD_URL, (request, response) -> {
            int userId = Integer.parseInt(request.params(":userId"));
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            AccountResponseService accountResponseService = responseServiceFactory.getAccountResponseService(entityManager, properties);

            String result = accountResponseService.addAccount(userId);
            entityManager.close();
            return result;
        });


        service.get(ACCOUNT_ID_URL, (request, response) -> {
            int accountId = Integer.parseInt(request.params(":accountId"));
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            AccountResponseService accountResponseService = responseServiceFactory.getAccountResponseService(entityManager, properties);

            String result = accountResponseService.getAccount(accountId);
            entityManager.close();
            return result;
        });

        service.get(ACCOUNT_ALL_URL, (request, response) -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            AccountResponseService accountResponseService = responseServiceFactory.getAccountResponseService(entityManager, properties);

            String result = accountResponseService.getAllAccounts();
            entityManager.close();
            return result;
        });

        service.post(ACCOUNT_TRANSFER_URL, (request, response) -> {
            String body = request.body();
            Transfer transfer = new Gson().fromJson(body, Transfer.class);
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            AccountResponseService accountResponseService = responseServiceFactory.getAccountResponseService(entityManager, properties);

            String result = accountResponseService.accountTransfer(transfer);
            entityManager.close();
            return result;
        });


        after((request, response) -> {
            response.type("application/json");
        });
    }

}
