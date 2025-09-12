package com.myapp.client.util;

import java.net.http.*;
import java.net.URI;

public class Api {
    // Reusable HttpClient
    private final HttpClient client = HttpClient.newHttpClient();

    // Base URL of your Spring backend
    private final String baseUrl = "https://api.example.com";

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
}
