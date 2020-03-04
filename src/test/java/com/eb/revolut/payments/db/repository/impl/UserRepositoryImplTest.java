package com.eb.revolut.payments.db.repository.impl;

import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private EntityTransaction transaction;
    @Mock
    private User user;
    @Mock
    private Query query;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl(entityManager);
    }

    @Test
    void testFindById() {
        int userId = 1;
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(entityManager.find(User.class, userId)).thenReturn(user);
        Optional<User> userOpt = userRepository.findById(userId);

        assertTrue(userOpt.isPresent());
        verify(transaction, times(1)).begin();
        verify(transaction, times(1)).commit();
    }

    @Test
    void testFindByIdThrowsIllegalArgumentException() {
        int userId = 1;
        when(transaction.isActive()).thenReturn(true);
        when(entityManager.getTransaction()).thenReturn(transaction);
        doThrow(new IllegalArgumentException()).when(entityManager).find(User.class, userId);
        Optional<User> userOpt = userRepository.findById(userId);

        assertFalse(userOpt.isPresent());
        verify(transaction, times(1)).begin();
        verify(transaction, never()).commit();
        verify(transaction, times(1)).rollback();
    }

    @Test
    void testFindAll() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(query.getResultList()).thenReturn(Collections.singletonList(user));
        when(entityManager.createQuery("from User")).thenReturn(query);
        List<User> lstOfUsers = userRepository.findAll();

        assertTrue(lstOfUsers.size()==1);
    }

    @Test
    void testFindAllThrowsIllegalArgumentException() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);
        doThrow(new IllegalArgumentException()).when(entityManager).createQuery("from User");
        List<User> lstOfUsers = userRepository.findAll();

        assertTrue(lstOfUsers.isEmpty());
        verify(transaction, times(1)).begin();
        verify(transaction, never()).commit();
        verify(transaction, times(1)).rollback();
    }

    @Test
    void testAddUser() {
        User user = User.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .username("username")
                .id(1)
                .build();

        when(entityManager.getTransaction()).thenReturn(transaction);

        Optional<User> userOpt = userRepository.add(user);
        assertTrue(userOpt.isPresent());
        assertThat(userOpt.get(), samePropertyValuesAs(user));

        verify(transaction, times(1)).begin();
        verify(entityManager, times(1)).persist(user);
        verify(transaction, times(1)).commit();
    }

    @Test
    void testAddUserThrowsIllegalArgumentException() {
        User user = User.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .username("username")
                .id(1)
                .build();

        when(entityManager.getTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);

        doThrow(new IllegalArgumentException()).when(entityManager).persist(user);

        Optional<User> userOpt = userRepository.add(user);
        assertFalse(userOpt.isPresent());

        verify(transaction, times(1)).begin();
        verify(transaction, never()).commit();
        verify(transaction, times(1)).rollback();
    }

    @Test
    void testAddNullUser() {
        Optional<User> userOpt = userRepository.add(null);
        assertFalse(userOpt.isPresent());

        verify(transaction, never()).begin();
        verify(transaction, never()).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void testUpdateUser() {
        User user = User.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .username("username")
                .id(1)
                .build();

        when(entityManager.getTransaction()).thenReturn(transaction);

        Optional<User> userOpt = userRepository.updateUser(user);
        assertTrue(userOpt.isPresent());
        assertThat(userOpt.get(), samePropertyValuesAs(user));

        verify(transaction, times(1)).begin();
        verify(entityManager, times(1)).merge(user);
        verify(transaction, times(1)).commit();
    }

    @Test
    void testUpdateUserThrowsIllegalArgumentException() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);

        doThrow(new IllegalArgumentException()).when(entityManager).merge(user);

        Optional<User> userOpt = userRepository.updateUser(user);
        assertFalse(userOpt.isPresent());

        verify(transaction, times(1)).begin();
        verify(transaction, never()).commit();
        verify(transaction, times(1)).rollback();
    }

    @Test
    void testUpdateNullUser() {
        Optional<User> userOpt = userRepository.updateUser(null);
        assertFalse(userOpt.isPresent());

        verify(transaction, never()).begin();
        verify(transaction, never()).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void testDeleteUser() {
        User user = User.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .username("username")
                .id(1)
                .build();

        when(entityManager.getTransaction()).thenReturn(transaction);

        Optional<User> userOpt = userRepository.deleteUser(user);
        assertTrue(userOpt.isPresent());
        assertThat(userOpt.get(), samePropertyValuesAs(user));

        verify(transaction, times(1)).begin();
        verify(entityManager, times(1)).remove(user);
        verify(transaction, times(1)).commit();
    }

    @Test
    void testDeleteUserThrowsIllegalArgumentException() {
        User user = User.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .username("username")
                .id(1)
                .build();

        when(transaction.isActive()).thenReturn(true);
        doThrow(new IllegalArgumentException()).when(entityManager).remove(user);
        when(entityManager.getTransaction()).thenReturn(transaction);

        Optional<User> userOpt = userRepository.deleteUser(user);
        assertFalse(userOpt.isPresent());

        verify(transaction, times(1)).begin();
        verify(transaction, never()).commit();
        verify(transaction, times(1)).rollback();
    }

}