package com.eb.revolut.payments.db.repository.factory;

import com.eb.revolut.payments.db.repository.impl.AccountRepositoryImpl;
import com.eb.revolut.payments.db.repository.impl.RetryStrategyImpl;
import com.eb.revolut.payments.db.repository.impl.UserRepositoryImpl;
import com.eb.revolut.payments.properties.ApplicationProperties;
import com.eb.revolut.payments.rest.response.AccountResponseService;
import com.eb.revolut.payments.rest.response.UserResponseService;
import com.eb.revolut.payments.rest.response.impl.AccountResponseServiceImpl;
import com.eb.revolut.payments.rest.response.impl.UserResponseServiceImpl;
import com.eb.revolut.payments.service.impl.AccountServicesImpl;
import com.eb.revolut.payments.service.impl.UserServicesImpl;
import lombok.NoArgsConstructor;

import javax.persistence.EntityManager;

@NoArgsConstructor
public class ResponseServiceFactory {

    public UserResponseService getUserResponseService(EntityManager entityManager) {
        return new UserResponseServiceImpl(new UserRepositoryImpl(entityManager), new UserServicesImpl());
    }

    public AccountResponseService getAccountResponseService(EntityManager entityManager, ApplicationProperties properties ) {
        long sleepTime = properties.getLongProperty("retry.sleeptime");
        int retryCount = properties.getIntProperty("retry.count");
        return new AccountResponseServiceImpl(
                new UserRepositoryImpl(entityManager),
                new AccountRepositoryImpl(entityManager, new RetryStrategyImpl(retryCount, sleepTime)),
                new AccountServicesImpl());
    }

}
