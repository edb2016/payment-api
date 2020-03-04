package com.eb.revolut.payments.rest.endpoint.impl;

import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.repository.factory.ResponseServiceFactory;
import com.eb.revolut.payments.rest.endpoint.Endpoint;
import com.eb.revolut.payments.rest.response.UserResponseService;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import spark.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static spark.Spark.*;

@AllArgsConstructor
public class UsersEndpoint implements Endpoint {

    private static final String USER_ADD_URL = "/api/user/add";
    private static final String USER_ALL_URL = "/api/user/users";
    private static final String USER_ID_URL = "/api/user/:id";
    private static final String USER_EDIT_URL = "/api/user/edit";

    private final Service service;
    private final EntityManagerFactory entityManagerFactory;
    private final ResponseServiceFactory responseServiceFactory;

    @Override
    public void configure() {
        service.post(USER_ADD_URL, (request, response) -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            UserResponseService userResponseService = responseServiceFactory.getUserResponseService(entityManager);

            String body = request.body();
            String result = userResponseService.addUser(body);
            entityManager.close();
            return result;
        });

        service.get(USER_ALL_URL, (request, response) -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            UserResponseService userResponseService = responseServiceFactory.getUserResponseService(entityManager);

            String result = userResponseService.getAllUsers();
            entityManager.close();
            return result;
        });

        service.get(USER_ID_URL, (request, response) -> {
            int userId = Integer.parseInt(request.params(":id"));
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            UserResponseService userResponseService = responseServiceFactory.getUserResponseService(entityManager);

            String result = userResponseService.getUser(userId);
            entityManager.close();
            return result;
        });

        service.put(USER_EDIT_URL, (request, response) -> {
            String body = request.body();
            System.out.println(body);
            User toEdit = new Gson().fromJson(body, User.class);
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            UserResponseService userResponseService = responseServiceFactory.getUserResponseService(entityManager);

            String result = userResponseService.editUser(toEdit);
            entityManager.close();
            return result;
        });

        after((request, response) -> {
            response.type("application/json");
        });
    }
}
