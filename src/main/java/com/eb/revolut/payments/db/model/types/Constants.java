package com.eb.revolut.payments.db.model.types;

public class Constants {


    public static class Errors {
        public static final String USER_NOT_DELETED = "User %s does not exist or cannot be deleted";
        public static final String USER_DOES_NOT_EXIST = "User with id %s does not exist";
        public static final String CANNOT_FIND_USER = "Unable to find user with id: %s";
        public static final String USER_NOT_UPDATED = "Unable to update user";
        public static final String USER_NOT_CREATED = "Unable to create user: %s";

        public static final String ACCOUNT_NOT_CREATED = "Unable to create account for user: %s";
        public static final String CANNOT_FIND_ACCOUNT = "Unable to find account with id: %s";
        public static final String ACCOUNT_TRANSFER_ERROR = "Unable to complete transfer";
    };

    public static class Success {
        public static final String ACCOUNT_CREATED_SUCCESSFULLY = "Account for user %s has been successfully setup, accoundId: %s";
        public static final String USER_CREATED_SUCCESSFULLY = "User %s has been successfully created";
        public static final String USER_DETLETD_SUCCESSFULLY = "User %s has been successfully deleted";
    }

}
