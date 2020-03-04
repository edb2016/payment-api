package com.revolut.payments.rest.utls;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.repository.AccountRepository;
import com.eb.revolut.payments.db.repository.UserRepository;
import com.eb.revolut.payments.db.repository.impl.AccountRepositoryImpl;
import com.eb.revolut.payments.db.repository.impl.RetryStrategyImpl;
import com.eb.revolut.payments.db.repository.impl.UserRepositoryImpl;
import com.eb.revolut.payments.db.model.types.StandardResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TestUtils {

    public static class Db {

        private static final String JSON_USERS_DATA = "src/test/resources/users.json";

        public static void resetDB(EntityManagerFactory entityManagerFactory) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY=0").executeUpdate();
            entityManager.createNativeQuery("delete from Account").executeUpdate();
            entityManager.createNativeQuery("delete from User").executeUpdate();
            entityManager.createNativeQuery("truncate table Account").executeUpdate();
            entityManager.createNativeQuery("truncate table User").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY=1").executeUpdate();
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        public static void resetUsersDB(EntityManagerFactory entityManagerFactory) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY=0").executeUpdate();
            entityManager.createNativeQuery("delete from User").executeUpdate();
            entityManager.createNativeQuery("truncate table User").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY=1").executeUpdate();
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        public static void resetAccountsDB(EntityManagerFactory entityManagerFactory) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY=0").executeUpdate();
            entityManager.createNativeQuery("delete from Account").executeUpdate();
            entityManager.createNativeQuery("truncate table Account").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY=1").executeUpdate();
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        public static List<User> setupUsers(EntityManagerFactory entityManagerFactory) throws IOException {
            JSONArray jsonArray = TestUtils.getStreamOfJsonObjects(JSON_USERS_DATA);

            List<User> users = IntStream
                    .range(0, jsonArray.length())
                    .mapToObj(i -> jsonArray.getJSONObject(i))
                    .map(JSONObject::valueToString)
                    .map(TestUtils::getUserFromString)
                    .collect(Collectors.toList());

            EntityManager entityManager = entityManagerFactory.createEntityManager();
            UserRepository userRepository = new UserRepositoryImpl(entityManager);
            users.stream().forEach( user -> userRepository.add(user));
            entityManager.close();
            return users;
        }

        public static List<Account> setupMutipleAccountsFromUserList(EntityManagerFactory entityManagerFactory, List<User> users) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, new RetryStrategyImpl(5, 1000));

            List<Account> accountsLst = users.stream()
                    .map(user -> createNewAccount(user))
                    .map(account -> accountRepository.add(account))
                    .filter(Optional::isPresent)
                    .map(accountOptional -> accountOptional.get())
                    .collect(Collectors.toList());

            entityManager.close();
            return accountsLst;
        }

        public static List<Account> setupMutipleAccountsFromUserListWithInitialBalance(
                EntityManagerFactory entityManagerFactory,
                List<User> users,
                double initialBalance) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, new RetryStrategyImpl(5, 1000));

            List<Account> accountsLst = users.stream()
                    .map(user -> createNewAccount(user, new BigDecimal(initialBalance)))
                    .map(account -> accountRepository.add(account))
                    .filter(Optional::isPresent)
                    .map(accountOptional -> accountOptional.get())
                    .collect(Collectors.toList());

            entityManager.close();
            return accountsLst;
        }

        public static List<Account> setupMutlipleAccounts(EntityManagerFactory entityManagerFactory, List<Account> accounts) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, new RetryStrategyImpl(5, 1000));

            List<Account> accountsLst = accounts.stream()
                    .map(account -> accountRepository.add(account))
                    .filter(Optional::isPresent)
                    .map(accountOptional -> accountOptional.get())
                    .collect(Collectors.toList());

            entityManager.close();
            return accountsLst;
        }

        public static Optional<Account> getAccount(EntityManagerFactory entityManagerFactory, int accountId) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            AccountRepository accountRepository = new AccountRepositoryImpl(entityManager, new RetryStrategyImpl(5, 1000));
            Optional<Account> result = accountRepository.findById(accountId);
            entityManager.close();
            return result;
        }


    }

    public static Account buildAccountFromJson(String json) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        return gson.fromJson(json, Account.class);
    }

    public static User buildUserFromJson(String json) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        return gson.fromJson(json, User.class);
    }

    public static String buildUserFromJson(User user) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        return gson.toJson(user);
    }

    public static List<User> getListOfUsersFromResponse(StandardResponse response) {
        List<User> result = new ArrayList<>();
        if (hasData(response)) {
            JsonArray jsonArray = response.getData().getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement jsonElement = jsonArray.get(i);
                result.add(new Gson().fromJson(jsonElement, User.class));
            }
        }

        if (hasMessage(response)) {
            JSONArray jsonArray = new JSONArray(response.getMessage());
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonStr = getStringFromJsonArray(jsonArray, i);
                result.add(new Gson().fromJson(jsonStr, User.class));
            }
        }
        return result;
    }

    public static Account createNewAccount(User user, BigDecimal startingBalance) {
        return Account.builder()
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .user(user)
                .balance(startingBalance)
                .build();
    }

    public static Account createNewAccount(User user) {
        return Account.builder()
                .creationDate(new Timestamp(System.currentTimeMillis()))
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();
    }

    public static Map<Integer, User> getUserIdToUserMap(List<User> users) {
        Map<Integer, User> map = new HashMap<>();
        users.stream()
                .filter(Objects::nonNull)
                .forEach(user -> {
                    map.put(user.getId(), user);
                });
        return map;
    }

    public static String readFileToJsonString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static JSONArray getStreamOfJsonObjects(String path) throws IOException {
        String jsonStr = readFileToJsonString(path);
        JSONObject jsonObject = new JSONObject(jsonStr);
        return ((JSONArray)jsonObject.get("users"));
    }

    public static String getStringFromJsonArray(JSONArray jsonArray, int index) {
        JSONObject jsonObject = jsonArray.getJSONObject(index);
        return JSONObject.valueToString(jsonObject);
    }

    public static User getUserFromString(String user) {
        return new Gson().fromJson(user, User.class);
    }

    private static boolean hasData(StandardResponse response) {
        return (Objects.nonNull(response) && Objects.nonNull(response.getData()));
    }

    private static boolean hasMessage(StandardResponse response) {
        return (Objects.nonNull(response) && Objects.nonNull(response.getMessage()));
    }

}
