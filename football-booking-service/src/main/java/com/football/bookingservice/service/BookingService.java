package com.football.bookingservice.service;

import com.football.bookingservice.dto.*;
import com.football.bookingservice.model.Booking;
import com.football.bookingservice.model.BookingStatus;
import com.football.bookingservice.repository.BookingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class BookingService {

    /*
    // =====================================================================================
    // [CODE CŨ BUỔI 1 - BUỔI 3: LƯU TRỮ TẠM TRÊN BỘ NHỚ RAM (IN-MEMORY LIST)]
    // Giảng viên yêu cầu comment lại để đối chiếu sự tiến hóa kiến trúc từ RAM sang Database
    // =====================================================================================
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
    // =====================================================================================
    */

    // =====================================================================================
    // [CODE MỚI BUỔI 4: PERSISTENCE LAYER VỚI SPRING DATA JPA & MYSQL DATABASE]
    // =====================================================================================
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Tự động khởi tạo dữ liệu mẫu vào MySQL nếu bảng rỗng
    @PostConstruct
    @Transactional
    public void initSeedData() {
        if (bookingRepository.count() == 0) {
            bookingRepository.saveAll(List.of(
                    new Booking(null, "Nguyen Van An", "0901234567", "San 7 nguoi so 1",
                            LocalDate.of(2026, 10, 10), LocalTime.of(18, 0), LocalTime.of(19, 30),
                            300000.0, BookingStatus.CONFIRMED, "customer01"),
                    new Booking(null, "Tran Thi Binh", "0912345678", "San 7 nguoi so 2",
                            LocalDate.of(2026, 10, 11), LocalTime.of(20, 0), LocalTime.of(21, 30),
                            350000.0, BookingStatus.PENDING, "customer02"),
                    new Booking(null, "Le Van Cuong", "0923456789", "San 7 nguoi so 1",
                            LocalDate.of(2026, 10, 12), LocalTime.of(17, 30), LocalTime.of(19, 0),
                            280000.0, BookingStatus.PAID, "customer01"),
                    new Booking(null, "Pham Minh Duc", "0934567890", "San 11 nguoi VIP",
                            LocalDate.of(2026, 10, 13), LocalTime.of(19, 0), LocalTime.of(20, 30),
                            400000.0, BookingStatus.CONFIRMED, "customer02"),
                    new Booking(null, "Hoang Thu Ha", "0945678901", "San 5 nguoi so 2",
                            LocalDate.of(2026, 10, 14), LocalTime.of(16, 0), LocalTime.of(17, 30),
                            250000.0, BookingStatus.CANCELLED, "customer01"),
                    new Booking(null, "Do Van Hung", "0956789012", "San 7 nguoi so 1",
                            LocalDate.of(2026, 10, 15), LocalTime.of(20, 30), LocalTime.of(22, 0),
                            450000.0, BookingStatus.PAID, "customer02")
            ));
        }
    }

    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    // Nghiệp vụ tạo mới được quản lý trong Transaction
    @Transactional
    public Booking create(Booking booking) {
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING);
        }
        return bookingRepository.save(booking);
    }

    // PUT: Cập nhật toàn bộ thông tin trong Transaction
    @Transactional
    public Optional<Booking> update(Long id, Booking data) {
        return bookingRepository.findById(id).map(existing -> {
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
            return bookingRepository.save(existing);
        });
    }

    // PATCH: Cập nhật một phần thông tin trong Transaction
    @Transactional
    public Optional<Booking> patch(Long id, BookingPatchRequest patchData) {
        return bookingRepository.findById(id).map(existing -> {
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
            return bookingRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Giai đoạn 4: Search, Filter, Sort, Pagination kết hợp trực tiếp qua Spring Data JPA
    @Transactional(readOnly = true)
    public PageResponse<Booking> searchFilterSortPaged(String name, BookingStatus status,
                                                      String sort, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        // Xử lý Dynamic Sort của Spring Data
        Sort sortObj = Sort.by(Sort.Direction.ASC, "id");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc"))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sortObj = Sort.by(direction, field);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<Booking> pageResult;

        boolean hasName = (name != null && !name.trim().isEmpty());
        boolean hasStatus = (status != null);

        if (hasName && hasStatus) {
            pageResult = bookingRepository.findByCustomerNameContainingIgnoreCaseAndStatus(name.trim(), status, pageable);
        } else if (hasName) {
            pageResult = bookingRepository.findByCustomerNameContainingIgnoreCase(name.trim(), pageable);
        } else if (hasStatus) {
            pageResult = bookingRepository.findByStatus(status, pageable);
        } else {
            pageResult = bookingRepository.findAll(pageable);
        }

        return new PageResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements()
        );
    }

    // Giai đoạn 5: API Versioning (v2)
    @Transactional(readOnly = true)
    public Optional<BookingV2DTO> findByIdV2(Long id) {
        return bookingRepository.findById(id).map(b -> {
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
    @Transactional(readOnly = true)
    public List<PaymentNestedDTO> findPaymentsByBookingId(Long bookingId) {
        return bookingRepository.findById(bookingId).map(b -> {
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

    @Transactional(readOnly = true)
    public StatisticsResponse statistics() {
        long total = bookingRepository.count();
        Double revenue = bookingRepository.sumPaidRevenue();
        return new StatisticsResponse(
                total,
                bookingRepository.countByStatus(BookingStatus.PENDING),
                bookingRepository.countByStatus(BookingStatus.CONFIRMED),
                bookingRepository.countByStatus(BookingStatus.PAID),
                bookingRepository.countByStatus(BookingStatus.CANCELLED),
                revenue != null ? revenue : 0.0);
    }

    // =====================================================================================
    // [BUỔI THỰC HÀNH 5: KIỂM TRA PHÂN QUYỀN SỞ HỮU TÀI NGUYÊN (RESOURCE OWNERSHIP)]
    // =====================================================================================
    public void verifyBookingOwnership(Booking booking, org.springframework.security.core.userdetails.UserDetails userDetails) {
        if (userDetails == null) {
            throw new org.springframework.security.access.AccessDeniedException("Yêu cầu đăng nhập để truy cập tài nguyên");
        }

        // Quyền quản trị và Chủ sân được phép xem mọi đơn đặt sân
        boolean isPrivileged = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_FIELD_OWNER"));
        if (isPrivileged) {
            return;
        }

        // Đối với Khách hàng (ROLE_CUSTOMER): CHỈ được xem đơn đặt sân do chính mình sở hữu
        String currentUsername = userDetails.getUsername();
        boolean isOwner = (booking.getUsername() != null && booking.getUsername().equalsIgnoreCase(currentUsername));

        if (!isOwner && userDetails instanceof com.football.bookingservice.security.CustomUserDetails customUserDetails) {
            if (booking.getPhone() != null && booking.getPhone().equals(customUserDetails.getPhone())) {
                isOwner = true;
            }
        }

        if (!isOwner) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bị từ chối truy cập (403 Forbidden): Bạn không có quyền truy cập lịch đặt sân của khách hàng khác (Vi phạm kiểm tra quyền sở hữu tài nguyên)");
        }
    }

    // Kiểm tra điều kiện nghiệp vụ kết hợp phân quyền khi hủy lịch đặt sân
    public void verifyBookingCancellation(Booking booking, org.springframework.security.core.userdetails.UserDetails userDetails) {
        // 1. Phải thỏa mãn kiểm tra quyền sở hữu trước
        verifyBookingOwnership(booking, userDetails);

        // 2. Kiểm tra điều kiện nghiệp vụ: Không cho phép hủy lịch đặt sân đã thanh toán (PAID)
        if (booking.getStatus() == BookingStatus.PAID) {
            throw new IllegalArgumentException("Không thể hủy lịch đặt sân đã thanh toán thành công (PAID). Vui lòng liên hệ ban quản lý để được hỗ trợ.");
        }
    }
}