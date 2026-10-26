package com.football.paymentservice.client;

import com.football.paymentservice.dto.BookingDTO;
import com.football.paymentservice.exception.GatewayTimeoutException;
import com.football.paymentservice.exception.ResourceNotFoundException;
import com.football.paymentservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;

@Component
public class BookingClient {

    private final RestClient bookingRestClient;
    private final String internalServiceKey;

    public BookingClient(
            RestClient bookingRestClient,
            @org.springframework.beans.factory.annotation.Value("${internal.service.key:FootballInternalSecretServiceKey2026}") String internalServiceKey) {
        this.bookingRestClient = bookingRestClient;
        this.internalServiceKey = internalServiceKey;
    }

    public BookingDTO getBookingById(Long bookingId) {
        try {
            return bookingRestClient.get()
                    .uri("/api/bookings/{id}", bookingId)
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ResourceNotFoundException("Không tìm thấy lịch đặt sân id=" + bookingId + " để thanh toán");
                    })
                    .body(BookingDTO.class);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                throw new GatewayTimeoutException("Booking Service phản hồi quá thời gian quy định (Timeout > 3s)");
            }
            throw new ServiceUnavailableException("Không thể kết nối tới Booking Service tại http://localhost:8081 (Dịch vụ đang tắt hoặc mất kết nối)");
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Không tìm thấy lịch đặt sân id=" + bookingId + " để thanh toán");
            }
            throw new RuntimeException("Lỗi giao tiếp từ Booking Service: " + ex.getMessage());
        }
    }
}
