package com.epam.asset.tracking.service;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;

public interface UserService {
    BusinessProvider generateNewPassword(String username) throws InvalidUserException;
    void sendEmail(BusinessProvider user) throws  InvalidUserException;
}
