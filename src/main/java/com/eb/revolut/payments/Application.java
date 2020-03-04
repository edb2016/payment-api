package com.eb.revolut.payments;


import com.eb.revolut.payments.db.repository.factory.ResponseServiceFactory;
import com.eb.revolut.payments.properties.ApplicationProperties;
import com.eb.revolut.payments.properties.ApplicationPropertiesImpl;
import com.eb.revolut.payments.rest.context.RestContextService;
import com.eb.revolut.payments.rest.context.impl.RestContextServiceImpl;
import com.eb.revolut.payments.rest.endpoint.impl.AccountsEndpoint;
import com.eb.revolut.payments.rest.endpoint.impl.UsersEndpoint;
import lombok.extern.slf4j.Slf4j;
import spark.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static spark.Service.ignite;

@Slf4j
public class Application {

    private static final String APPLICATION_PROPERTIES = "src/main/resources/application.properties";

    public static void main(String[] args) {

        //http://localhost:4567/api/user/adduser
        //http://localhost:4567/api/users

        //TODO - Add comments to transaction!
        //TOOD - Update account so it has a status active or in-active
        //TODO - More tests for UserServiceImpl
        //TODO - Doc comments on the interfaces?

        ApplicationProperties properties = new ApplicationPropertiesImpl(APPLICATION_PROPERTIES);
        int threadPoolSize = properties.getIntProperty("application.threadpool");

        Service service = ignite();
        service.threadPool(threadPoolSize);

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("jpaPersistenceUnit");
        ResponseServiceFactory responseServiceFactory = new ResponseServiceFactory();

        RestContextService context = new RestContextServiceImpl();
        context.addEndpoint(new UsersEndpoint(service, entityManagerFactory, responseServiceFactory));
        context.addEndpoint(new AccountsEndpoint(service, entityManagerFactory, responseServiceFactory, properties));
    }
}
