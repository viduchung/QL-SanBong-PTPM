package com.football.bookingservice.controller;

import com.football.bookingservice.dto.*;
import com.football.bookingservice.exception.ResourceNotFoundException;
import com.football.bookingservice.model.Booking;
import com.football.bookingservice.model.BookingStatus;
import com.football.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/bookings", "/api/bookings"})
@Tag(name = "Booking Resource (v1)", description = "RESTful API chuẩn hóa quản lý lịch đặt sân bóng đá")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/hello")
    @Operation(summary = "Lời chào dịch vụ", description = "Kiểm tra kết nối tới Booking Service")
    public Map<String, String> hello() {
        return Map.of("message", "Welcome to Football Booking Service (RESTful API v1)");
    }

    @GetMapping("/info")
    @Operation(summary = "Thông tin dịch vụ", description = "Xem phiên bản và trạng thái hoạt động của Service")
    public Map<String, Object> info() {
        return Map.of(
                "service", "Booking Service",
                "version", "v1.0 & v2.0",
                "apiStandard", "RESTful Maturity Level 2+",
                "documentation", "/swagger-ui/index.html",
                "status", "UP"
        );
    }

    // Giai đoạn 4: API Truy vấn kết hợp Search, Filter, Sort, Pagination
    @GetMapping
    @Operation(summary = "Truy vấn danh sách đặt sân (Search, Filter, Sort, Pagination)",
               description = "Tìm kiếm theo tên, lọc theo trạng thái, sắp xếp và phân trang trả về đầy đủ metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Truy vấn thành công trả về dữ liệu phân trang")
    })
    public PageResponse<Booking> getBookings(
            @Parameter(description = "Từ khóa tìm kiếm theo tên khách hàng")
            @RequestParam(required = false) String name,

            @Parameter(description = "Lọc theo trạng thái: PENDING, CONFIRMED, PAID, CANCELLED")
            @RequestParam(required = false) BookingStatus status,

            @Parameter(description = "Sắp xếp theo định dạng: field,asc hoặc field,desc (VD: price,desc; customerName,asc)")
            @RequestParam(defaultValue = "id,asc") String sort,

            @Parameter(description = "Chỉ số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Kích thước mỗi trang")
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookingService.searchFilterSortPaged(name, status, sort, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết đặt sân theo ID", description = "Truy vấn một bản ghi đặt sân cụ thể")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy bản ghi"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lịch đặt sân với ID tương ứng")
    })
    public Booking getById(@PathVariable Long id) {
        return bookingService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch đặt sân với id=" + id));
    }

    @PostMapping
    @Operation(summary = "Tạo mới lịch đặt sân", description = "Tiếp nhận dữ liệu, kiểm tra Bean Validation và trả về mã 201 Created")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo mới thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ vi phạm quy tắc validation")
    })
    public ResponseEntity<Booking> create(@Valid @RequestBody BookingRequest request) {
        Booking newBooking = new Booking(
                null,
                request.getCustomerName(),
                request.getPhone(),
                request.getFieldName(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPrice(),
                request.getStatus()
        );
        Booking created = bookingService.create(newBooking);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật toàn bộ thông tin (PUT)", description = "Thay thế toàn bộ dữ liệu của lịch đặt sân bằng dữ liệu mới")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu cập nhật không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi để cập nhật")
    })
    public Booking update(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        Booking data = new Booking(
                id,
                request.getCustomerName(),
                request.getPhone(),
                request.getFieldName(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPrice(),
                request.getStatus()
        );
        return bookingService.update(id, data)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch đặt sân với id=" + id));
    }

    // Giai đoạn 1 & 2: Cập nhật một phần (PATCH)
    @PatchMapping("/{id}")
    @Operation(summary = "Cập nhật một phần (PATCH)", description = "Chỉ cập nhật những trường được truyền trong body (không bắt buộc nhập lại toàn bộ)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật một phần thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu patch không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi")
    })
    public Booking patch(@PathVariable Long id, @Valid @RequestBody BookingPatchRequest request) {
        return bookingService.patch(id, request)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch đặt sân với id=" + id));
    }

    // Giai đoạn 2: Chuẩn hóa DELETE trả về 204 No Content
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa lịch đặt sân", description = "Xóa bản ghi theo ID và trả về mã chuẩn HTTP 204 No Content")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công, không có nội dung trả về"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi để xóa")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = bookingService.delete(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Không tìm thấy lịch đặt sân với id=" + id);
        }
        return ResponseEntity.noContent().build();
    }

    // Challenge 1: Nested Resource (GET /api/v1/bookings/{id}/payments)
    @GetMapping("/{id}/payments")
    @Operation(summary = "Lấy lịch sử thanh toán của lịch đặt (Nested Resource)",
               description = "Mô hình tài nguyên lồng nhau trong REST: biểu diễn mối quan hệ 1-N giữa Booking và Payments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thanh toán thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lịch đặt sân")
    })
    public List<PaymentNestedDTO> getPaymentsForBooking(@PathVariable Long id) {
        bookingService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch đặt sân với id=" + id));
        return bookingService.findPaymentsByBookingId(id);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Báo cáo thống kê doanh thu và số lượng", description = "Tổng hợp số đơn theo từng trạng thái và tổng doanh thu thu được")
    public StatisticsResponse statistics() {
        return bookingService.statistics();
    }
}