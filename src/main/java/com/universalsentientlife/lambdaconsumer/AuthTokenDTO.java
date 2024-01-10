package com.universalsentientlife.lambdaconsumer;

public class AuthTokenDTO {
    private final String token;
    private final Long expiration;

    public AuthTokenDTO(String token, Long expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    public String getToken() {
        return token;
    }

    public Long getExpiration() {
        return expiration;
    }
}
