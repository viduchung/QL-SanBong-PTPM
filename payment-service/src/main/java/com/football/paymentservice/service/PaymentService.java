package com.football.paymentservice.service;

import com.football.paymentservice.client.BookingClient;
import com.football.paymentservice.dto.BookingDTO;
import com.football.paymentservice.dto.PaymentRequest;
import com.football.paymentservice.dto.PaymentResponse;
import com.football.paymentservice.model.Payment;
import com.football.paymentservice.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentService {

    private final List<Payment> payments = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final BookingClient bookingClient;

    public PaymentService(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    public List<Payment> findAll() {
        return payments;
    }

    public Optional<Payment> findById(Long id) {
        return payments.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    // Xử lý thanh toán đồng bộ (Synchronous Service-to-Service call)
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Gọi sang Booking Service (Port 8081) để xác minh lịch đặt sân
        BookingDTO booking = bookingClient.getBookingById(request.getBookingId());

        // 2. Tạo bản ghi thanh toán thành công
        Payment payment = new Payment(
                idGenerator.incrementAndGet(),
                request.getBookingId(),
                request.getAmount(),
                request.getPaymentMethod(),
                PaymentStatus.SUCCESS,
                "TXN_" + System.currentTimeMillis(),
                LocalDateTime.now(),
                request.getNote() != null ? request.getNote() : "Thanh toán sân " + booking.getFieldName()
        );

        payments.add(payment);

        return new PaymentResponse(
                payment,
                booking,
                "Thanh toán thành công! Lịch đặt sân đã được xác minh qua Booking Service."
        );
    }

    // Thử thách mở rộng: Cơ chế Fallback khi Booking Service gián đoạn
    public PaymentResponse processPaymentFallback(PaymentRequest request) {
        Payment payment = new Payment(
                idGenerator.incrementAndGet(),
                request.getBookingId(),
                request.getAmount(),
                request.getPaymentMethod(),
                PaymentStatus.PENDING,
                "TXN_FALLBACK_" + System.currentTimeMillis(),
                LocalDateTime.now(),
                "Thanh toán tạm ghi nhận (Booking Service ngoại tuyến, chờ đối soát)"
        );

        payments.add(payment);

        return new PaymentResponse(
                payment,
                null,
                "Booking Service hiện chưa phản hồi. Giao dịch đã được lưu tạm ở trạng thái PENDING để xử lý sau."
        );
    }
}
