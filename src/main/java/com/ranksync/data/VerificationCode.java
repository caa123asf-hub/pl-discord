package com.ranksync.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class VerificationCode {
    private UUID playerUuid;
    private String code;
    private long expiresAt;
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}