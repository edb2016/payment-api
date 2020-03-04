package com.eb.revolut.payments.service.impl;

import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.service.UserServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServicesImplTest {

    private UserServices userServices;

    @BeforeEach
    void setUp() {
        userServices = new UserServicesImpl();
    }

    @Test
    void testIsUserValid() {
        User user = User.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .username("username")
                .id(1)
                .build();

        assertTrue(userServices.isUserValid(user));
    }

    @Test
    void testUserIsNotValid() {
        User user = User.builder()
                .firstName("firstName")
                .email("email")
                .username("username")
                .id(1)
                .build();

        assertFalse(userServices.isUserValid(user));
    }

}