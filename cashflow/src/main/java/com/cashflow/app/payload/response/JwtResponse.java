package com.cashflow.app.payload.response;

import com.cashflow.app.entity.User;
import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private User user;

    public JwtResponse(String accessToken, Long id, String username, String email, User user) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.user = user;
    }
}
