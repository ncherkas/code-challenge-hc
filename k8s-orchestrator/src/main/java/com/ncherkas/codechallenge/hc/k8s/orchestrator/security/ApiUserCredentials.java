package com.ncherkas.codechallenge.hc.k8s.orchestrator.security;

/**
 * Just a POJO for the API User credentials.
 */
public class ApiUserCredentials {

    private String username;
    private String password;

    public ApiUserCredentials() {
    }

    private ApiUserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static ApiUserCredentials of(String login, String password) {
        return new ApiUserCredentials(login, password);
    }
}

