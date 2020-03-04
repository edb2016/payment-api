package com.eb.revolut.payments.db.repository;

import com.eb.revolut.payments.db.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(int id);

    List<User> findAll();

    Optional<User> add(User user);

    Optional<User> updateUser(User user);

    Optional<User> deleteUser(User user);

}
