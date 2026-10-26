# KIẾN TRÚC VÀ THIẾT KẾ BẢO MẬT REST API (API SECURITY DESIGN)
**Hệ thống:** Quản lý đặt lịch sân bóng đá (Football Booking System)  
**Công nghệ:** Spring Boot 3.2.5, Spring Security 6, JJWT 0.12.5, BCrypt, MySQL  

---

## 1. Tổng quan kiến trúc bảo mật (Security Architecture)

Hệ thống áp dụng mô hình **Stateless JWT Authentication & Role-Based Authorization** kết hợp với **Resource Ownership Verification** tại Service Layer.

```
+-----------------------------------------------------------------------------------+
|                                  CLIENT (POSTMAN)                                 |
+-----------------------------------------------------------------------------------+
            |                                           |
    1. POST /auth/login                         2. Request có Header
    (username, password)                        Authorization: Bearer <JWT>
            |                                           |
            v                                           v
+-----------------------+                   +---------------------------------------+
|     Auth Service      |                   |       Spring Security Filter Chain    |
| (Authentication Mgr)  |                   +---------------------------------------+
+-----------------------+                                       |
            |                                                   v
    - So khớp BCrypt Hash                       +-----------------------------------+
    - Cấp JWT có thời hạn                       |      JwtAuthenticationFilter      |
            |                                   | - Giải mã & kiểm tra chữ ký HS256 |
            v                                   | - Nạp UserDetails vào Context     |
    Response Access Token                       +-----------------------------------+
    {"accessToken": "...",                                      |
     "expiresIn": 86400}                                        v
                                                +-----------------------------------+
                                                |     URL Matching & RBAC Check     |
                                                |  (permitAll vs authenticated)     |
                                                +-----------------------------------+
                                                                |
                                                                v
                                                +-----------------------------------+
                                                |        Controller Layer           |
                                                |   (@AuthenticationPrincipal)      |
                                                +-----------------------------------+
                                                                |
                                                                v
                                                +-----------------------------------+
                                                |   Resource Ownership Validation   |
                                                | (booking.username == currentUser) |
                                                +-----------------------------------+
                                                                |
                                                +---------------+---------------+
                                                |                               |
                                           [Thỏa mãn]                    [Vi phạm]
                                                |                               |
                                                v                               v
                                          Xử lý DB MySQL                403 FORBIDDEN
```

---

## 2. Luồng xác thực đăng nhập (Login & Token Generation Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Client as Người dùng / Postman
    participant AC as AuthController
    participant AS as AuthService
    participant AM as AuthenticationManager
    participant DB as MySQL (users table)
    participant TP as JwtTokenProvider

    Client->>AC: POST /api/v1/auth/login {username, password}
    AC->>AS: login(LoginRequest)
    AS->>AM: authenticate(UsernamePasswordAuthenticationToken)
    AM->>DB: Truy vấn user theo username
    DB-->>AM: Trả về password_hash (BCrypt)
    AM->>AM: BCrypt.checkpw(rawPassword, password_hash)
    alt Mật khẩu không đúng
        AM-->>AS: BadCredentialsException
        AS-->>AC: Ném ngoại lệ
        AC-->>Client: 401 Unauthorized {"error": "Unauthorized", "message": "Sai tên đăng nhập hoặc mật khẩu"}
    else Mật khẩu chính xác
        AM-->>AS: Authentication (Principal)
        AS->>TP: generateToken(Authentication)
        TP->>TP: Ký HMAC-SHA256 (Subject, Role, FullName, Exp)
        TP-->>AS: JWT String
        AS-->>AC: AuthResponse {accessToken, tokenType, expiresIn, username, role}
        AC-->>Client: 200 OK (Kèm Access Token)
    end
