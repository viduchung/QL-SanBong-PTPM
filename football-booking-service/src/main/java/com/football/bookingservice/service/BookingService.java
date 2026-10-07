package com.football.bookingservice.service;

import com.football.bookingservice.dto.*;
import com.football.bookingservice.model.Booking;
import com.football.bookingservice.model.BookingStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final List<Booking> bookings = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public BookingService() {
        create(new Booking(null, "Nguyen Van An", "0901234567", "San so 1",
                LocalDate.of(2026, 10, 10), LocalTime.of(18, 0), LocalTime.of(19, 30),
                300000.0, BookingStatus.CONFIRMED));
        create(new Booking(null, "Tran Thi Binh", "0912345678", "San so 2",
                LocalDate.of(2026, 10, 11), LocalTime.of(20, 0), LocalTime.of(21, 30),
                350000.0, BookingStatus.PENDING));
        create(new Booking(null, "Le Van Cuong", "0923456789", "San so 1",
                LocalDate.of(2026, 10, 12), LocalTime.of(17, 30), LocalTime.of(19, 0),
                280000.0, BookingStatus.PAID));
        create(new Booking(null, "Pham Minh Duc", "0934567890", "San so 3",
                LocalDate.of(2026, 10, 13), LocalTime.of(19, 0), LocalTime.of(20, 30),
                400000.0, BookingStatus.CONFIRMED));
        create(new Booking(null, "Hoang Thu Ha", "0945678901", "San so 2",
                LocalDate.of(2026, 10, 14), LocalTime.of(16, 0), LocalTime.of(17, 30),
                250000.0, BookingStatus.CANCELLED));
        create(new Booking(null, "Do Van Hung", "0956789012", "San so 1",
                LocalDate.of(2026, 10, 15), LocalTime.of(20, 30), LocalTime.of(22, 0),
                450000.0, BookingStatus.PAID));
    }

    public List<Booking> findAll() {
        return bookings;
    }

    public Optional<Booking> findById(Long id) {
        return bookings.stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    public Booking create(Booking booking) {
        booking.setId(idGenerator.incrementAndGet());
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING);
        }
        bookings.add(booking);
        return booking;
    }

    // PUT: Cập nhật toàn bộ thông tin
    public Optional<Booking> update(Long id, Booking data) {
        return findById(id).map(existing -> {
            existing.setCustomerName(data.getCustomerName());
            existing.setPhone(data.getPhone());
            existing.setFieldName(data.getFieldName());
            existing.setBookingDate(data.getBookingDate());
            existing.setStartTime(data.getStartTime());
            existing.setEndTime(data.getEndTime());
            existing.setPrice(data.getPrice());
            if (data.getStatus() != null) {
                existing.setStatus(data.getStatus());
            }
            return existing;
        });
    }

    // PATCH: Cập nhật một phần thông tin (Buổi 3 - Giai đoạn 1)
    public Optional<Booking> patch(Long id, BookingPatchRequest patchData) {
        return findById(id).map(existing -> {
            if (patchData.getCustomerName() != null) {
                existing.setCustomerName(patchData.getCustomerName());
            }
            if (patchData.getPhone() != null) {
                existing.setPhone(patchData.getPhone());
            }
            if (patchData.getFieldName() != null) {
                existing.setFieldName(patchData.getFieldName());
            }
            if (patchData.getBookingDate() != null) {
                existing.setBookingDate(patchData.getBookingDate());
            }
            if (patchData.getStartTime() != null) {
                existing.setStartTime(patchData.getStartTime());
            }
            if (patchData.getEndTime() != null) {
                existing.setEndTime(patchData.getEndTime());
            }
            if (patchData.getPrice() != null) {
                existing.setPrice(patchData.getPrice());
            }
            if (patchData.getStatus() != null) {
                existing.setStatus(patchData.getStatus());
            }
            return existing;
        });
    }

    public boolean delete(Long id) {
        return bookings.removeIf(b -> b.getId().equals(id));
    }

    // Giai đoạn 4: Search, Filter, Sort, Pagination kết hợp
    public PageResponse<Booking> searchFilterSortPaged(String name, BookingStatus status,
                                                      String sort, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        // 1. Filter theo tên và trạng thái
        String keyword = (name == null) ? "" : name.trim().toLowerCase();
        List<Booking> filtered = bookings.stream()
                .filter(b -> keyword.isEmpty() || (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(keyword)))
                .filter(b -> status == null || b.getStatus() == status)
                .collect(Collectors.toList());

        // 2. Sorting
        Comparator<Booking> comparator = Comparator.comparing(Booking::getId);
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            boolean desc = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc");

            switch (field) {
                case "customerName":
                    comparator = Comparator.comparing(b -> b.getCustomerName() != null ? b.getCustomerName() : "");
                    break;
                case "price":
                    comparator = Comparator.comparing(b -> b.getPrice() != null ? b.getPrice() : 0.0);
                    break;
                case "bookingDate":
                    comparator = Comparator.comparing(b -> b.getBookingDate() != null ? b.getBookingDate() : LocalDate.MIN);
                    break;
                case "status":
                    comparator = Comparator.comparing(b -> b.getStatus() != null ? b.getStatus().name() : "");
                    break;
                default:
                    comparator = Comparator.comparing(Booking::getId);
            }
            if (desc) {
                comparator = comparator.reversed();
            }
        }
        filtered.sort(comparator);

        // 3. Pagination
        long totalElements = filtered.size();
        int from = page * size;
        List<Booking> pagedContent;
        if (from >= totalElements) {
            pagedContent = Collections.emptyList();
        } else {
            int to = Math.min(from + size, (int) totalElements);
            pagedContent = filtered.subList(from, to);
        }

        return new PageResponse<>(pagedContent, page, size, totalElements);
    }

    // Giai đoạn 5: API Versioning (v2) - Tách tên thành firstName, lastName
    public Optional<BookingV2DTO> findByIdV2(Long id) {
        return findById(id).map(b -> {
            String fullName = b.getCustomerName() != null ? b.getCustomerName() : "";
            String[] parts = fullName.trim().split("\\s+");
            String lastName = parts.length > 0 ? parts[0] : "";
            String firstName = parts.length > 1 ? String.join(" ", Arrays.copyOfRange(parts, 1, parts.length)) : "";

            CustomerNameDTO customerNameDTO = new CustomerNameDTO(firstName, lastName);
            return new BookingV2DTO(
                    b.getId(),
                    customerNameDTO,
                    b.getPhone(),
                    b.getFieldName(),
                    b.getBookingDate(),
                    b.getStartTime(),
                    b.getEndTime(),
                    b.getPrice(),
                    b.getStatus()
            );
        });
    }

    // Thử thách 1: Nested Resource (GET /api/v1/bookings/{id}/payments)
    public List<PaymentNestedDTO> findPaymentsByBookingId(Long bookingId) {
        return findById(bookingId).map(b -> {
            List<PaymentNestedDTO> list = new ArrayList<>();
            if (b.getStatus() == BookingStatus.PAID) {
                list.add(new PaymentNestedDTO(
                        101L,
                        bookingId,
                        b.getPrice(),
                        "VNPAY",
                        "SUCCESS",
                        "TXN_" + bookingId + "_FULL",
                        LocalDateTime.now().minusHours(2)
                ));
            } else if (b.getStatus() == BookingStatus.CONFIRMED) {
                list.add(new PaymentNestedDTO(
                        102L,
                        bookingId,
                        100000.0,
                        "MOMO",
                        "SUCCESS",
                        "TXN_" + bookingId + "_DEPOSIT",
                        LocalDateTime.now().minusDays(1)
                ));
            }
            return list;
        }).orElse(Collections.emptyList());
    }

    public StatisticsResponse statistics() {
        long total = bookings.size();
        double revenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PAID)
                .mapToDouble(b -> b.getPrice() == null ? 0 : b.getPrice())
                .sum();
        return new StatisticsResponse(
                total,
                count(BookingStatus.PENDING),
                count(BookingStatus.CONFIRMED),
                count(BookingStatus.PAID),
                count(BookingStatus.CANCELLED),
                revenue);
    }

    private long count(BookingStatus status) {
        return bookings.stream().filter(b -> b.getStatus() == status).count();
    }
}