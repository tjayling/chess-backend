package org.server.api.service.impl;

import org.server.api.model.LoginDetails;
import org.server.api.model.User;
import org.server.api.persistence.UserRepository;
import org.server.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User login(LoginDetails loginDetails) {
        String username = loginDetails.getUsername();
        String password = loginDetails.getPassword();

        List<User> users = userRepository.findByUsername(username);
        if (users.size() == 0) {
            logger.info("User does not exist.");
            return null;
        }

        User user = users.get(0);
        if( user.getPassword().equals(password)) {
            return users.get(0);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean signup(User user) {
        Assert.notNull(user, "User cannot be null in service.");
        boolean usernameExists = userRepository.findByUsername(user.getUsername()).size() > 0;
        boolean emailExists = userRepository.findByEmail(user.getEmail()).size() > 0;
        if (usernameExists && emailExists) {
            return false;
        }
        userRepository.save(user);
        return true;
    }
}
