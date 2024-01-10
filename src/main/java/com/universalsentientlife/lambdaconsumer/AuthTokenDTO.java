package com.universalsentientlife.lambdaconsumer;

public class AuthTokenDTO {
    private String token;
    private Long expiration;

    public String getToken() {
        return token;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}
