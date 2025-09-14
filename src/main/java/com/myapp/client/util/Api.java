package com.myapp.client.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.myapp.client.dto.LoginRequest;
import com.myapp.client.dto.SignupRequest;
import com.myapp.client.dto.StringDto;

import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static com.myapp.client.util.Json.MAPPER;

public class Api {
    // Reusable HttpClient
    private final HttpClient client = HttpClient.newHttpClient();

    // Base URL of your Spring backend
    private final String baseUrl = "http://localhost:8080";

    // Helper: build a request with Authorization header if we have a JWT
    private HttpRequest.Builder req(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(baseUrl + path));

        // Grab JWT token from Session singleton
        String jwt = Session.get().getAccessToken();
        if (jwt != null) {
            b.header("Authorization", "Bearer " + jwt);
        }

        return b;
    }

    // Example API call: GET /policies
    public HttpResponse<String> getPolicies() throws Exception {
        HttpRequest r = req("/policies").GET().build();
        return client.send(r, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> login(LoginRequest request) throws Exception {
        String s = MAPPER.writeValueAsString(request);
        HttpRequest r = req("/api/auth/login").setHeader("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Accept-Encoding", "identity")
                .POST(HttpRequest.BodyPublishers.ofString(s, StandardCharsets.UTF_8))
                .build();
        return client.send(r, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> analyze1Async(StringDto request) {
        try {
            String s = MAPPER.writeValueAsString(request);
            HttpRequest r = req("/api/policies/analyze1")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(s, StandardCharsets.UTF_8))
                    .build();

            return client.sendAsync(r, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private boolean refreshIfPossible() {
        try {
            var opt = RefreshStore.load();
            if (opt.isEmpty()) return false;

            String body = "{\"refreshToken\":\"" + opt.get() + "\"}";
            HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/refresh"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return false;

            JsonNode json = MAPPER.readTree(res.body());
            String newAccess = json.get("accessToken").asText();
            /*Instant exp = json.has("expiresAt")
                    ? Instant.parse(json.get("expiresAt").asText())
                    : parseExpFromJwt(newAccess);*/

            Session.set(newAccess);

            if (json.has("refreshToken")) { // rotazione
                RefreshStore.save(json.get("refreshToken").asText());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Decodifica exp dal payload JWT (seconds since epoch) */
   /* private static Instant parseExpFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
            long exp = MAPPER.readTree(payload).get("exp").asLong();
            return Instant.ofEpochSecond(exp);
        } catch (Exception e) {
            // Se non riesci a leggere exp, costringi al fallback su 401
            return null;
        }
    }*/
}
