package org.social.chatservice.feignClient;

import org.social.chatservice.dto.TokenValidateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${auth-service.url}")
public interface UserClient {

    @GetMapping("/auth/validate-token")
    TokenValidateResponse validateToken(
            @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) String authorization);

}
