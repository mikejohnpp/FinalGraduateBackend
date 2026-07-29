package org.social.authservice.security;

public class Endpoints {
    public static final String front_end_host = "http://localhost:3000";

    public static final String[] PUBLIC_GET_ENDPOINTS = new String[] {
            "/auth/validate-token",
            "/auth/active",
    };

    public static final String[] PUBLIC_POST_ENDPOINTS = new String[] {
            "/auth/register",
            "/auth/login",
            "/auth/refresh-token",
            "/auth/logout",
            "/auth/forgot-password",
            "/auth/verify-otp",
            "/auth/reset-password"
    };

    public static final String[] PRIVATE_GET_ENDPOINT = new String[] {

    };

    public static final String[] PRIVATE_POST_ENDPOINT = new String[] {
    };
}
