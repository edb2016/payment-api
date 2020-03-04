package com.eb.revolut.payments.service.impl;

import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.service.UserServices;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.Objects;

public class UserServicesImpl implements UserServices {

    @Override
    public User createNewUser(String body) {
        User user = new Gson().fromJson(body, User.class);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        user.setCreationDate(timestamp);
        return user;
    }

    @Override
    public boolean isUserValid(User user) {
        return Objects.nonNull(user) && user.getId()>0
                && StringUtils.isNotBlank(user.getFirstName())
                && StringUtils.isNotBlank(user.getLastName())
                && StringUtils.isNotBlank(user.getEmail())
                && StringUtils.isNotBlank(user.getUsername());
    }

}
