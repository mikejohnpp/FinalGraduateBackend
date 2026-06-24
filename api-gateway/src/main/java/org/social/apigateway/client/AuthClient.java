package org.social.apigateway.client;

import org.social.apigateway.dto.TokenValidateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${auth-service.url}")
public interface AuthClient {

    @GetMapping("/auth/validate-token")
    TokenValidateResponse validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);
}
