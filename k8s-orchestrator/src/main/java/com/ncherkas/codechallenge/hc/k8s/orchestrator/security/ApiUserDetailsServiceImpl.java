package com.ncherkas.codechallenge.hc.k8s.orchestrator.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static java.util.Collections.emptyList;

/**
 * This is a kind of dummy service which in a real life should be backed by some repository over the database.
 */
@Service
public class ApiUserDetailsServiceImpl implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUserDetailsServiceImpl.class);

    private final ApiUserCredentials validCredentials;

    public ApiUserDetailsServiceImpl(@Value("${k8s.orchestrator.api.user.username}") String login,
                                     @Value("${k8s.orchestrator.api.user.password}") String password) {

        // The password is bcrypt-hashed
        this.validCredentials = ApiUserCredentials.of(login, password);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.debug("Login check for {}", username);
        if (!validCredentials.getUsername().equals(username)) {
            throw new UsernameNotFoundException(username);
        }

        return new User(validCredentials.getUsername(), validCredentials.getPassword(), emptyList());
    }
}
