# MA TRẬN PHÂN QUYỀN HỆ THỐNG QUẢN LÝ SÂN BÓNG ĐÁ (ROLE-PERMISSION MATRIX)
**Hệ thống:** Quản lý cho thuê sân bóng đá (Football Booking System)  
**Tiêu chuẩn:** Role-Based Access Control (RBAC) + Resource Ownership Validation  

---

## 1. Định nghĩa 3 Vai trò người dùng (Actors)

1. **`ROLE_CUSTOMER` (Người thuê sân / Cầu thủ):**
   - Đăng ký, đăng nhập tài khoản.
   - Tra cứu danh sách lịch sân bóng đá công khai (Public).
   - Đặt lịch sân bóng mới (hệ thống tự động liên kết quyền sở hữu với tài khoản đang đăng nhập).
   - Xem chi tiết và hủy **CHỈ đơn đặt sân của chính mình**.
   - Thanh toán tiền cọc hoặc thanh toán toàn phần cho đơn của mình.
   - **Bị cấm:** Xem/sửa/hủy đơn của người khác; duyệt đơn sang `CONFIRMED`; xem thống kê doanh thu hệ thống.

2. **`ROLE_FIELD_OWNER` (Chủ sân bóng / Quản lý sân):**
   - Xem toàn bộ danh sách lịch đặt sân tại sân bóng của mình.
   - Duyệt đơn đặt sân (`PENDING` $\rightarrow$ `CONFIRMED`).
   - Cập nhật thông tin sân bóng, giá thuê, giờ hoạt động.
   - Xem báo cáo thống kê doanh thu sân bóng (`GET /api/v1/bookings/statistics`).
   - **Bị cấm:** Quản lý tài khoản Admin, xóa tài khoản người dùng khác.

3. **`ROLE_ADMIN` (Quản trị viên toàn hệ thống):**
   - Toàn quyền truy cập mọi tài nguyên trong hệ thống (Superuser).
   - Quản lý tài khoản người dùng (khóa/mở tài khoản, phân quyền).
   - Hủy bỏ đơn đặt sân bất kỳ khi có vi phạm hoặc sự cố kỹ thuật.
   - Xem toàn bộ báo cáo doanh thu tổng hợp.

---

## 2. Bảng ma trận phân quyền chi tiết (Access Control Matrix)

| STT | HTTP Method | Endpoint URI | Công khai (Public) | Yêu cầu đăng nhập | ROLE_CUSTOMER | ROLE_FIELD_OWNER | ROLE_ADMIN | Kiểm tra quyền sở hữu (Ownership) | Điều kiện nghiệp vụ (Business Rules) | Mã HTTP Thành công | Mã HTTP Từ chối |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | `POST` | `/api/v1/auth/register` | Có | Không | Có | Có | Có | Không | Tên đăng nhập và SĐT chưa tồn tại | `201 Created` | `400 Bad Request` |
| **2** | `POST` | `/api/v1/auth/login` | Có | Không | Có | Có | Có | Không | Tài khoản hoạt động, đúng mật khẩu | `200 OK` | `401 Unauthorized` |
| **3** | `GET` | `/api/v1/auth/me` | Không | Có | Có | Có | Có | Có (Bản thân) | Token JWT hợp lệ | `200 OK` | `401 Unauthorized` |
| **4** | `GET` | `/api/v1/bookings` | Có | Không | Có | Có | Có | Không | Phân trang, lọc theo trạng thái | `200 OK` | `400 Bad Request` |
| **5** | `POST` | `/api/v1/bookings` | Không | Có | Có | Có | Có | Tự động gán `username` | Ngày đặt sân $\ge$ hiện tại, khung giờ hợp lệ | `201 Created` | `401 Unauthorized` |
| **6** | `GET` | `/api/v1/bookings/{id}` | Không | Có | **Chỉ đơn của mình** | **Mọi đơn** | **Mọi đơn** | **Bắt buộc với Customer** | ID tồn tại trong DB | `200 OK` | `401 / 403 / 404` |
| **7** | `PUT` | `/api/v1/bookings/{id}` | Không | Có | **Chỉ đơn của mình** | **Mọi đơn** | **Mọi đơn** | **Bắt buộc với Customer** | Toàn bộ trường hợp lệ | `200 OK` | `401 / 403 / 404` |
| **8** | `PATCH` | `/api/v1/bookings/{id}` | Không | Có | **Chỉ đơn của mình** (không duyệt) | **Duyệt đơn** | **Mọi đơn** | **Bắt buộc với Customer** | Duyệt `CONFIRMED` bắt buộc là OWNER/ADMIN | `200 OK` | `401 / 403 / 404` |
| **9** | `DELETE` | `/api/v1/bookings/{id}` | Không | Có | **Chỉ đơn của mình** | **Không** | **Mọi đơn** | **Bắt buộc với Customer** | **Chưa thanh toán (Không được hủy đơn PAID)** | `204 No Content` | `400 / 401 / 403 / 404` |
| **10** | `GET` | `/api/v1/bookings/{id}/payments` | Không | Có | **Chỉ đơn của mình** | Có | Có | **Bắt buộc với Customer** | Booking tồn tại | `200 OK` | `401 / 403 / 404` |
| **11** | `GET` | `/api/v1/bookings/statistics` | Không | Có | **Bị từ chối (403)** | **Được phép (200)** | **Được phép (200)** | Không | Chỉ dành cho quản lý sân | `200 OK` | `403 Forbidden` |

---

## 3. Quy tắc phân biệt mã lỗi HTTP chuẩn mực

- **`401 Unauthorized` (Chưa được xác thực):**
  - Xảy ra khi Request không gửi Header `Authorization: Bearer <token>`, hoặc token bị sai chữ ký, hết hạn, không đúng định dạng.
  - Phản hồi từ: `JwtAuthenticationEntryPoint`.
- **`403 Forbidden` (Đã xác thực nhưng không đủ quyền):**
  - Xảy ra khi:
    1. Khách hàng (`ROLE_CUSTOMER`) cố tình xem hoặc hủy đơn đặt sân của khách hàng khác (Vi phạm **Resource Ownership Check**).
    2. Khách hàng thông thường gọi API báo cáo doanh thu `/api/v1/bookings/statistics`.
    3. Khách hàng tự ý gửi request PATCH đổi trạng thái sang `CONFIRMED`.
  - Phản hồi từ: `CustomAccessDeniedHandler` hoặc `GlobalExceptionHandler`.
- **`400 Bad Request` (Vi phạm điều kiện nghiệp vụ):**
  - Dữ liệu gửi lên sai định dạng Bean Validation (giá $\le$ 0, SĐT không đúng 10 số, ngày đặt trống).
  - Khách hàng hợp lệ muốn hủy đơn của mình nhưng đơn đã ở trạng thái `PAID` (đã thanh toán thành công).
- **`404 Not Found` (Không tìm thấy tài nguyên):**
  - Truy vấn mã đặt sân không tồn tại trong hệ thống.
