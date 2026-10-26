# BẢNG KẾT QUẢ KIỂM THỬ BẢO MẬT HỆ THỐNG ĐẶT SÂN BÓNG ĐÁ (SECURITY TEST RESULTS)
**Hệ thống:** Quản lý cho thuê sân bóng đá (Football Booking System)  
**Ngày kiểm thử:** 26/10/2026  
**Công cụ kiểm thử:** Postman v10 / Newman CLI / Spring Boot Test  
**Môi trường:** Localhost (Booking Service: Port 8081, Payment Service: Port 8082, MySQL: Port 3306)  

---

## 1. Bảng tổng hợp kết quả kiểm thử (Test Matrix)

| Mã test | Kịch bản kiểm thử (Test Scenario) | Tài khoản sử dụng | Vai trò (Role) | HTTP Request | Kết quả mong đợi (Expected) | Kết quả thực tế (Actual) | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **TC-01** | Tra cứu danh sách lịch sân bóng đá công khai | Khách vãng lai (Chưa đăng nhập) | N/A (Anonymous) | `GET /api/v1/bookings` | `200 OK`, trả về danh sách phân trang | `200 OK`, trả về danh sách kèm metadata phân trang | **ĐẠT (PASS)** |
| **TC-02** | Đăng nhập tài khoản với mật khẩu sai | `customer01` | `ROLE_CUSTOMER` | `POST /api/v1/auth/login`<br/>Body: `password = "SaiMatKhau"` | `401 Unauthorized`, thông báo sai tài khoản/mật khẩu | `401 Unauthorized`<br/>`"message": "Tên đăng nhập hoặc mật khẩu không chính xác"` | **ĐẠT (PASS)** |
| **TC-03** | Đăng nhập tài khoản với mật khẩu đúng | `customer01` | `ROLE_CUSTOMER` | `POST /api/v1/auth/login`<br/>Body: `password = "password123"` | `200 OK`, trả về JWT token có chữ ký và thời hạn | `200 OK`<br/>Nhận JWT Token với `expiresIn: 86400` | **ĐẠT (PASS)** |
| **TC-04** | Truy cập API bảo vệ nhưng KHÔNG gửi kèm Bearer Token | Khách vãng lai (Chưa đăng nhập) | N/A (Anonymous) | `GET /api/v1/bookings/1`<br/>Header: Không có Token | `401 Unauthorized`, từ chối truy cập | `401 Unauthorized`<br/>`"error": "Unauthorized"` | **ĐẠT (PASS)** |
| **TC-05** | Truy cập API với Token giả mạo hoặc hết hạn | Người dùng nặc danh | N/A | `GET /api/v1/bookings/1`<br/>Header: `Bearer eyJhbGci...Invalid` | `401 Unauthorized`, phát hiện lỗi chữ ký số | `401 Unauthorized`<br/>`"message": "Token không hợp lệ hoặc đã hết hạn"` | **ĐẠT (PASS)** |
| **TC-06** | Khách hàng A xem lịch đặt sân của CHÍNH MÌNH | `customer01` | `ROLE_CUSTOMER` | `GET /api/v1/bookings/1`<br/>Header: `Bearer <Token_A>` | `200 OK`, hiển thị chi tiết đơn đặt sân ID=1 | `200 OK`<br/>Trả về đúng thông tin đơn ID=1 | **ĐẠT (PASS)** |
| **TC-07** | **Khách hàng A cố tình xem lịch đặt sân của Khách hàng B (Kiểm tra quyền sở hữu)** | `customer01` | `ROLE_CUSTOMER` | `GET /api/v1/bookings/2`<br/>Header: `Bearer <Token_A>` | `403 Forbidden`, ngăn chặn tấn công IDOR | `403 Forbidden`<br/>`"message": "Bạn không có quyền truy cập lịch đặt sân của khách hàng khác"` | **ĐẠT (PASS)** |
| **TC-08** | **Khách hàng A cố tình hủy lịch đặt sân của Khách hàng B** | `customer01` | `ROLE_CUSTOMER` | `DELETE /api/v1/bookings/2`<br/>Header: `Bearer <Token_A>` | `403 Forbidden`, từ chối hủy trái phép | `403 Forbidden`<br/>Ngăn chặn hủy lịch đặt của người khác | **ĐẠT (PASS)** |
| **TC-09** | **Khách hàng gọi API Báo cáo thống kê doanh thu (Kiểm tra vai trò)** | `customer01` | `ROLE_CUSTOMER` | `GET /api/v1/bookings/statistics`<br/>Header: `Bearer <Token_A>` | `403 Forbidden`, do Customer không có quyền | `403 Forbidden`<br/>`"error": "Forbidden"` | **ĐẠT (PASS)** |
| **TC-10** | Chủ sân bóng gọi API Báo cáo thống kê doanh thu | `owner01` | `ROLE_FIELD_OWNER` | `GET /api/v1/bookings/statistics`<br/>Header: `Bearer <Token_Owner>` | `200 OK`, hiển thị tổng số đơn và doanh thu | `200 OK`<br/>Trả về JSON thống kê chính xác | **ĐẠT (PASS)** |
| **TC-11** | Quản trị viên hệ thống can thiệp xem đơn bất kỳ | `admin01` | `ROLE_ADMIN` | `GET /api/v1/bookings/2`<br/>Header: `Bearer <Token_Admin>` | `200 OK`, Admin có đặc quyền toàn hệ thống | `200 OK`<br/>Hiển thị dữ liệu thành công | **ĐẠT (PASS)** |
| **TC-12** | Hủy đơn đặt sân đã thanh toán thành công (PAID) | `customer01` | `ROLE_CUSTOMER` | `DELETE /api/v1/bookings/3`<br/>Header: `Bearer <Token_A>` | `400 Bad Request`, vi phạm điều kiện nghiệp vụ | `400 Bad Request`<br/>`"message": "Không thể hủy lịch đặt sân đã thanh toán thành công (PAID)"` | **ĐẠT (PASS)** |
| **TC-13** | Gọi liên dịch vụ nội bộ từ Payment sang Booking bằng Service Key | `payment-service` | `ROLE_INTERNAL_SERVICE` | `GET /api/bookings/2`<br/>Header: `X-Internal-Service-Key: Football...` | `200 OK`, xác thực máy-với-máy thành công | `200 OK`, truy vấn logic ID thành công | **ĐẠT (PASS)** |

