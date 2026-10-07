package com.football.paymentservice.dto;

import com.football.paymentservice.model.Payment;

public class PaymentResponse {

    private Payment payment;
    private BookingDTO booking;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(Payment payment, BookingDTO booking, String message) {
        this.payment = payment;
        this.booking = booking;
        this.message = message;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public BookingDTO getBooking() {
        return booking;
    }

    public void setBooking(BookingDTO booking) {
        this.booking = booking;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
