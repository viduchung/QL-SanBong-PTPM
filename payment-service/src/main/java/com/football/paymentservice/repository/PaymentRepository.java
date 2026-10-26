package com.football.paymentservice.repository;

import com.football.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Tìm kiếm các giao dịch thanh toán theo mã đặt sân (Logical ID)
    List<Payment> findByBookingId(Long bookingId);

    // Tra cứu giao dịch theo mã giao dịch duy nhất
    Optional<Payment> findByTransactionCode(String transactionCode);
}
