# BUỔI THỰC HÀNH 5: DỊCH VỤ XÁC THỰC VÀ BẢO MẬT
## Học phần: Phát triển phần mềm hướng dịch vụ (SOA)

* **Thời lượng trên lớp:** 150 phút | **Thời lượng tự học mở rộng:** 90–120 phút
* **Đối tượng:** Sinh viên năm 3
* **Hệ thống áp dụng:** Football Booking System (Quản lý đặt lịch sân bóng đá - Kiến trúc Microservices)
* **Nền tảng công nghệ:** Java 17, Spring Boot 3.2.5, Spring Security 6, JJWT (io.jsonwebtoken 0.12.5), BCrypt, MySQL 8.x, Swagger/OpenAPI 3, Postman

---

## 1. Vị trí của Buổi 5 trong chuỗi học phần

Sau khi hoàn thành Buổi 1–3 (Xây dựng và chuẩn hóa RESTful API) và Buổi 4 (Quản lý dữ liệu bền vững theo nguyên tắc Database-per-Service), toàn bộ các API của hệ thống vẫn ở trạng thái mở (ai cũng có thể gọi, ai cũng có thể sửa hoặc xóa bất kỳ dữ liệu nào). 

Buổi 5 thiết lập lớp phòng thủ thiết yếu cho toàn bộ hệ thống phân tán:
$$\text{Anonymous REST API} \longrightarrow \text{BCrypt Hashing} \longrightarrow \text{Stateless JWT Token} \longrightarrow \text{Role-Based Access Control (RBAC)} \longrightarrow \text{Resource Ownership Check}$$

```
+---------------------------------------------------------------------------------------------------+
|               HỆ THỐNG ĐẶT SÂN BÓNG ĐÁ - MÔ HÌNH XÁC THỰC & PHÂN QUYỀN TOÀN DIỆN                  |
+---------------------------------------------------------------------------------------------------+

   [Client / Postman]
           │
           │ 1. POST /api/v1/auth/login {username, password}
           ▼
   ┌────────────────────────────────────────────────────────┐
   │ Booking Service (Port 8081) - Authentication Engine    │
   │   - So khớp mật khẩu đã băm bằng BCrypt                │
   │   - Cấp phát Access Token (HMAC-SHA256, Expiration)    │
   └────────────────────────────────────────────────────────┘
           │
           │ 2. Trả về JWT Access Token: "eyJhbGciOiJIUzI1NiIsIn..."
           ▼
   [Client lưu trữ Bearer Token]
           │
           │ 3. Gửi Request có Header: "Authorization: Bearer <JWT>"
           ▼
   ┌────────────────────────────────────────────────────────────────────────────────────────┐
   │ Spring Security Filter Chain (JwtAuthenticationFilter)                                 │
   │   - Giải mã và xác thực chữ ký số của Token                                            │
   │   - Nạp thông tin định danh & Vai trò (Roles) vào SecurityContext                      │
   └────────────────────────────────────────────────────────────────────────────────────────┘
           │
           ├── [Chưa có Token hoặc Token hết hạn] ──────> 401 UNAUTHORIZED (AuthenticationEntryPoint)
           │
           ▼
   ┌────────────────────────────────────────────────────────────────────────────────────────┐
   │ Authorization & Resource Ownership Check (Service Layer)                               │
   │   - Kiểm tra vai trò: ROLE_CUSTOMER, ROLE_FIELD_OWNER, ROLE_ADMIN                      │
   │   - Kiểm tra quyền sở hữu: booking.username == currentUser.username?                   │
   └────────────────────────────────────────────────────────────────────────────────────────┘
           │
           ├── [Khách hàng A cố tình xem/hủy đơn của Khách hàng B] ──> 403 FORBIDDEN
           │
           ▼
   ┌────────────────────────────────────────────────────────┐
   │ Xử lý nghiệp vụ an toàn tại MySQL Database             │
   └────────────────────────────────────────────────────────┘
```

### 3 Actor (Vai trò) cốt lõi trong đề tài Quản lý sân bóng đá:
1. **`ROLE_CUSTOMER` (Người thuê sân / Cầu thủ):**
   - Đăng ký tài khoản, đăng nhập hệ thống.
   - Tra cứu danh sách lịch sân bóng đá công khai (Public).
   - Đặt lịch sân bóng mới (hệ thống tự động liên kết đơn với tài khoản đang đăng nhập).
   - Xem chi tiết và hủy **CHỈ đơn đặt sân của chính mình** (Resource Ownership).
   - Bị từ chối (403 Forbidden) nếu cố tình xem hoặc hủy đơn của khách hàng khác.
