package com.eb.revolut.payments.db.repository.impl;

import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final EntityManager entityManager;

    public Optional<User> findById(int id) {
        User user = null;
        try {
            entityManager.getTransaction().begin();
            user = entityManager.find(User.class, id);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error error loading user with Id: %s",id), e);
        }
        return Objects.nonNull(user) ? Optional.of(user) : Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = null;
        try {
            entityManager.getTransaction().begin();
            users = entityManager.createQuery("from User").getResultList();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error error call findAll "), e);
        }
        return Objects.isNull(users) ? Collections.emptyList():users;
    }

    public Optional<User> add(User user) {
        try {
            if (Objects.nonNull(user)) {
                entityManager.getTransaction().begin();
                entityManager.persist(user);
                entityManager.getTransaction().commit();
                return Optional.of(user);
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error adding user: %s",user), e);
        }
        return Optional.empty();
    }

    public Optional<User> updateUser(User user) {
        try {
            if (Objects.nonNull(user)) {
                entityManager.getTransaction().begin();
                entityManager.merge(user);
                entityManager.getTransaction().commit();
                return Optional.of(user);
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error updating user: %s",user), e);
        }
        return Optional.empty();
    }

    public Optional<User> deleteUser(User user) {
        try {
            if (Objects.nonNull(user)) {
                entityManager.getTransaction().begin();
                entityManager.remove(user);
                entityManager.getTransaction().commit();
                return Optional.of(user);
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(String.format("Error updating user: %s",user), e);
        }
        return Optional.empty();
    }
}
