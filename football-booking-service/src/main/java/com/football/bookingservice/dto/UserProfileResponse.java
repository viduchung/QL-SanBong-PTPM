package com.football.bookingservice.dto;

import com.football.bookingservice.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin chi tiết tài khoản đang đăng nhập")
public class UserProfileResponse {

    private Long id;
    private String username;
    private Role role;
    private String fullName;
    private String phone;
    private boolean active;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String username, Role role, String fullName, String phone, boolean active) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