2. **`ROLE_FIELD_OWNER` (Chủ sân bóng / Quản lý sân):**
   - Xem toàn bộ danh sách lịch đặt sân tại cơ sở bóng đá của mình.
   - Duyệt đơn đặt sân từ trạng thái `PENDING` sang `CONFIRMED`.
   - Xem báo cáo thống kê doanh thu và số lượng đơn (`GET /api/v1/bookings/statistics`).
3. **`ROLE_ADMIN` (Quản trị viên toàn hệ thống):**
   - Quyền hạn tối cao: Quản trị tài khoản người dùng, xem toàn bộ thống kê, can thiệp hủy đơn bất kỳ khi có tranh chấp hoặc vi phạm quy định.

---

## 2. Chi tiết 6 Giai đoạn triển khai trên lớp (150 phút)

---

### GIAI ĐOẠN 1: Phân tích yêu cầu và Thiết kế ma trận quyền (15 phút)

#### 1. Bảng ma trận phân quyền hệ thống (Role-Permission Matrix)

| Endpoint URI | HTTP Method | Public | ROLE_CUSTOMER | ROLE_FIELD_OWNER | ROLE_ADMIN | Kiểm tra quyền sở hữu (Ownership) | HTTP Status mong đợi |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| `/api/v1/auth/login` | `POST` | **Có** | Cho phép | Cho phép | Cho phép | Không | `200 OK` / `401 Unauthorized` |
| `/api/v1/auth/register` | `POST` | **Có** | Cho phép | Cho phép | Cho phép | Không | `201 Created` / `400 Bad Request` |
| `/api/v1/bookings` | `GET` | **Có** | Cho phép | Cho phép | Cho phép | Không | `200 OK` |
| `/api/v1/bookings` | `POST` | Không | Cho phép | Cho phép | Cho phép | Tự động gán quyền | `201 Created` / `401 Unauthorized` |
| `/api/v1/bookings/{id}` | `GET` | Không | **Chỉ đơn của mình** | **Mọi đơn** | **Mọi đơn** | **Bắt buộc với Customer** | `200 OK` / `403 Forbidden` |
| `/api/v1/bookings/{id}` | `DELETE` | Không | **Chỉ đơn của mình** | Không | Cho phép | **Bắt buộc với Customer** | `204 No Content` / `403 Forbidden` |
| `/api/v1/bookings/statistics`| `GET` | Không | **Bị từ chối (403)** | Cho phép | Cho phép | Không | `200 OK` / `403 Forbidden` |

---

### GIAI ĐOẠN 2: Xây dựng Đăng nhập và JWT (30 phút)

#### 1. Cấu trúc bảng `users` trong MySQL (`init_football_system.sql`)
```sql
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Tên đăng nhập',
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã băm bằng BCrypt (password123)',
    `role` VARCHAR(30) NOT NULL COMMENT 'Vai trò: ROLE_CUSTOMER, ROLE_FIELD_OWNER, ROLE_ADMIN',
    `full_name` VARCHAR(100) NOT NULL COMMENT 'Họ và tên người dùng',
    `phone` VARCHAR(15) NOT NULL COMMENT 'Số điện thoại liên hệ',
    `active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Trạng thái tài khoản',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_username` (`username`),
    INDEX `idx_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 2. Cấp phát Token tại [JwtTokenProvider.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/security/JwtTokenProvider.java)
- Ký bảo mật bằng thuật toán **HMAC-SHA256 (HS256)** với khóa bí mật dài từ `.env`:
```java
public String generateToken(String username, String role, String fullName) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .claim("fullName", fullName)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
}
```

#### 3. API Đăng nhập [AuthController.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/controller/AuthController.java)
* **Endpoint:** `POST /api/v1/auth/login`
* **Request Body:**
  ```json
  {
    "username": "customer01",
    "password": "password123"
  }
  ```
* **Response Thành công (200 OK):**
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjAxIiwicm9sZSI6IlJPTEVfQ1VTVE9NRVIiLCJmdWxsTmFtZSI6Ik5ndXllbiBWYW4gQW4iLCJpYXQiOjE3Mjk5MTk2MDAsImV4cCI6MTczMDAwNjAwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "username": "customer01",
    "role": "ROLE_CUSTOMER",
    "fullName": "Nguyen Van An"
  }
  ```

