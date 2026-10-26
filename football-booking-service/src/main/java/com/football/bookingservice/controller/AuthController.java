package com.football.bookingservice.controller;

import com.football.bookingservice.dto.AuthResponse;
import com.football.bookingservice.dto.LoginRequest;
import com.football.bookingservice.dto.RegisterRequest;
import com.football.bookingservice.dto.UserProfileResponse;
import com.football.bookingservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
@Tag(name = "Authentication Resource", description = "Dịch vụ xác thực và cấp phát JWT Token (Buổi thực hành 5)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Kiểm tra tài khoản, mật khẩu bằng BCrypt và cấp JWT Access Token (15 phút - 24 giờ)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công, trả về JWT Access Token"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đăng nhập không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Sai tên đăng nhập hoặc mật khẩu")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo tài khoản mới, mã hóa mật khẩu bằng BCrypt và cấp luôn JWT Token")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @ApiResponse(responseCode = "400", description = "Tên đăng nhập hoặc số điện thoại đã tồn tại / Dữ liệu không hợp lệ")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập", description = "Yêu cầu gửi Bearer JWT Token ở Header", security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về thông tin chi tiết của người dùng hiện tại"),
            @ApiResponse(responseCode = "401", description = "Chưa gửi token hoặc token hết hạn")
    })
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileResponse profile = authService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }
}
