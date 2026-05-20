package org.social.apigateway.security;

public class Endpoints {
    public static final String front_end_host = "http://localhost:5173";

    public static final String[] PUBLIC_GET_ENDPOINTS = new String[]{
            "/api/auth/active",
    };

    public static final String[] PUBLIC_POST_ENDPOINTS = new String[]{
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh-token"
    };

    public static final String[] PRIVATE_GET_ENDPOINT = new String[]{

    };

    public static final String[] PRIVATE_POST_ENDPOINT = new String[]{
            "/api/auth/dang-xuat"
    };
}