---

### GIAI ĐOẠN 3 & 4: Bảo vệ API và Kiểm tra quyền sở hữu tài nguyên (Resource Ownership) (70 phút)

#### 1. Cấu hình bảo vệ API trong [SecurityConfig.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/security/SecurityConfig.java)
```java
.authorizeHttpRequests(auth -> auth
    // API Public
    .requestMatchers("/api/v1/auth/**", "/api/auth/**").permitAll()
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/v1/bookings").permitAll()

    // Báo cáo thống kê: Chỉ Chủ sân (ROLE_FIELD_OWNER) và Quản trị viên (ROLE_ADMIN)
    .requestMatchers(HttpMethod.GET, "/api/v1/bookings/statistics").hasAnyRole("FIELD_OWNER", "ADMIN")

    // Tất cả các API còn lại: Bắt buộc đăng nhập
    .anyRequest().authenticated()
)
```

#### 2. Kiểm tra quyền sở hữu tài nguyên tại [BookingService.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/service/BookingService.java)
Điểm mấu chốt để đạt chuẩn bảo mật là **không tin tưởng ID từ phía Client gửi lên**. Dù đã có JWT hợp lệ, hệ thống phải so khớp quyền sở hữu thực tế giữa tài khoản đăng nhập và bản ghi trong CSDL:

```java
public void verifyBookingOwnership(Booking booking, UserDetails userDetails) {
    if (userDetails == null) {
        throw new AccessDeniedException("Yêu cầu đăng nhập để truy cập tài nguyên");
    }

    // Quản trị viên và Chủ sân được phép xem mọi đơn
    boolean isPrivileged = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_FIELD_OWNER"));
    if (isPrivileged) {
        return;
    }

    // Khách hàng (ROLE_CUSTOMER) CHỈ được xem đơn của chính mình
    String currentUsername = userDetails.getUsername();
    boolean isOwner = (booking.getUsername() != null && booking.getUsername().equalsIgnoreCase(currentUsername));

    if (!isOwner) {
        throw new AccessDeniedException(
            "Bị từ chối truy cập (403 Forbidden): Bạn không có quyền truy cập lịch đặt sân của khách hàng khác (Vi phạm kiểm tra quyền sở hữu tài nguyên)");
    }
}
```

---

### GIAI ĐOẠN 5: Kiểm thử bảo mật, Xử lý lỗi và Bảo mật liên dịch vụ (25 phút)

#### 1. Phân biệt rành mạch giữa 401 Unauthorized và 403 Forbidden

| Tình huống thực tế | Mã HTTP Status | Lớp xử lý | Ý nghĩa nghiệp vụ |
| :--- | :---: | :--- | :--- |
| Không gửi Header `Authorization` | `401 Unauthorized` | [JwtAuthenticationEntryPoint](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/security/JwtAuthenticationEntryPoint.java) | Hệ thống không biết bạn là ai |
| Gửi Token bị sai chữ ký hoặc hết hạn | `401 Unauthorized` | [JwtAuthenticationEntryPoint](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/security/JwtAuthenticationEntryPoint.java) | Phiên làm việc không hợp lệ |
| Khách hàng A xem lịch đặt sân của Khách hàng B | **`403 Forbidden`** | [CustomAccessDeniedHandler](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/security/CustomAccessDeniedHandler.java) | Biết bạn là ai, nhưng bạn KHÔNG SỞ HỮU tài nguyên này |
| Khách hàng gọi API xem thống kê `/statistics` | **`403 Forbidden`** | [CustomAccessDeniedHandler](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/security/CustomAccessDeniedHandler.java) | Biết bạn là ai, nhưng bạn KHÔNG CÓ VAI TRÒ quản lý |
| Khách hàng hủy đơn của mình nhưng đơn đã `PAID` | **`400 Bad Request`** | [GlobalExceptionHandler](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/exception/GlobalExceptionHandler.java) | Vi phạm điều kiện nghiệp vụ thanh toán |

