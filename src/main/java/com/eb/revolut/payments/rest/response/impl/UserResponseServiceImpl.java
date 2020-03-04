package com.eb.revolut.payments.rest.response.impl;

import com.eb.revolut.payments.db.model.User;
import com.eb.revolut.payments.db.model.types.Constants;
import com.eb.revolut.payments.db.repository.UserRepository;
import com.eb.revolut.payments.utils.ResponseUtils;
import com.eb.revolut.payments.rest.response.UserResponseService;
import com.eb.revolut.payments.db.model.types.StatusResponse;
import com.eb.revolut.payments.service.UserServices;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class UserResponseServiceImpl implements UserResponseService {

    private final UserRepository userRepoService;
    private final UserServices userServices;

    @Override
    public String addUser(String body) {
        User user = userServices.createNewUser(body);
        Optional<User> addUserOpt = userRepoService.add(user);

        if (addUserOpt.isPresent()) {
            return ResponseUtils.getBasicResponseAsGsonString(
                    StatusResponse.SUCCESS,
                    String.format(Constants.Success.USER_CREATED_SUCCESSFULLY,
                            addUserOpt.get().getId()));
        }


        return ResponseUtils.getBasicResponseAsGsonString(
                StatusResponse.ERROR,
                String.format(Constants.Errors.USER_NOT_CREATED,
                        body));
    }

    @Override
    public String getUser(int userId) {
        Optional<User> userOpt = userRepoService.findById(userId);

        if (userOpt.isPresent()) {
            return ResponseUtils.getObjectResultAsGsonString(
                    StatusResponse.SUCCESS,
                    userOpt.get());
        }

        return ResponseUtils.getBasicResponseAsGsonString(
                StatusResponse.ERROR,
                String.format(Constants.Errors.CANNOT_FIND_USER,
                        userId));
    }

    @Override
    public String editUser(User editedUser) {
        if (userServices.isUserValid(editedUser)) {
            Optional<User> userOpt= userRepoService.updateUser(editedUser);

            if (userOpt.isPresent()) {
                return ResponseUtils.getObjectResultAsGsonString(
                        StatusResponse.SUCCESS,
                        userOpt.get());
            }
        }
        return ResponseUtils.getBasicResponseAsGsonString(
                StatusResponse.ERROR,
                Constants.Errors.USER_NOT_UPDATED);
    }

    @Override
    public String delete(int userId) {
        Optional<User> userToDeleteOpt = userRepoService.findById(userId);

        if (userToDeleteOpt.isPresent()) {
            Optional<User> deletedUserOpt = userRepoService.deleteUser(userToDeleteOpt.get());;

            if (deletedUserOpt.isPresent()) {
                return ResponseUtils.getBasicResponseAsGsonString(
                        StatusResponse.SUCCESS,
                        String.format(Constants.Success.USER_DETLETD_SUCCESSFULLY,
                                userId));
            }
        }
        return ResponseUtils.getBasicResponseAsGsonString(
                StatusResponse.ERROR,
                String.format(Constants.Errors.USER_NOT_DELETED,
                        userId));
    }

    @Override
    public String getAllUsers() {
        List<User> users = userRepoService.findAll();
        return ResponseUtils.getObjectListResultAsGsonString(users);
    }
}
