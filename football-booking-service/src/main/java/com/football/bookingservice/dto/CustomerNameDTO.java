package com.football.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin tên khách hàng có cấu trúc theo chuẩn API v2")
public class CustomerNameDTO {

    @Schema(example = "Van An")
    private String firstName;

    @Schema(example = "Nguyen")
    private String lastName;

    public CustomerNameDTO() {
    }

    public CustomerNameDTO(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