```

---

## 3. Luồng kiểm tra quyền sở hữu tài nguyên (Resource Ownership Check)

```mermaid
sequenceDiagram
    autonumber
    actor CustomerA as Khách hàng A (customer01)
    participant JF as JwtAuthenticationFilter
    participant BC as BookingController
    participant BS as BookingService
    participant DB as MySQL (bookings table)

    CustomerA->>JF: GET /api/v1/bookings/2 (Đơn của customer02)<br/>Header: Authorization: Bearer <Token_customer01>
    JF->>JF: Validate JWT & trích xuất username = "customer01", role = "ROLE_CUSTOMER"
    JF->>BC: Chuyển tiếp request với SecurityContext
    BC->>BS: findById(2)
    BS->>DB: SELECT * FROM bookings WHERE id = 2
    DB-->>BS: Booking {id: 2, username: "customer02", ...}
    BC->>BS: verifyBookingOwnership(booking, userDetails)
    BS->>BS: Kiểm tra: role có phải ADMIN/OWNER không? -> Không.<br/>booking.username ("customer02") == currentUser ("customer01")? -> KHÔNG!
    BS-->>BC: Ném AccessDeniedException("Vi phạm kiểm tra quyền sở hữu tài nguyên")
    BC-->>CustomerA: 403 Forbidden {<br/>  "status": 403,<br/>  "error": "Forbidden",<br/>  "message": "Bị từ chối truy cập (403 Forbidden): Bạn không có quyền truy cập lịch đặt sân của khách hàng khác"<br/>}
```

---

## 4. Bảo mật giao tiếp giữa các dịch vụ (Inter-Service Security)

Trong kiến trúc Microservices, dịch vụ `payment-service` cần gọi sang `booking-service` để kiểm tra sự tồn tại của lịch đặt sân trước khi cho phép thanh toán.

### 4.1. Giải pháp Machine-to-Machine: Service Credential Key
- **Header xác thực:** `X-Internal-Service-Key`
- **Khóa bí mật:** `FootballInternalSecretServiceKey2026` (được cấu hình đồng bộ qua biến môi trường `INTERNAL_SERVICE_KEY` trong file `.env` tổng).
- **Cơ chế hoạt động:**
  1. `payment-service` cấu hình `BookingClient` tự động thêm Header `X-Internal-Service-Key` vào mỗi REST request gửi sang `booking-service`.
  2. Tại `booking-service`, bộ lọc `JwtAuthenticationFilter` kiểm tra nếu Header `X-Internal-Service-Key` khớp với cấu hình hệ thống, hệ thống lập tức cấp quyền nội bộ `ROLE_INTERNAL_SERVICE` và `ROLE_ADMIN` trong `SecurityContext`.
  3. Yêu cầu nội bộ được thực thi an toàn mà không cần truyền mật khẩu người dùng hoặc giả mạo token.

```mermaid
sequenceDiagram
    autonumber
    participant PS as Payment Service (Port 8082)
    participant BC as BookingClient (RestClient)
    participant BS as Booking Service (Port 8081)
    participant JF as JwtAuthenticationFilter

    PS->>BC: getBookingById(bookingId)
    BC->>BS: GET /api/bookings/{id}<br/>Header: X-Internal-Service-Key: FootballInternalSecret...
    BS->>JF: Đón bắt request
    JF->>JF: So sánh Header với internal.service.key
    alt Khớp khóa bí mật nội bộ
        JF->>JF: Gán quyền ROLE_INTERNAL_SERVICE + ROLE_ADMIN
        JF->>BS: Thực thi truy vấn dữ liệu đặt sân
        BS-->>PS: 200 OK (BookingDTO)
    else Sai hoặc thiếu khóa nội bộ
        JF-->>PS: 401 Unauthorized (Từ chối gọi nội bộ trái phép)
    end
```

---

## 5. Chuẩn hóa cấu trúc phản hồi lỗi (Standard Error Format)

Tất cả các lỗi xác thực (401), từ chối truy cập (403), lỗi nghiệp vụ (400), và không tìm thấy (404) đều được định dạng JSON đồng nhất:

```json
{
  "timestamp": "2026-10-26T10:30:00.123456",
  "status": 403,
  "error": "Forbidden",
  "message": "Bị từ chối truy cập (403 Forbidden): Bạn không có quyền truy cập lịch đặt sân của khách hàng khác (Vi phạm kiểm tra quyền sở hữu tài nguyên)",
  "path": "/api/v1/bookings/2"
}
```
