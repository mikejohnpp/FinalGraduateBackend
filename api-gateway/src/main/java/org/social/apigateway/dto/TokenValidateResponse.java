package org.social.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidateResponse {

    private Boolean success;

    private String message;

    private Map<String, String> data;

    private Integer code;
}
