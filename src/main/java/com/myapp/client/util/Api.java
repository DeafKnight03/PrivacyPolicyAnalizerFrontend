package com.myapp.client.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.myapp.client.dto.LoginRequest;
import com.myapp.client.dto.SaveResultRequest;
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
    public HttpResponse<String> getPolicies(StringDto request) throws Exception {

        String s = MAPPER.writeValueAsString(request);
        HttpRequest r = req("/api/policies/getPolicies")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(s, StandardCharsets.UTF_8))
                .build();
        return client.send(r,HttpResponse.BodyHandlers.ofString());
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

    public HttpResponse<Void> save(SaveResultRequest request) throws Exception {
        String s = MAPPER.writeValueAsString(request);
        HttpRequest r = req("/api/policies/save")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(s, StandardCharsets.UTF_8))
                .build();
        return client.send(r,HttpResponse.BodyHandlers.discarding());
    }

    public HttpResponse<String> count(StringDto request) throws Exception {
        String s = MAPPER.writeValueAsString(request);
        HttpRequest r = req("/api/policies/count")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(s, StandardCharsets.UTF_8))
                .build();
        return client.send(r,HttpResponse.BodyHandlers.ofString());
    }


}
