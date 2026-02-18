package com.cashflow.app.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeService {
    // In-memory store: Key = Email/Phone, Value = {Code, ExpiryTime}
    private final Map<String, CodeData> codeStore = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final long EXPIRATION_TIME_MS = TimeUnit.MINUTES.toMillis(5);

    public String generateCode(String identifier) {
        if (identifier != null)
            identifier = identifier.trim().toLowerCase();
        String code = String.format("%06d", random.nextInt(1000000));
        codeStore.put(identifier, new CodeData(code, System.currentTimeMillis() + EXPIRATION_TIME_MS));
        System.out.println("[DEBUG] Generated code " + code + " for identifier: [" + identifier + "]");
        return code;
    }

    public boolean verifyCode(String identifier, String code) {
        if (identifier != null)
            identifier = identifier.trim().toLowerCase();
        if (code != null)
            code = code.trim();

        System.out.println("[DEBUG] Verifying code [" + code + "] for identifier: [" + identifier + "]");

        if (code == null || code.isEmpty() || identifier == null || identifier.isEmpty()) {
            return false;
        }

        CodeData data = codeStore.get(identifier);
        if (data == null) {
            System.out.println("[DEBUG] No code found in store for identifier: [" + identifier + "]");
            return false;
        }

        if (System.currentTimeMillis() > data.expiryTime) {
            codeStore.remove(identifier);
            return false;
        }

        boolean isValid = data.code.equals(code);
        System.out.println(
                "[DEBUG] Code match result: " + isValid + " (Stored: [" + data.code + "], Provided: [" + code + "])");
        if (isValid) {
            codeStore.remove(identifier); // One-time use
        }
        return isValid;
    }

    private static class CodeData {
        String code;
        long expiryTime;

        CodeData(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}
