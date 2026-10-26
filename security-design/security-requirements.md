# TÀI LIỆU YÊU CẦU BẢO MẬT HỆ THỐNG QUẢN LÝ SÂN BÓNG ĐÁ
**Dự án:** Hệ thống Quản lý cho thuê sân bóng đá (Football Pitch Rental Management System)  
**Kiến trúc:** Microservices (Booking Service, Payment Service, Notification Service)  
**Tác giả:** Nhóm phát triển SOA  

---

## 1. Xác định tài sản cần bảo vệ (Asset Identification)

Trong hệ thống đặt sân bóng đá, 3 nhóm tài sản cốt lõi cần bảo vệ nghiêm ngặt bao gồm:

### 1.1. Thông tin tài khoản và dữ liệu cá nhân khách hàng (User & Identity Data)
- **Tài sản:** Tên đăng nhập (`username`), mật khẩu (`password_hash`), họ tên, số điện thoại (`phone`), vai trò (`role`).
- **Mức độ nhạy cảm:** Cao (Confidentiality & Integrity).
- **Rủi ro:** 
  - Lộ mật khẩu người dùng nếu lưu dưới dạng plain text.
  - Rò rỉ số điện thoại khách hàng cho bên thứ ba để spam quảng cáo.
  - Đánh cắp phiên đăng nhập (Session Hijacking) hoặc giả mạo danh tính (Impersonation).

### 1.2. Dữ liệu lịch đặt sân bóng (Booking Records & Time Slots)
- **Tài sản:** Lịch đặt sân (`customer_name`, `phone`, `field_name`, `booking_date`, `start_time`, `end_time`, `status`, `username`).
- **Mức độ nhạy cảm:** Rất cao (Integrity & Availability).
- **Rủi ro:**
  - **Lỗ hổng BOLA/IDOR (Broken Object Level Authorization):** Khách hàng A tự ý sửa URL (`GET /api/v1/bookings/2`, `DELETE /api/v1/bookings/2`) để xem trộm hoặc hủy lịch đá bóng của khách hàng B.
  - **Tranh chấp khung giờ (Double Booking):** Hai khách hàng cùng đặt một sân trong cùng khung giờ do thiếu cơ chế kiểm tra đồng thời.
  - **Sửa đổi trái phép trạng thái đơn:** Khách hàng tự ý gọi API PATCH để chuyển trạng thái từ `PENDING` sang `CONFIRMED` hoặc `PAID` mà không thông qua chủ sân.

### 1.3. Giao dịch tài chính và doanh thu sân bóng (Financial & Revenue Data)
- **Tài sản:** Lịch sử thanh toán (`payments`), mã giao dịch (`transaction_code`), báo cáo doanh thu (`/statistics`).
- **Mức độ nhạy cảm:** Tối mật (Confidentiality & Auditability).
- **Rủi ro:**
  - Khách hàng xem lén doanh thu tổng của chủ sân bóng.
  - Giả mạo giao dịch thanh toán thành công để chiếm dụng sân.
  - Hủy đơn sau khi đã thanh toán tiền sân mà không qua quy trình hoàn tiền kiểm duyệt.

---

## 2. Mô hình đe dọa (Threat Modeling & Vulnerability Analysis)

| Mã nguy cơ | Loại tấn công / Lỗ hổng | Mô tả kịch bản | Biện pháp giảm thiểu |
| :--- | :--- | :--- | :--- |
| **TH-01** | **BOLA / IDOR (Broken Object Level Authorization)** | Người dùng đã đăng nhập thay đổi ID trên URL để đọc hoặc hủy lịch đặt sân của người khác. | Kiểm tra **Resource Ownership** ở Service Layer: `booking.username == currentUser.username`. Nếu vi phạm $\rightarrow$ HTTP 403 Forbidden. |
| **TH-02** | **Privilege Escalation** | Khách hàng thông thường gọi các API dành riêng cho Chủ sân hoặc Admin (như duyệt đơn `CONFIRMED`, xem thống kê `/statistics`). | Áp dụng RBAC bằng Spring Security `@PreAuthorize` và cấu hình phân quyền theo Role trên URI. |
| **TH-03** | **Credential Stuffing / Brute Force** | Kẻ tấn công thử hàng loạt mật khẩu để dò tài khoản. | Mã hóa mật khẩu bằng thuật toán băm chậm **BCrypt (10 rounds)**. Thông báo lỗi đăng nhập chung: *"Tên đăng nhập hoặc mật khẩu không chính xác"*, không tiết lộ tài khoản có tồn tại hay không. |
| **TH-04** | **Token Tampering / Forgery** | Kẻ tấn công tự tạo JWT giả mạo bằng thuật toán `none` hoặc sửa đổi `role` trong payload. | Ký token bằng thuật toán **HMAC-SHA256 (HS256)** với khóa bí mật an toàn `JWT_SECRET` lưu trong file môi trường `.env`. Kiểm tra chữ ký bắt buộc ở mỗi request. |
| **TH-05** | **Man-in-the-Middle (MitM) & Replay Attack** | Đánh cắp Token trên đường truyền và sử dụng lại. | Quy định thời gian hết hạn (`exp`) cho Access Token (15 phút - 24 giờ). Giao tiếp liên dịch vụ sử dụng khóa bí mật riêng biệt `X-Internal-Service-Key`. |
| **TH-06** | **Sensitive Data Exposure qua Log** | Ghi log toàn bộ HTTP request/response làm lộ JWT, mật khẩu hoặc số thẻ. | Cấu hình log không in mật khẩu, chỉ log username, action, timestamp và resource ID. |

---

## 3. Nguyên tắc bảo mật cốt lõi áp dụng vào hệ thống

1. **Least Privilege (Quyền hạn tối thiểu):** Mỗi Actor chỉ được cấp đúng các quyền cần thiết để thực hiện công việc của mình.
2. **Stateless Authentication (Xác thực không trạng thái):** Không lưu trữ Session trên RAM của Server. Mọi thông tin định danh và quyền hạn được mã hóa trong JWT Access Token.
3. **Defense in Depth (Bảo vệ theo chiều sâu):** Bảo vệ tại tầng Filter (URL Matching trong `SecurityFilterChain`), tầng Method (`@PreAuthorize`), và tầng Nghiệp vụ (Ownership Check trong `BookingService`).
4. **Machine-to-Machine Security:** Các dịch vụ nội bộ (Payment $\rightarrow$ Booking) giao tiếp qua header `X-Internal-Service-Key`, không cho phép truy cập nặc danh kể cả trong mạng nội bộ.
