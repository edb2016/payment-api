package com.eb.revolut.payments.service;

import com.eb.revolut.payments.db.model.User;

public interface UserServices {

    User createNewUser(String body);

    boolean isUserValid(User user);

}
