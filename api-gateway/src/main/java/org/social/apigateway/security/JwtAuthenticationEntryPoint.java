package org.social.apigateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ErrorResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("Yêu cầu xác thực. Vui lòng đăng nhập lại.")
                .data(null)
                .code(ErrorCode.UNAUTHORIZED.getCode())
                .build();

        MAPPER.writeValue(response.getOutputStream(), errorResponse);
    }
}
