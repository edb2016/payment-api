package com.eb.revolut.payments.rest.response;

import com.eb.revolut.payments.db.model.User;
import spark.Request;

public interface UserResponseService {

    String addUser(String body);

    String getUser(int userId);

    String editUser(User user);

    String delete(int userId);

    String getAllUsers();

}
