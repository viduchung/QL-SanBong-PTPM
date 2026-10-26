package com.football.bookingservice.repository;

import com.football.bookingservice.model.Booking;
import com.football.bookingservice.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Tìm kiếm tương đối theo tên khách hàng
    Page<Booking> findByCustomerNameContainingIgnoreCase(String name, Pageable pageable);

    // Lọc theo trạng thái
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    // Kết hợp tìm kiếm theo tên và lọc theo trạng thái
    Page<Booking> findByCustomerNameContainingIgnoreCaseAndStatus(String name, BookingStatus status, Pageable pageable);

    // Thống kê số lượng theo trạng thái
    long countByStatus(BookingStatus status);

    // Thống kê tổng doanh thu từ các đơn đã thanh toán (PAID)
    @Query("SELECT COALESCE(SUM(b.price), 0.0) FROM Booking b WHERE b.status = com.football.bookingservice.model.BookingStatus.PAID")
    Double sumPaidRevenue();
}
