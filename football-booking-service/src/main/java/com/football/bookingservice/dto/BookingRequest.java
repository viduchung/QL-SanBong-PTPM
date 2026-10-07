package com.football.bookingservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.football.bookingservice.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Yêu cầu tạo hoặc cập nhật toàn bộ lịch đặt sân")
public class BookingRequest {

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(min = 3, max = 100, message = "Tên khách hàng phải từ 3 đến 100 ký tự")
    @Schema(example = "Nguyen Van An", description = "Họ và tên khách hàng")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "Số điện thoại phải có 10 chữ số hợp lệ (VD: 0901234567)")
    @Schema(example = "0901234567", description = "Số điện thoại liên hệ")
    private String phone;

    @NotBlank(message = "Tên sân không được để trống")
    @Schema(example = "San 7 nguoi so 1", description = "Tên sân bóng")
    private String fieldName;

    @NotNull(message = "Ngày đặt sân không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2026-10-15", description = "Ngày đặt sân (yyyy-MM-dd)")
    private LocalDate bookingDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @JsonFormat(pattern = "HH:mm")
    @Schema(example = "18:00", description = "Giờ bắt đầu (HH:mm)")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    @JsonFormat(pattern = "HH:mm")
    @Schema(example = "19:30", description = "Giờ kết thúc (HH:mm)")
    private LocalTime endTime;

    @NotNull(message = "Giá thuê không được để trống")
    @Positive(message = "Giá thuê sân phải lớn hơn 0")
    @Schema(example = "350000.0", description = "Giá thuê tính bằng VNĐ")
    private Double price;

    @Schema(example = "PENDING", description = "Trạng thái đặt sân (PENDING, CONFIRMED, PAID, CANCELLED)")
    private BookingStatus status;

    public BookingRequest() {
    }

    public BookingRequest(String customerName, String phone, String fieldName, LocalDate bookingDate,
                          LocalTime startTime, LocalTime endTime, Double price, BookingStatus status) {
        this.customerName = customerName;
        this.phone = phone;
        this.fieldName = fieldName;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
}
