package org.social.apigateway.security;

import java.util.List;

public class Endpoints {
        public static final List<String> ALLOWED_ORIGINS = List.of(
                "http://localhost:3000",
                "https://vieface.io.vn",
                "http://192.168.1.20:3000"
        );

        public static final String[] PUBLIC_GET_ENDPOINTS = new String[] {
                        "/auth/active",
                        "/auth/validate-token",
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
