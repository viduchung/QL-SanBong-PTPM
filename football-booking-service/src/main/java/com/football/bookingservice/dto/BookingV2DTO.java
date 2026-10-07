package com.football.bookingservice.dto;

import com.football.bookingservice.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Mô hình dữ liệu đặt sân phiên bản API v2 (tiến hóa trường customer)")
public class BookingV2DTO {

    private Long id;
    private CustomerNameDTO customer;
    private String phone;
    private String fieldName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double price;
    private BookingStatus status;
    private String apiVersion = "v2.0";

    public BookingV2DTO() {
    }

    public BookingV2DTO(Long id, CustomerNameDTO customer, String phone, String fieldName,
                        LocalDate bookingDate, LocalTime startTime, LocalTime endTime,
                        Double price, BookingStatus status) {
        this.id = id;
        this.customer = customer;
        this.phone = phone;
        this.fieldName = fieldName;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomerNameDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerNameDTO customer) {
        this.customer = customer;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}
