package com.ncherkas.codechallenge.hc.k8s.orchestrator.security;

/**
 * Constants used by the JWT token generator.
 */
public class JwtConstants {

    public static final String SECRET = "hazelcast_cloud";
    public static final long EXPIRATION_TIME_MILLIS = 432_000_000; // 5 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}

