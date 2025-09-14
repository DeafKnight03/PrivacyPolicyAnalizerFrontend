package com.myapp.client.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class JwtUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Extracts the "uid" claim as Long from a JWT (no signature verification). */
    public static Long userIdFromJwt(String token) {
        if (token == null || token.isBlank()) return null;

        // strip "Bearer " if present
        if (token.startsWith("Bearer ")) token = token.substring(7);

        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT format");

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        try {
            JsonNode n = MAPPER.readTree(payloadJson);
            JsonNode uid = n.get("uid");
            if (uid == null || uid.isNull()) return null;

            // handle number or string
            if (uid.isNumber()) return uid.longValue();
            String s = uid.asText();
            if (s.isBlank()) return null;
            return Long.parseLong(s);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT payload", e);
        }
    }

}
