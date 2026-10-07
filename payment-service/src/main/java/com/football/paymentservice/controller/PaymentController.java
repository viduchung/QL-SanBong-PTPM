package com.football.paymentservice.controller;

import com.football.paymentservice.dto.PaymentRequest;
import com.football.paymentservice.dto.PaymentResponse;
import com.football.paymentservice.exception.ResourceNotFoundException;
import com.football.paymentservice.model.Payment;
import com.football.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Welcome to Payment Service (Football System)");
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "service", "Payment Service",
                "version", "1.0",
                "port", 8082,
                "role", "Service Consumer (Calls Booking Service at 8081)",
                "status", "UP"
        );
    }

    @GetMapping
    public List<Payment> getAll() {
        return paymentService.findAll();
    }

    @GetMapping("/{id}")
    public Payment getById(@PathVariable Long id) {
        return paymentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán id=" + id));
    }

    // API trọng tâm Buổi 2: Tạo thanh toán có xác minh liên dịch vụ (Service-to-Service)
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(201).body(response);
    }

    // Thử thách mở rộng Bài 5: Fallback khi Booking Service không khả dụng
    @PostMapping("/fallback")
    public ResponseEntity<PaymentResponse> createPaymentFallback(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPaymentFallback(request);
        return ResponseEntity.status(202).body(response);
    }
}
