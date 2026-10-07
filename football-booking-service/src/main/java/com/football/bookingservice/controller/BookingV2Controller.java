package com.football.bookingservice.controller;

import com.football.bookingservice.dto.BookingV2DTO;
import com.football.bookingservice.exception.ResourceNotFoundException;
import com.football.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/bookings")
@Tag(name = "Booking Resource (v2 - Evolution Demo)",
     description = "Minh họa API Versioning và sự tiến hóa schema (Backward Compatibility)")
public class BookingV2Controller {

    private final BookingService bookingService;

    public BookingV2Controller(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết đặt sân phiên bản v2",
               description = "Phiên bản v2 tiến hóa trường customerName thành đối tượng lồng nhau { firstName, lastName }")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy dữ liệu v2 thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lịch đặt sân")
    })
    public BookingV2DTO getByIdV2(@PathVariable Long id) {
        return bookingService.findByIdV2(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch đặt sân với id=" + id));
    }
}
