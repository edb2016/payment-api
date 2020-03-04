package com.eb.revolut.payments.utils;

import com.eb.revolut.payments.db.model.Account;
import com.eb.revolut.payments.db.model.types.Transfer;
import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.model.types.StatusResponse;
import com.eb.revolut.payments.db.model.types.StandardResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ResponseUtils {

    public static String getBasicResponseAsGsonString(StatusResponse statusResponse, String message) {
        return new Gson().toJson(
                new StandardResponse(statusResponse, message));
    }

    public static String getObjectResultAsGsonString(StatusResponse statusResponse, Account account) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return new Gson().toJson(
                new StandardResponse(statusResponse,gson.toJson(account)));
    }

    public static String getObjectResultAsGsonString(StatusResponse statusResponse, User user) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return new Gson().toJson(
                new StandardResponse(statusResponse,gson.toJson(user)));
    }

    public static String getObjectResultAsGsonString(StatusResponse statusResponse, Transfer transfer) {
        return new Gson().toJson(
                new StandardResponse(statusResponse,
                        new Gson().toJson(transfer)));
    }

    public static String getObjectResultAsGsonString(Optional<?> result) {
        return new Gson().toJson(
                new StandardResponse(result.isPresent() ?
                        StatusResponse.SUCCESS : StatusResponse.ERROR,
                        new Gson().toJson(result.isPresent() ?
                                result.get(): Optional.empty())));
    }

    public static String getObjectListResultAsGsonString(List<?> result) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return new Gson().toJson(
                new StandardResponse(Objects.nonNull(result) ?
                        StatusResponse.SUCCESS : StatusResponse.ERROR, gson.toJson(result)));
    }
}
