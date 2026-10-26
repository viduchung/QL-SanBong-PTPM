package com.football.paymentservice.service;

import com.football.paymentservice.client.BookingClient;
import com.football.paymentservice.dto.BookingDTO;
import com.football.paymentservice.dto.PaymentRequest;
import com.football.paymentservice.dto.PaymentResponse;
import com.football.paymentservice.model.Payment;
import com.football.paymentservice.model.PaymentMethod;
import com.football.paymentservice.model.PaymentStatus;
import com.football.paymentservice.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    /*
    // =====================================================================================
    // [CODE CŨ BUỔI 2: LƯU TRỮ TẠM TRÊN BỘ NHỚ RAM (IN-MEMORY LIST)]
    // Giảng viên yêu cầu comment lại để đối chiếu sự tiến hóa sang CSDL quan hệ MySQL
    // =====================================================================================
    private final List<Payment> payments = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();
    // =====================================================================================
    */

    // =====================================================================================
    // [CODE MỚI BUỔI 4: PERSISTENCE LAYER VỚI SPRING DATA JPA & MYSQL DATABASE]
    // =====================================================================================
    private final PaymentRepository paymentRepository;
    private final BookingClient bookingClient;

    public PaymentService(PaymentRepository paymentRepository, BookingClient bookingClient) {
        this.paymentRepository = paymentRepository;
        this.bookingClient = bookingClient;
    }

    // Khởi tạo dữ liệu mẫu vào MySQL nếu bảng rỗng
    @PostConstruct
    @Transactional
    public void initSeedData() {
        if (paymentRepository.count() == 0) {
            paymentRepository.saveAll(List.of(
                    new Payment(null, 3L, 280000.0, PaymentMethod.VNPAY, PaymentStatus.SUCCESS,
                            "TXN_1728300000001", LocalDateTime.of(2026, 10, 12, 17, 0),
                            "Thanh toán tiền sân Le Van Cuong qua VNPAY"),
                    new Payment(null, 6L, 450000.0, PaymentMethod.BANKING, PaymentStatus.SUCCESS,
                            "TXN_1728300000002", LocalDateTime.of(2026, 10, 15, 20, 0),
                            "Chuyển khoản thanh toán toàn phần Do Van Hung"),
                    new Payment(null, 1L, 100000.0, PaymentMethod.MOMO, PaymentStatus.SUCCESS,
                            "TXN_1728300000003", LocalDateTime.of(2026, 10, 10, 16, 30),
                            "Thanh toán cọc sân Nguyen Van An qua Momo")
            ));
        }
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    // Nghiệp vụ thanh toán đồng bộ (Service-to-Service) được quản lý trong Transaction
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Gọi sang Booking Service (Port 8081) để xác minh lịch đặt sân qua REST API
        BookingDTO booking = bookingClient.getBookingById(request.getBookingId());

        // 2. Tạo bản ghi thanh toán thành công và lưu vào MySQL Database (football_payment_db)
        Payment payment = new Payment(
                null,
                request.getBookingId(),
                request.getAmount(),
                request.getPaymentMethod(),
                PaymentStatus.SUCCESS,
                "TXN_" + System.currentTimeMillis(),
                LocalDateTime.now(),
                request.getNote() != null ? request.getNote() : "Thanh toán sân " + booking.getFieldName()
        );

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(
                savedPayment,
                booking,
                "Thanh toán thành công! Lịch đặt sân đã được xác minh qua Booking Service và lưu vào CSDL MySQL."
        );
    }

    // Thử thách mở rộng: Cơ chế Fallback khi Booking Service gián đoạn, lưu trạng thái PENDING
    @Transactional
    public PaymentResponse processPaymentFallback(PaymentRequest request) {
        Payment payment = new Payment(
                null,
                request.getBookingId(),
                request.getAmount(),
                request.getPaymentMethod(),
                PaymentStatus.PENDING,
                "TXN_FALLBACK_" + System.currentTimeMillis(),
                LocalDateTime.now(),
                "Thanh toán tạm ghi nhận vào MySQL (Booking Service ngoại tuyến, chờ đối soát)"
        );

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(
                savedPayment,
                null,
                "Booking Service hiện chưa phản hồi. Giao dịch đã được lưu tạm vào MySQL ở trạng thái PENDING để xử lý sau."
        );
    }
}
