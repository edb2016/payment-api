package com.eb.revolut.payments.rest.response.impl;

import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.repository.factory.ResponseServiceFactory;
import com.eb.revolut.payments.db.repository.UserRepository;
import com.eb.revolut.payments.db.model.types.StandardResponse;
import com.eb.revolut.payments.rest.response.UserResponseService;
import com.eb.revolut.payments.db.model.types.StatusResponse;
import com.eb.revolut.payments.service.UserServices;
import com.google.gson.Gson;
import com.revolut.payments.rest.utls.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserResponseServiceImplTest {

    @Mock
    ResponseServiceFactory responseServiceFactory;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserServices userServices;

    private UserResponseService userResponseService;
    private User user;
    private Gson gson;

    @BeforeEach
    void beforeEach() {
        gson = new Gson();
        userResponseService = new UserResponseServiceImpl(userRepository, userServices);
        user = User.builder()
                .firstName("firstName")
                .lastName("lastName")
                .username("username")
                .email("email")
                .id(1)
                .build();
    }

    @Test
    void testAddNewUser() {
        String requestBody = "body";

        when(userServices.createNewUser(requestBody)).thenReturn(user);
        when(userRepository.add(user)).thenReturn(Optional.of(user));

        String jsonResult = userResponseService.addUser(requestBody);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "User 1 has been successfully created";

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testFailedAddNewUser() {
        String requestBody = "body";

        when(userServices.createNewUser(requestBody)).thenReturn(user);
        when(userRepository.add(user)).thenReturn(Optional.empty());

        String jsonResult = userResponseService.addUser(requestBody);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "Unable to create user: "+requestBody;

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.ERROR));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testGetUser() {
        int userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String jsonResult = userResponseService.getUser(userId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));

        User result = TestUtils.buildUserFromJson(response.getMessage());
        assertThat(result, samePropertyValuesAs(user));
    }

    @Test
    void testFailedToGetUser() {
        int userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        String jsonResult = userResponseService.getUser(userId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "Unable to find user with id: 1";

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.ERROR));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testEditUser() {
        when(userServices.isUserValid(user)).thenReturn(true);
        when(userRepository.updateUser(user)).thenReturn(Optional.of(user));

        String jsonResult = userResponseService.editUser(user);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));

        User result = TestUtils.buildUserFromJson(response.getMessage());
        assertThat(result, samePropertyValuesAs(user));
    }

    @Test
    void testEditInvalidUser() {
        when(userServices.isUserValid(user)).thenReturn(false);

        String jsonResult = userResponseService.editUser(user);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "Unable to update user";

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.ERROR));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testDeleteUser() {
        int userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.deleteUser(user)).thenReturn(Optional.of(user));

        String jsonResult = userResponseService.delete(userId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "User 1 has been successfully deleted";

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

    @Test
    void testFailedDeleteUser() {
        int userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.deleteUser(user)).thenReturn(Optional.empty());

        String jsonResult = userResponseService.delete(userId);
        StandardResponse response = gson.fromJson(jsonResult, StandardResponse.class);

        String expectedMessage = "User 1 does not exist or cannot be deleted";

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.ERROR));
        assertThat(response.getMessage(), equalTo(expectedMessage));
    }

}