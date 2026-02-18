package com.cashflow.app.payload.request;

import lombok.Data;

@Data
public class CodeRequest {
    private String target; // Email or Phone
    private String method; // EMAIL or PHONE
}
