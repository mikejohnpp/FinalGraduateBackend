package org.social.apigateway.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.social.apigateway.client.AuthClient;
import org.social.apigateway.dto.TokenValidateResponse;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ErrorResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthClient authClient;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TokenValidateResponse validateResponse = authClient.validateToken(authHeader);

            Map<String, String> data = validateResponse != null ? validateResponse.getData() : null;

            if (data == null || data.get("email") == null) {
                writeErrorResponse(response, ErrorCode.INVALID_TOKEN, "Token không hợp lệ.");
                return;
            }

            final String userEmail = data.get("email");
            final String userId = data.getOrDefault("userId", "");
            final String role = data.getOrDefault("role", "");

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                List<SimpleGrantedAuthority> authorities = StringUtils.hasText(role)
                        ? List.of(new SimpleGrantedAuthority(role))
                        : List.of();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }

            HttpServletRequest mutatedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("X-User-Email".equalsIgnoreCase(name))
                        return userEmail;
                    if ("X-User-Id".equalsIgnoreCase(name))
                        return userId;
                    if ("X-User-Role".equalsIgnoreCase(name))
                        return role;
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = Collections.list(super.getHeaderNames());
                    names.add("X-User-Email");
                    names.add("X-User-Id");
                    names.add("X-User-Role");
                    return Collections.enumeration(names);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Email".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(userEmail));
                    }
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(userId));
                    }
                    if ("X-User-Role".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(role));
                    }
                    return super.getHeaders(name);
                }
            };

            filterChain.doFilter(mutatedRequest, response);
        } catch (Exception e) {
            writeErrorResponse(response, ErrorCode.INVALID_TOKEN, "Token không hợp lệ hoặc đã hết hạn.");
        }
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message(message)
                .data(null)
                .code(errorCode.getCode())
                .build();

        MAPPER.writeValue(response.getOutputStream(), errorResponse);
    }
}
