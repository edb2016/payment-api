package com.eb.revolut.payments.db.repository.factory;

import com.eb.revolut.payments.properties.ApplicationProperties;
import com.eb.revolut.payments.rest.response.AccountResponseService;
import com.eb.revolut.payments.rest.response.UserResponseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseServiceFactoryTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private ApplicationProperties properties;

    @Test
    void testGetUserResponseService() {
        ResponseServiceFactory factory = new ResponseServiceFactory();
        UserResponseService userResponseService = factory.getUserResponseService(entityManager);
        assertNotNull(userResponseService);
    }

    @Test
    void testGetAccountResponseService() {
        when(properties.getLongProperty("retry.sleeptime")).thenReturn(1000l);
        when(properties.getIntProperty("retry.count")).thenReturn(5);

        ResponseServiceFactory factory = new ResponseServiceFactory();
        AccountResponseService accountResponseService = factory.getAccountResponseService(entityManager, properties);
        assertNotNull(accountResponseService);
    }

}