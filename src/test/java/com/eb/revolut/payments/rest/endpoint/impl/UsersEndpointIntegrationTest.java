package com.eb.revolut.payments.rest.endpoint.impl;

import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.repository.factory.ResponseServiceFactory;
import com.eb.revolut.payments.rest.context.RestContextService;
import com.eb.revolut.payments.rest.context.impl.RestContextServiceImpl;
import com.eb.revolut.payments.db.model.types.StandardResponse;
import com.eb.revolut.payments.db.model.types.StatusResponse;
import com.revolut.payments.rest.utls.RequestResponseUtils;
import com.revolut.payments.rest.utls.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static spark.Service.ignite;

@Slf4j
class UsersEndpointIntegrationTest {

    private static final String JSON_TEST_DATA = "src/test/resources/users.json";
    private static final String USER_ADD_URL = "http://localhost:8080/api/user/add";
    private static final String USER_ALL_URL = "http://localhost:8080/api/user/users";
    private static final String USER_ID_URL = "http://localhost:8080/api/user/";
    private static final String USER_EDIT_URL = "http://localhost:8080/api/user/edit";

    private static Service service;
    private static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    public static void beforeAll() {
        service = ignite()
                .threadPool(4)
                .port(8080);

        entityManagerFactory = Persistence.createEntityManagerFactory("jpaPersistenceUnitTest");

        RestContextService context = new RestContextServiceImpl();
        context.addEndpoint(new UsersEndpoint(service, entityManagerFactory, new ResponseServiceFactory()));
    }

    @Test
    public void testAddUsers() throws IOException {
        JSONArray jsonArray = TestUtils.getStreamOfJsonObjects(JSON_TEST_DATA);
        String user1 = TestUtils.getStringFromJsonArray(jsonArray,0);
        String user2 = TestUtils.getStringFromJsonArray(jsonArray,1);

        StandardResponse response1 = RequestResponseUtils.requestJsonPost(USER_ADD_URL, user1);

        assertNotNull(response1);
        assertThat(response1.getStatus(), equalTo(StatusResponse.SUCCESS));
        assertThat(response1.getMessage(), containsString("has been successfully created"));

        StandardResponse response2 = RequestResponseUtils.requestJsonPost(USER_ADD_URL, user2);

        assertNotNull(response2);
        assertThat(response2.getStatus(), equalTo(StatusResponse.SUCCESS));
        assertThat(response2.getMessage(), containsString("has been successfully created"));
    }

    @Test
    public void testGetAllUsers() throws IOException {
        List<User> users = TestUtils.Db.setupUsers(entityManagerFactory);
        StandardResponse response = RequestResponseUtils.requestJsonGet(USER_ALL_URL);

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));

        Map<Integer, User> userMap = TestUtils.getUserIdToUserMap(users);
        List<User> responseUsers = TestUtils.getListOfUsersFromResponse(response);

        assertTrue(responseUsers.size() == userMap.entrySet().size());

        responseUsers.stream()
                .filter(Objects::nonNull)
                .forEach(user -> {
                    User expected = userMap.get(user.getId());
                    assertThat(user.getFirstName(), equalTo(expected.getFirstName()));
                    assertThat(user.getLastName(), equalTo(expected.getLastName()));
                    assertThat(user.getUsername(), equalTo(expected.getUsername()));
                    assertThat(user.getEmail(), equalTo(expected.getEmail()));
                });
    }


    @Test
    public void testGetUser() throws IOException {
        List<User> users = TestUtils.Db.setupUsers(entityManagerFactory);

        User user = users.get(0);
        String url = USER_ID_URL + user.getId();
        StandardResponse response = RequestResponseUtils.requestJsonGet(url);

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));

        User result = TestUtils.getUserFromString(response.getMessage());
        assertThat(result.getFirstName(), equalTo(user.getFirstName()));
        assertThat(result.getLastName(), equalTo(user.getLastName()));
        assertThat(result.getUsername(), equalTo(user.getUsername()));
        assertThat(result.getEmail(), equalTo(user.getEmail()));

    }

    @Test
    public void testEditUser() throws IOException {
        List<User> users = TestUtils.Db.setupUsers(entityManagerFactory);

        //Select a random record to edit
        User editedRecord = users.get(1);
        editedRecord.setFirstName("Andrew");
        editedRecord.setLastName("Smith");

        String editedUserJson = TestUtils.buildUserFromJson(editedRecord);

        StandardResponse response = RequestResponseUtils.requestJsonPut(USER_EDIT_URL, editedUserJson);

        assertNotNull(response);
        assertThat(response.getStatus(), equalTo(StatusResponse.SUCCESS));

        User actualResult = TestUtils.getUserFromString(response.getMessage());

        assertThat(actualResult.getId(), equalTo(editedRecord.getId()));
        assertThat(actualResult.getFirstName(), equalTo(editedRecord.getFirstName()));
        assertThat(actualResult.getLastName(), equalTo(editedRecord.getLastName()));
        assertThat(actualResult.getUsername(), equalTo(editedRecord.getUsername()));
        assertThat(actualResult.getEmail(), equalTo(editedRecord.getEmail()));

    }

    @AfterEach
    void afterEach() throws IOException {
        TestUtils.Db.resetUsersDB(entityManagerFactory);
    }

    @AfterAll
    public static void afterAll() {
        service.stop();
        entityManagerFactory.close();
    }
}