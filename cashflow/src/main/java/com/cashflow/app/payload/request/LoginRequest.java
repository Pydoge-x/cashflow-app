package com.cashflow.app.payload.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String account;
    private String password;
}
