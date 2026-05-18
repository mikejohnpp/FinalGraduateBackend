package org.social.apigateway.services;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.function.Function;

public interface JWTService {

    String generateToken(String email);

    String createRefreshToken(String email);

    <T> T extractClaim(String token, Function<Claims, T> claimsTFunction);

    Date extractExpiration(String token);

    String extractEmail(String token);

    Boolean validateToken(String token, UserDetails userDetails);

}
