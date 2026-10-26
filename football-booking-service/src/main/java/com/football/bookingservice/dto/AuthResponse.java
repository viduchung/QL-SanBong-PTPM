package com.football.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin phản hồi đăng nhập kèm Access Token")
public class AuthResponse {

    @Schema(description = "Chuỗi JSON Web Token dùng để xác thực Bearer", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String accessToken;

    @Schema(description = "Loại token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Thời gian hết hạn của token tính bằng giây", example = "86400")
    private long expiresIn;

    @Schema(description = "Tên đăng nhập", example = "customer01")
    private String username;

    @Schema(description = "Vai trò", example = "ROLE_CUSTOMER")
    private String role;

    @Schema(description = "Họ và tên", example = "Nguyen Van An")
    private String fullName;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String tokenType, long expiresIn, String username, String role, String fullName) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
