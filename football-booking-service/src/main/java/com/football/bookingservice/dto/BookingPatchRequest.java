package com.football.bookingservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.football.bookingservice.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Yêu cầu cập nhật một phần (PATCH) lịch đặt sân")
public class BookingPatchRequest {

    @Size(min = 3, max = 100, message = "Tên khách hàng phải từ 3 đến 100 ký tự")
    @Schema(example = "Nguyen Van An", description = "Tên mới (nếu muốn đổi)")
    private String customerName;

    @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "Số điện thoại phải có 10 chữ số hợp lệ (VD: 0901234567)")
    @Schema(example = "0988888888", description = "Số điện thoại mới (nếu muốn đổi)")
    private String phone;

    @Schema(example = "San 7 nguoi so 2", description = "Tên sân mới (nếu muốn đổi)")
    private String fieldName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2026-10-20", description = "Ngày mới (nếu muốn đổi)")
    private LocalDate bookingDate;

    @JsonFormat(pattern = "HH:mm")
    @Schema(example = "19:00", description = "Giờ bắt đầu mới")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(example = "20:30", description = "Giờ kết thúc mới")
    private LocalTime endTime;

    @Positive(message = "Giá thuê sân phải lớn hơn 0")
    @Schema(example = "400000.0", description = "Giá mới")
    private Double price;

    @Schema(example = "CONFIRMED", description = "Trạng thái mới (PENDING, CONFIRMED, PAID, CANCELLED)")
    private BookingStatus status;

    public BookingPatchRequest() {
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
