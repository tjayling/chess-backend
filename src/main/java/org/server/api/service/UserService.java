package org.server.api.service;

import org.server.api.model.LoginDetails;
import org.server.api.model.User;

public interface UserService {
    User login(LoginDetails loginDetails);
    boolean signup(User user);
}
