package com.ncherkas.codechallenge.hc.k8s.orchestrator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ncherkas.codechallenge.hc.k8s.orchestrator.security.JwtConstants.*;
import static java.util.Collections.emptyList;

/**
 * Generating the JWT token on successful authentication.
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        this.authenticationManager = checkNotNull(authenticationManager);
        this.objectMapper = checkNotNull(objectMapper);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {

        LOGGER.debug("An authentication attempt");

        try {
            ApiUserCredentials credentials = objectMapper.readValue(req.getInputStream(), ApiUserCredentials.class);
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken( credentials.getUsername(), credentials.getPassword(),
                            emptyList());
            return authenticationManager.authenticate(token);
        } catch (IOException e) {
            throw new RuntimeException("Authentication attempt failure", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication auth) {

        LOGGER.debug("Processing a successful authentication");

        String token = Jwts.builder()
                .setSubject(((User) auth.getPrincipal()).getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MILLIS))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        response.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
    }
}