#### 2. Bảo mật giao tiếp giữa các dịch vụ (Machine-to-Machine Security)
Khi `Payment Service` gọi sang `Booking Service` để xác thực logic ID trước khi thanh toán:
- Sử dụng cơ chế Service Credential thông qua Header: `X-Internal-Service-Key: FootballInternalSecretServiceKey2026`.
- Tại [JwtAuthenticationFilter.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/security/JwtAuthenticationFilter.java), nếu phát hiện đúng khóa nội bộ, hệ thống tự động cấp quyền `ROLE_INTERNAL_SERVICE` để thực thi truy vấn mà không làm lộ token người dùng.

---

### GIAI ĐOẠN 6: So sánh bảo mật nghiệp vụ và Tổng kết (10 phút)

| Tiêu chí so sánh | Hệ thống Y tế (Phòng khám) | Hệ thống Giao dịch (Sân bóng đá / Đặt vé) |
| :--- | :--- | :--- |
| **Tài sản cốt lõi** | Dữ liệu nhạy cảm bệnh án, thông tin sức khỏe | Thông tin giao dịch đặt chỗ, tiền cọc, khung giờ sân |
| **API công khai** | Cực kỳ hạn chế (hầu hết API yêu cầu bảo vệ) | Tra cứu danh sách sân bóng, bảng giá, khung giờ trống |
| **Quy tắc phân quyền** | Vai trò + Phạm vi điều trị được phân công | Vai trò + Quyền sở hữu đơn đặt (Resource Ownership) |
| **Rủi ro lớn nhất** | Rò rỉ thông tin cá nhân và hồ sơ bệnh nhân | Đặt trùng giờ, hủy trộm lịch sân của đối thủ, gian lận cọc |

---

## 3. Danh mục sản phẩm bàn giao (Deliverables)

Toàn bộ các tài liệu thiết kế bảo mật cho Bài tập lớn đã được tạo hoàn chỉnh tại thư mục [security-design/](file:///c:/Users/abinh/Downloads/football-booking-service/security-design):
1. [security-requirements.md](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/security-requirements.md): Yêu cầu bảo mật, nhận diện tài sản, phân tích đe dọa (Threat Modeling).
2. [role-permission-matrix.md](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/role-permission-matrix.md): Ma trận phân quyền 3 Actor đầy đủ phương thức HTTP, điều kiện sở hữu và mã lỗi.
3. [api-security-design.md](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/api-security-design.md): Sơ đồ Sequence Diagram cho luồng xác thực đăng nhập, kiểm tra Resource Ownership và bảo mật liên dịch vụ.
4. [postman-collection.json](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/postman-collection.json): Bộ sưu tập Postman Collection với Test Script tự động kiểm tra mã lỗi và lưu trữ Token.
5. [test-results.md](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/test-results.md): Nhật ký kiểm thử chi tiết 13 kịch bản kiểm thử (100% PASS).

---

## 4. Hướng dẫn khởi chạy và Kiểm thử toàn diện

### Bước 1: Khởi động hệ thống
1. Chạy MySQL Server (Port 3306) và chạy script SQL [database/init_football_system.sql](file:///c:/Users/abinh/Downloads/football-booking-service/database/init_football_system.sql).
2. Khởi động Booking Service (Port 8081):
   ```powershell
   cd c:\Users\abinh\Downloads\football-booking-service\football-booking-service
   .\mvnw.cmd spring-boot:run
   ```
3. Khởi động Payment Service (Port 8082):
   ```powershell
   cd c:\Users\abinh\Downloads\football-booking-service\payment-service
   .\mvnw.cmd spring-boot:run
   ```

### Bước 2: Danh sách tài khoản mẫu sẵn sàng sử dụng
* Khách hàng 1: `username: customer01` | `password: password123` (Sở hữu đơn ID = 1, 3, 5)
* Khách hàng 2: `username: customer02` | `password: password123` (Sở hữu đơn ID = 2, 4, 6)
* Chủ sân bóng: `username: owner01` | `password: password123`
* Quản trị viên: `username: admin01` | `password: password123`

### Bước 3: Kiểm thử tương tác trực tiếp qua Swagger UI
* Mở trình duyệt tại: `http://localhost:8081/swagger-ui/index.html`
* Nhấn vào nút **Authorize** có biểu tượng ổ khóa màu xanh lá ở góc trên bên phải.
* Dán Access Token nhận được từ API `POST /api/v1/auth/login` để thực hiện kiểm thử các API bảo vệ một cách trực quan!
