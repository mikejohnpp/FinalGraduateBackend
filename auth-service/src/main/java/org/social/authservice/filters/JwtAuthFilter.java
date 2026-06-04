package org.social.authservice.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.social.authservice.services.JWTService;
import org.social.authservice.services.UserService;
import org.social.common.exceptions.ErrorCode;
import org.social.common.exceptions.ErrorResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserService userService;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        String path = request.getServletPath();

        if (path.equals("/auth/refresh-token")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractEmail(jwt);

            HttpServletRequest mutatedRequest = request;

            if (StringUtils.hasText(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(userEmail);

                if (jwtService.validateToken(jwt, userDetails)) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);

                    mutatedRequest = new jakarta.servlet.http.HttpServletRequestWrapper(request) {
                        @Override
                        public String getHeader(String name) {
                            if ("X-User-Email".equalsIgnoreCase(name)) return userEmail;
                            return super.getHeader(name);
                        }

                        @Override
                        public java.util.Enumeration<String> getHeaderNames() {
                            java.util.List<String> names = java.util.Collections.list(super.getHeaderNames());
                            names.add("X-User-Email");
                            return java.util.Collections.enumeration(names);
                        }

                        @Override
                        public java.util.Enumeration<String> getHeaders(String name) {
                            if ("X-User-Email".equalsIgnoreCase(name)) {
                                return java.util.Collections.enumeration(java.util.List.of(userEmail));
                            }
                            return super.getHeaders(name);
                        }
                    };
                }
            }
            filterChain.doFilter(mutatedRequest, response);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            writeErrorResponse(response, request, ErrorCode.INVALID_TOKEN, "Token đã hết hạn. Vui lòng đăng nhập lại hoặc sử dụng Refresh Token.");
        } catch (io.jsonwebtoken.JwtException e) {
            writeErrorResponse(response, request, ErrorCode.INVALID_TOKEN, "Token không hợp lệ.");
        }
    }

    private void writeErrorResponse(HttpServletResponse response, HttpServletRequest request,
                                     ErrorCode errorCode, String message) throws IOException {
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
