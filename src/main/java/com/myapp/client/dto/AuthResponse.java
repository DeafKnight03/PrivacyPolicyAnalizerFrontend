package com.myapp.client.dto;


public record AuthResponse(String accessToken, String refreshToken, String username, String role) {
}