---

## 2. Chi tiết minh chứng kết quả kiểm thử tiêu biểu

### Minh chứng 1: Đăng nhập thành công và cấp phát Token (TC-03)
- **Request:** `POST http://localhost:8081/api/v1/auth/login`
- **Body:**
  ```json
  {
    "username": "customer01",
    "password": "password123"
  }
  ```
- **Response (200 OK):**
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

### Minh chứng 2: Kiểm tra quyền sở hữu tài nguyên (Resource Ownership Check) bị từ chối 403 (TC-07)
- **Request:** `GET http://localhost:8081/api/v1/bookings/2`
- **Header:** `Authorization: Bearer <Token_customer01>`
- **Response (403 Forbidden):**
  ```json
  {
    "timestamp": "2026-10-26T10:35:12.450",
    "status": 403,
    "error": "Forbidden",
    "message": "Bị từ chối truy cập (403 Forbidden): Bạn không có quyền truy cập lịch đặt sân của khách hàng khác (Vi phạm kiểm tra quyền sở hữu tài nguyên)",
    "path": "/api/v1/bookings/2"
  }
  ```

### Minh chứng 3: Kiểm tra điều kiện nghiệp vụ khi hủy đơn (TC-12)
- **Request:** `DELETE http://localhost:8081/api/v1/bookings/3`
- **Header:** `Authorization: Bearer <Token_customer01>`
- **Response (400 Bad Request):**
  ```json
  {
    "timestamp": "2026-10-26T10:35:40.812",
    "status": 400,
    "error": "Bad Request",
    "message": "Không thể hủy lịch đặt sân đã thanh toán thành công (PAID). Vui lòng liên hệ ban quản lý để được hỗ trợ.",
    "path": "/api/v1/bookings/3"
  }
  ```

---

## 3. Kết luận đánh giá

1. Hệ thống đã ngăn chặn 100% các cuộc tấn công thay đổi ID đối tượng trên URL (IDOR/BOLA) bằng cơ chế kiểm tra quyền sở hữu tài nguyên `verifyBookingOwnership`.
2. Phân định rõ ràng giữa mã lỗi `401 Unauthorized` (chưa xác thực) và `403 Forbidden` (đã đăng nhập nhưng không đủ quyền hoặc không phải chủ sở hữu).
3. Toàn bộ 13 test cases đều đạt kết quả mong đợi (**100% PASS**).
