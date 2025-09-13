package com.myapp.client.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

public final class Session {
    private static final Duration SKEW = Duration.ofSeconds(60); // margine prima della scadenza
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static volatile Session INSTANCE;

    private static volatile String accessToken; // JWT
    private static volatile Instant expiresAt;  // scadenza del JWT

    private Session() {}

    public static Session get() {
        Session ref = INSTANCE;
        if (ref == null) {
            synchronized (Session.class) {
                ref = INSTANCE;
                if (ref == null) {
                    ref = new Session();
                    INSTANCE = ref;
                }
            }
        }
        return ref;
    }

    /** Imposta token e calcola exp dal payload JWT */
    public static void set(String token) {
        Objects.requireNonNull(token, "token must not be null");

        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String payloadJson = new String(
                Base64.getUrlDecoder().decode(parts[1]),
                java.nio.charset.StandardCharsets.UTF_8
        );

        try {
            JsonNode n = MAPPER.readTree(payloadJson);
            if (!n.hasNonNull("exp")) {
                throw new IllegalArgumentException("JWT has no 'exp' claim");
            }
            long expSec = n.get("exp").asLong();
            accessToken = token;
            expiresAt = Instant.ofEpochSecond(expSec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse JWT payload", e);
        }
    }

    /** Variante se il server restituisce anche expiresAt/ExpiresIn */
    public synchronized void set(String token, Instant explicitExpiry) {
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(explicitExpiry, "explicitExpiry must not be null");
        this.accessToken = token;
        this.expiresAt = explicitExpiry;
    }

    public String getAccessToken() { return accessToken; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean hasToken() { return accessToken != null; }

    /** True se manca poco alla scadenza (o non abbiamo ancora una scadenza) */
    public boolean nearExpiry() {
        return expiresAt == null || Instant.now().plus(SKEW).isAfter(expiresAt);
    }

    /** Secondi rimanenti (o -1 se sconosciuto) */
    public long remainingSeconds() {
        return (expiresAt == null) ? -1 : Math.max(0, java.time.Duration.between(Instant.now(), expiresAt).getSeconds());
    }

    /** Comodo per costruire l'header Authorization */
    public String getAuthHeader() {
        return accessToken == null ? null : "Bearer " + accessToken;
    }

    public static synchronized void clear() {
        accessToken = null;
        expiresAt = null;
    }
}
