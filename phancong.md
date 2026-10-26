# KẾ HOẠCH PHÂN CÔNG DỰ ÁN VÀ MA TRẬN CÂU HỎI BÀI KIỂM TRA SỐ 1
## Học phần: Phát triển phần mềm hướng dịch vụ (SOA)
**Đề tài lớn:** Hệ thống Quản lý cho thuê sân bóng đá (Football Pitch Rental Management System)  
**Nhóm sinh viên thực hiện (4 thành viên):**  
1. **Hùng** (Trưởng nhóm - Kiến trúc sư hệ thống & RESTful API)  
2. **Hoàng** (Kỹ sư tích hợp - Giao tiếp liên dịch vụ & Xử lý lỗi phân tán)  
3. **Linh** (Kỹ sư dữ liệu - Quản trị CSDL MySQL & Database-per-Service)  
4. **Nhật** (Kỹ sư an toàn thông tin - Xác thực JWT & Phân quyền RBAC)  

---

# PHẦN 1: MA TRẬN PHÂN CHIA CÂU HỎI THI (TỔNG 10,0 ĐIỂM)

Căn cứ theo tài liệu **BÀI KIỂM TRA SỐ 1 (Đánh giá CĐR L1)**, tổng số điểm là **10,0 điểm**, chia đều cho 4 thành viên với mức điểm chịu trách nhiệm tương đương nhau (**2,5 điểm / thành viên**).

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                        BẢNG TỔNG HỢP PHÂN CÔNG CÂU HỎI & ĐIỂM SỐ CHỊU TRÁCH NHIỆM                       │
├─────────────┬──────────────────────────┬──────────────────────────┬─────────────┬──────────────────────┤
│ Thành viên  │ Ý Lý thuyết (Câu 1)      │ Ý Thực nghiệm (Câu 2)    │ Tổng điểm   │ Phụ trách Báo cáo    │
├─────────────┼──────────────────────────┼──────────────────────────┼─────────────┼──────────────────────┤
│ 1. HÙNG     │ Ý 1 (1,5 điểm)           │ Ý 1 (1,0 điểm)           │ 2,5 điểm    │ Chương 1 + Slide     │
│ 2. HOÀNG    │ Ý 2.a (0,75 điểm) + demo │ Ý 2 (1,5 điểm)           │ 2,5 điểm    │ Chương 2             │
│ 3. LINH     │ Ý 2.b (0,75 điểm) + data │ Ý 3 (1,5 điểm)           │ 2,5 điểm    │ Chương 3             │
│ 4. NHẬT     │ Ý 3 (1,0 điểm)           │ Ý 4 (2,0 điểm - phần lõi)│ 2,5 - 3,0 đ │ Chương 4 + Postman   │
└─────────────┴──────────────────────────┴──────────────────────────┴─────────────┴──────────────────────┘
```

---

## 1. THÀNH VIÊN: HÙNG (Chịu trách nhiệm: 2,5 điểm)
* **Vai trò:** Trưởng nhóm – Thiết kế kiến trúc dịch vụ & Chuẩn hóa RESTful API.
* **Nhiệm vụ Câu 1 (Lý thuyết):**
  * **Ý 1. Thiết kế dịch vụ và RESTful API (1,5 điểm):**
    * *Ý 1.a (0,5 điểm):* Trình bày các đặc điểm cốt lõi của kiến trúc hướng dịch vụ (SOA): Tính liên kết lỏng (Loose Coupling), tính trừu tượng (Service Abstraction), khả năng tái sử dụng (Reusability), khả năng cộng tác (Interoperability). Phân biệt rành mạch giữa:
      * **Dịch vụ (Service):** Đơn vị logic độc lập thực thi một năng lực nghiệp vụ cụ thể.
      * **API (Application Programming Interface):** Hợp đồng giao tiếp kỹ thuật công khai phương thức truy cập dịch vụ.
      * **Ứng dụng khách (Client):** Bên tiêu thụ dịch vụ (Postman, Frontend, Mobile app).
    * *Ý 1.b (1,0 điểm):* Xác định các dịch vụ chính của đề tài quản lý sân bóng đá (`booking-service`, `payment-service`, `notification-service`), trách nhiệm của từng dịch vụ, và trình bày ít nhất 03 API tiêu biểu (`GET /api/v1/bookings`, `POST /api/v1/bookings`, `GET /api/v1/bookings/{id}`). Giải thích lý do phân chia hệ thống theo ranh giới nghiệp vụ (Bounded Context).
* **Nhiệm vụ Câu 2 (Thực nghiệm):**
  * **Ý 1. Thiết kế và triển khai RESTful API (1,0 điểm):**
    * Triển khai ít nhất 03 API nghiệp vụ chính tại `booking-service` (Port 8081).
    * Thiết kế cấu trúc URL chuẩn RESTful (danh từ số nhiều, phân cấp rõ ràng).
    * Sử dụng đúng phương thức HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) và mã trạng thái chuẩn (`200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`).
    * Cài đặt kiểm tra dữ liệu đầu vào bằng Bean Validation (`@Valid`, `@NotBlank`, `@Positive`, `@NotNull`).
    * Xuất bản tài liệu API chuẩn qua Swagger UI / OpenAPI 3 tại `http://localhost:8081/swagger-ui/index.html`.
* **Phần Báo cáo:** Chủ trì biên soạn **Chương 1: Giới thiệu đề tài và kiến trúc dịch vụ**.
* **Phần Demo:** Khởi động hệ thống, trình bày tổng quan kiến trúc và demo các API cơ bản (Phút 1 – 5).

---

## 2. THÀNH VIÊN: HOÀNG (Chịu trách nhiệm: 2,5 điểm)
* **Vai trò:** Kỹ sư tích hợp – Giao tiếp liên dịch vụ & Cơ chế xử lý lỗi hệ thống phân tán.
* **Nhiệm vụ Câu 1 (Lý thuyết):**
  * **Ý 2.a. Giao tiếp giữa các dịch vụ trong nghiệp vụ cụ thể (0,75 điểm + 0,25 điểm quy đổi):**
    * Mô tả chi tiết quy trình giao tiếp giữa `payment-service` và `booking-service` khi khách hàng thực hiện thanh toán tiền cọc sân bóng.
    * Làm rõ vai trò của HTTP/REST trong hệ thống phân tán: Request URI, Headers (`Authorization`, `X-Internal-Service-Key`), Request Body JSON, Response Body và các mã trạng thái (`200 OK`, `404 Not Found`, `503 Service Unavailable`, `504 Gateway Timeout`).
* **Nhiệm vụ Câu 2 (Thực nghiệm):**
  * **Ý 2. Giao tiếp giữa các dịch vụ (1,5 điểm):**
    * Xây dựng nghiệp vụ có sự phối hợp của 2 dịch vụ trở lên: `Payment Service` (Port 8082) gọi sang `Booking Service` (Port 8081) qua `RestClient` (`BookingClient.java`).
    * Thể hiện rõ: Dịch vụ gọi (`Payment`), dịch vụ được gọi (`Booking`), dữ liệu gửi đi (`bookingId`) và kết quả nhận về (`BookingDTO`).
    * Cài đặt và minh chứng xử lý tối thiểu **2 tình huống lỗi giao tiếp phân tán**:
      1. Đơn đặt sân không tồn tại $\rightarrow$ trả về `404 Not Found`.
      2. Dịch vụ `Booking Service` bị tắt hoặc mất mạng $\rightarrow$ bắt ngoại lệ `ResourceAccessException`, trả về `503 Service Unavailable`.
      3. Quá thời gian chờ phản hồi (Timeout > 2000ms) $\rightarrow$ trả về `504 Gateway Timeout`.
    * Vẽ sơ đồ tuần tự (**Sequence Diagram**) chuẩn UML mô tả rõ cả luồng thành công và luồng lỗi.
* **Phần Báo cáo:** Chủ trì biên soạn **Chương 2: Thiết kế API và giao tiếp dịch vụ**.
* **Phần Demo:** Trình diễn nghiệp vụ thanh toán liên dịch vụ và cố tình gây lỗi mất kết nối / timeout để chứng minh tính chịu lỗi (Phút 6 – 8).

---

## 3. THÀNH VIÊN: LINH (Chịu trách nhiệm: 2,5 điểm)
* **Vai trò:** Kỹ sư dữ liệu – Quản lý dữ liệu bền vững & Kiến trúc Database-per-Service.
* **Nhiệm vụ Câu 1 (Lý thuyết):**
  * **Ý 2.b. Quản lý dữ liệu và nguyên tắc Database-per-Service (0,75 điểm + 0,25 điểm quy đổi):**
    * Giải thích nguyên tắc mỗi dịch vụ quản lý dữ liệu thuộc phạm vi trách nhiệm của mình (Database-per-Service).
    * Phân tích lý do vì sao một dịch vụ **không nên và không được phép** truy cập trực tiếp cơ sở dữ liệu của dịch vụ khác (vi phạm tính đóng gói, gây coupling chặt chẽ, phụ thuộc lược đồ bảng vật lý).
    * Chỉ ra rủi ro cụ thể: Khi `Booking Service` thay đổi cấu trúc bảng `bookings`, `Payment Service` sẽ bị lỗi crash hàng loạt nếu truy cập trực tiếp cùng một database.
* **Nhiệm vụ Câu 2 (Thực nghiệm):**
  * **Ý 3. Quản lý dữ liệu và kết nối dịch vụ (1,5 điểm):**
    * Xóa bỏ hoàn toàn việc lưu trữ tạm thời trên bộ nhớ RAM (được comment lại làm minh chứng đối chiếu).
    * Chuyển đổi sang CSDL MySQL 8.x với 2 database độc lập: `football_booking_db` và `football_payment_db`.
    * Xây dựng Entity ánh xạ JPA (`Booking`, `Payment`), các Interface `BookingRepository`, `PaymentRepository` kế thừa `JpaRepository`.
    * Quản lý tính toàn vẹn dữ liệu thông qua annotation `@Transactional`.
    * Thể hiện rõ việc sử dụng **Logical ID** (`booking_id` kiểu `BIGINT`) thay vì tạo khóa ngoại vật lý (Foreign Key) xuyên database.
    * Đóng gói script khởi tạo cơ sở dữ liệu hoàn chỉnh (`database/init_football_system.sql`), chèn dữ liệu mẫu ban đầu.
* **Phần Báo cáo:** Chủ trì biên soạn **Chương 3: Quản lý dữ liệu (ERD, Entity, Repository, Database-per-Service)**.
* **Phần Demo:** Thực hiện ghi dữ liệu mới, mở MySQL Workbench/DBeaver truy vấn trực tiếp để chứng minh dữ liệu lưu bền vững và 2 CSDL độc lập (Phút 9 – 11).

---

## 4. THÀNH VIÊN: NHẬT (Chịu trách nhiệm: 2,5 – 3,0 điểm)
* **Vai trò:** Kỹ sư an toàn thông tin – Xác thực JWT, Phân quyền RBAC & Kiểm thử bảo mật.
* **Nhiệm vụ Câu 1 (Lý thuyết):**
  * **Ý 3. Xác thực và phân quyền dịch vụ (1,0 điểm):**
    * Phân biệt rõ ràng giữa *Xác thực (Authentication - Nhận diện danh tính)* và *Phân quyền (Authorization - Kiểm tra quyền hạn)*.
    * Trình bày cấu trúc JSON Web Token (Header, Payload, Signature) và cơ chế Stateless Token.
    * Phân biệt bản chất giữa:
      * **`HTTP 401 Unauthorized`:** Yêu cầu đăng nhập, thiếu token, token không hợp lệ hoặc đã hết hạn.
      * **`HTTP 403 Forbidden`:** Đã đăng nhập thành công nhưng không đủ quyền hạn (Role) hoặc cố tình truy cập tài nguyên của người khác.
    * Liên hệ trực tiếp với 3 vai trò trong đề tài: `ROLE_CUSTOMER`, `ROLE_FIELD_OWNER`, `ROLE_ADMIN`.
* **Nhiệm vụ Câu 2 (Thực nghiệm):**
  * **Ý 4. Xác thực, phân quyền và kiểm thử bảo mật (2,0 điểm - Nhật chủ trì kỹ thuật):**
    * Xây dựng API đăng nhập `POST /api/v1/auth/login`, mã hóa mật khẩu bằng **BCrypt**, cấp phát JWT ký bằng HMAC-SHA256 có thời hạn (`expiresIn`).
    * Cấu hình Spring Security 6 với `SecurityFilterChain`, bảo vệ các API cần xác thực.
    * Cài đặt cơ chế kiểm tra quyền sở hữu (**Resource Ownership Check**): Khách hàng A (`customer01`) chỉ được xem/hủy đơn của mình; nếu cố tình xem/hủy đơn của Khách hàng B (`customer02`) $\rightarrow$ trả về `403 Forbidden`.
    * Bảo vệ API báo cáo thống kê `/statistics`: Chỉ cho phép `ROLE_FIELD_OWNER` và `ROLE_ADMIN` truy cập; `ROLE_CUSTOMER` gọi sẽ bị từ chối `403 Forbidden`.
    * Cài đặt cơ chế xác thực máy-với-máy qua Header nội bộ `X-Internal-Service-Key` cho `Payment Service`.
    * Xuất bản bộ kiểm thử `postman-collection.json` và bảng kết quả kiểm thử [test-results.md](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/test-results.md) (13 test cases đạt 100% PASS).
* **Phần Báo cáo:** Chủ trì biên soạn **Chương 4: Xác thực và bảo mật** và hỗ trợ **Chương 5: Kiểm thử, kết quả và tự đánh giá**.
* **Phần Demo:** Trình diễn các kịch bản bảo mật trên Postman (Chưa login $\rightarrow$ 401; Login đúng $\rightarrow$ 200; Xem trộm đơn người khác $\rightarrow$ 403; Xem thống kê sai quyền $\rightarrow$ 403) (Phút 12 – 14).

---

# PHẦN 2: KẾ HOẠCH TIẾN ĐỘ 5 TUẦN CHI TIẾT (WBS)

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 LỘ TRÌNH 5 TUẦN TRIỂN KHAI PROJECT                                     │
├───────────┬─────────────────────────────────────────────────────────┬──────────────────────────────────┤
│ Tuần      │ Trọng tâm nghiệp vụ & Chuẩn kiến thức                   │ Sản phẩm đầu ra (Deliverables)   │
├───────────┼─────────────────────────────────────────────────────────┼──────────────────────────────────┤
│ TUẦN 1    │ Khởi động, Phân tích Kiến trúc SOA & Xây dựng REST API  │ Khung mã nguồn, REST API v1      │
│ TUẦN 2    │ Giao tiếp liên dịch vụ & Xử lý lỗi phân tán (Timeout)   │ RestClient, Sequence Diagram     │
│ TUẦN 3    │ Quản lý dữ liệu bền vững (MySQL & Database-per-Service) │ Script SQL, JPA Repositories     │
│ TUẦN 4    │ Dịch vụ xác thực JWT, Phân quyền RBAC & Ownership Check │ Spring Security, JWT Provider    │
│ TUẦN 5    │ Hợp nhất file `.env`, Kiểm thử Postman, Báo cáo & Demo  │ Báo cáo 10-15 trang, Demo 15p    │
└───────────┴─────────────────────────────────────────────────────────┴──────────────────────────────────┘
```

---

### TUẦN 1: Khởi động, Thiết kế Kiến trúc SOA & Xây dựng RESTful API Cơ bản
* **Mục tiêu:** Hoàn thành thiết kế hệ thống, phân chia 3 service, triển khai bộ REST API CRUD cơ bản tại `booking-service`.
* **Phân công chi tiết:**
  * **Hùng (Lead):**
    * Khởi tạo dự án Spring Boot 3.2.5 cho `football-booking-service` (Port 8081).
    * Thiết kế các endpoint CRUD ban đầu: `GET`, `POST`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.
    * Cài đặt Bean Validation kiểm tra tính hợp lệ dữ liệu.
    * Viết phần Lý thuyết Câu 1 - Ý 1.a & Ý 1.b vào bản thảo Báo cáo.
  * **Hoàng:**
    * Khảo sát kịch bản giao tiếp giữa dịch vụ đặt sân và dịch vụ thanh toán.
    * Phác thảo các thông điệp trao đổi dữ liệu (Request/Response DTO).
  * **Linh:**
    * Xây dựng mô hình thực thể Entity `Booking.java` và Enum `BookingStatus.java`.
    * Khảo sát các trường dữ liệu cần thiết cho việc quản lý sân bóng.
  * **Nhật:**
    * Nghiên cứu tài liệu Spring Security 6 và cơ chế Token Bearer JWT.
    * Xác định danh sách quyền hạn cho 3 Actor (`CUSTOMER`, `FIELD_OWNER`, `ADMIN`).
* **Sản phẩm nghiệm thu Tuần 1:**
  * `football-booking-service` biên dịch thành công, chạy trên Port 8081 với dữ liệu in-memory.
  * Bản thảo **Chương 1 Báo cáo kỹ thuật**.

---

### TUẦN 2: Xây dựng Giao tiếp liên dịch vụ & Cơ chế xử lý lỗi (Fault Tolerance)
* **Mục tiêu:** Khởi tạo `payment-service` (Port 8082), thiết lập gọi RESTful API sang `booking-service` và xử lý triệt để các lỗi mạng.
* **Phân công chi tiết:**
  * **Hoàng (Lead):**
    * Khởi tạo `payment-service` (Port 8082), viết `BookingClient.java` sử dụng `RestClient`.
    * Cài đặt cấu hình Timeout (`connect-timeout=2000ms`, `read-timeout=3000ms`).
    * Bắt các lỗi giao tiếp: 404 (Không thấy booking), 503 (Booking Service tắt), 504 (Timeout).
    * Vẽ sơ đồ tuần tự Sequence Diagram cho luồng gọi API thanh toán.
    * Hoàn thành nội dung Câu 1 - Ý 2.a và viết **Chương 2 Báo cáo**.
  * **Hùng:**
    * Bổ sung API lọc, tìm kiếm, phân trang và sắp xếp (`PageResponse<Booking>`).
    * Tích hợp Swagger UI / OpenAPI 3 hiển thị đầy đủ tài liệu API.
  * **Linh:**
    * Xây dựng Entity `Payment.java` (`amount`, `payment_method`, `status`, `transaction_code`, `booking_id`).
    * Đảm bảo DTO tương thích giữa 2 dịch vụ.
  * **Nhật:**
    * Thiết kế cơ chế khóa nội bộ `X-Internal-Service-Key` cho giao tiếp máy-với-máy giữa 2 service.
* **Sản phẩm nghiệm thu Tuần 2:**
  * Hai dịch vụ giao tiếp thông suốt; bắt lỗi chuẩn xác khi tắt một trong hai service.
  * Sơ đồ Sequence Diagram hoàn chỉnh; bản thảo **Chương 2 Báo cáo kỹ thuật**.

---

### TUẦN 3: Chuyển dịch Dữ liệu bền vững sang MySQL (Database-per-Service)
* **Mục tiêu:** Loại bỏ hoàn toàn in-memory RAM (comment lại để đối chiếu), tích hợp MySQL 8.x với 2 database độc lập.
* **Phân công chi tiết:**
  * **Linh (Lead):**
    * Thiết kế script CSDL [init_football_system.sql](file:///c:/Users/abinh/Downloads/football-booking-service/database/init_football_system.sql) tạo `football_booking_db` và `football_payment_db`.
    * Cấu hình Spring Data JPA, Hibernate trong `application.properties`.
    * Tạo `BookingRepository` và `PaymentRepository` kế thừa `JpaRepository`.
    * Áp dụng `@Transactional` để bảo đảm tính toàn vẹn giao dịch.
    * Hoàn thành nội dung Câu 1 - Ý 2.b và viết **Chương 3 Báo cáo**.
  * **Hùng:**
    * Comment lại code RAM cũ trong `BookingService.java` theo đúng yêu cầu bài học.
    * Kiểm tra tính hoạt động của các API sau khi chuyển sang JPA.
  * **Hoàng:**
    * Kiểm thử lại giao tiếp giữa `payment-service` và `booking-service` trên nền CSDL MySQL.
    * Đảm bảo cột `booking_id` trong `payments` là Logical ID, không tạo Foreign Key vật lý xuyên CSDL.
  * **Nhật:**
    * Bổ sung bảng `users` vào script SQL và tạo `UserRepository.java`.
* **Sản phẩm nghiệm thu Tuần 3:**
  * Dữ liệu được lưu trữ bền vững vào MySQL, khởi động lại ứng dụng không bị mất dữ liệu.
  * Script SQL khởi tạo hoàn chỉnh; bản thảo **Chương 3 Báo cáo kỹ thuật**.

---

### TUẦN 4: Dịch vụ Xác thực JWT, Phân quyền RBAC & Resource Ownership
* **Mục tiêu:** Cài đặt bảo mật toàn diện: Đăng nhập cấp JWT, băm mật khẩu BCrypt, phân quyền 3 Actor và ngăn chặn tấn công IDOR/BOLA.
* **Phân công chi tiết:**
  * **Nhật (Lead):**
    * Triển khai `JwtTokenProvider.java` (ký HMAC-SHA256, thời hạn 24h) và `JwtAuthenticationFilter.java`.
    * Cấu hình `SecurityConfig.java`: Phân biệt API công khai và API bảo vệ.
    * Triển khai kiểm tra **Resource Ownership** tại `BookingService.java`: Khách hàng chỉ xem/hủy đơn của mình $\rightarrow$ vi phạm trả về `403 Forbidden`.
    * Cài đặt `JwtAuthenticationEntryPoint` (bắt 401) và `CustomAccessDeniedHandler` (bắt 403).
    * Hoàn thành nội dung Câu 1 - Ý 3 và viết **Chương 4 Báo cáo**.
  * **Hùng:**
    * Cập nhật `BookingController.java` tự động liên kết tài khoản đang đăng nhập với đơn đặt sân mới.
    * Cấu hình Swagger UI hỗ trợ nút nhập Bearer Token JWT.
  * **Hoàng:**
    * Cập nhật `BookingClient.java` gửi Header `X-Internal-Service-Key` khi gọi sang Booking Service.
  * **Linh:**
    * Thêm cột `username` vào bảng `bookings` để lưu trữ thông tin chủ sở hữu đơn.
    * Chèn dữ liệu người dùng mẫu (`customer01`, `customer02`, `owner01`, `admin01`) với mật khẩu băm BCrypt.
* **Sản phẩm nghiệm thu Tuần 4:**
  * Hệ thống xác thực và phân quyền hoạt động hoàn hảo; ngăn chặn 100% việc xem trộm đơn của người khác.
  * Bản thảo **Chương 4 Báo cáo kỹ thuật**.

---

### TUẦN 5: Tích hợp `.env` Tổng, Kiểm thử Postman, Đóng gói Báo cáo & Luyện tập Demo
* **Mục tiêu:** Hợp nhất cấu hình hệ thống qua file `.env`, chạy kiểm thử tự động, đóng gói báo cáo hoàn chỉnh (10-15 trang) và tổng duyệt kịch bản demo 10-15 phút.
* **Phân công chi tiết:**
  * **Cả 4 thành viên phối hợp:**
    * Đưa toàn bộ cấu hình (Port, MySQL user/password, JWT secret, Timeout) vào duy nhất file [.env](file:///c:/Users/abinh/Downloads/football-booking-service/.env) ở thư mục gốc.
    * Kiểm tra biên dịch sạch toàn bộ dự án bằng lệnh Maven Wrapper (`mvnw clean compile`).
  * **Nhật:**
    * Hoàn thiện bộ sưu tập Postman [postman-collection.json](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/postman-collection.json) với Test Script tự động lưu biến token.
    * Lập bảng kết quả kiểm thử [test-results.md](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/test-results.md) (13 test cases) đưa vào **Chương 5 Báo cáo**.
  * **Linh:**
    * Viết tài liệu `README.md` hướng dẫn chi tiết các bước import CSDL và khởi chạy hệ thống.
  * **Hoàng:**
    * Chuẩn bị kịch bản demo kiểm thử chịu lỗi giao tiếp phân tán.
  * **Hùng (Lead):**
    * Tổng hợp toàn bộ 5 chương thành tài liệu Báo cáo kỹ thuật hoàn chỉnh (file PDF/DOCX từ 10-15 trang).
    * Thiết kế Slide trình chiếu ngắn gọn (7-10 slide).
    * Chủ trì buổi tổng duyệt demo trực tiếp cho nhóm theo đúng thời lượng 10-15 phút.
* **Sản phẩm nghiệm thu Tuần 5:**
  * File Báo cáo kỹ thuật hoàn chỉnh (10-15 trang).
  * Toàn bộ mã nguồn chạy mượt mà, file `.env` tổng và script SQL.
  * Bộ sưu tập Postman Collection và Slide thuyết trình.
  * Nhóm sẵn sàng 100% cho buổi chấm thi trực tiếp.

---

# PHẦN 3: KỊCH BẢN PHÂN VAI DEMO THI (10 – 15 PHÚT)

Căn cứ theo mục **II.3 - CÁCH DEMO** của đề thi, 4 thành viên phối hợp trình bày nhịp nhàng theo 5 nội dung bắt buộc:

| Thời lượng | Nội dung Demo | Thành viên chính | Thao tác chi tiết trên màn hình |
| :---: | :--- | :---: | :--- |
| **Phút 1 – 2** | **1. Khởi chạy hệ thống** | **Hùng** | Mở Terminal khởi chạy MySQL, `Booking Service` (Port 8081) và `Payment Service` (Port 8082). Giới thiệu ngắn gọn kiến trúc hệ thống và file `.env` tổng. |
| **Phút 3 – 5** | **2. Kiểm tra RESTful API** | **Hùng** | Mở Swagger UI, gọi API tra cứu danh sách sân bóng (Public, có phân trang/lọc). Gọi API tạo mới lịch đặt sân với dữ liệu vi phạm validation để chứng minh trả về `400 Bad Request`. |
| **Phút 6 – 8** | **3. Kiểm tra Giao tiếp liên dịch vụ & Xử lý lỗi** | **Hoàng** | Gọi API thanh toán từ `Payment Service` $\rightarrow$ gọi sang `Booking Service` thành công. Sau đó mở Terminal tắt `Booking Service`, gửi lại request để chứng minh hệ thống bắt lỗi trả về `503 Service Unavailable` hoặc `504 Timeout` chuẩn xác. |
| **Phút 9 – 11** | **4. Kiểm tra Quản lý dữ liệu CSDL** | **Linh** | Thực hiện một giao dịch thanh toán thành công. Mở trực tiếp công cụ quản lý MySQL Workbench/DBeaver, truy vấn bảng `bookings` và `payments` để chứng minh dữ liệu lưu bền vững và 2 database hoàn toàn tách biệt (Database-per-Service). |
| **Phút 12 – 14**| **5. Kiểm tra Xác thực & Bảo mật (401 vs 403)** | **Nhật** | Trình diễn 3 tình huống trên Postman:<br>1. Gọi API xem chi tiết khi chưa có Token $\rightarrow$ trả về **`401 Unauthorized`**.<br>2. Đăng nhập tài khoản `customer01`, nhận JWT Token, gọi xem đơn ID=1 của chính mình $\rightarrow$ trả về **`200 OK`**.<br>3. Giữ nguyên token của `customer01`, đổi URL xem đơn ID=2 của `customer02` $\rightarrow$ trả về **`403 Forbidden`** (Chứng minh chặn thành công IDOR). |
| **Phút 15** | **Phản biện câu hỏi của Giảng viên** | **Cả nhóm** | Thầy yêu cầu đổi trực tiếp một tham số trong code hoặc CSDL (ví dụ: đổi mật khẩu, đổi role từ CUSTOMER sang ADMIN, đổi timeout), nhóm thao tác ngay để chứng minh nắm vững mã nguồn. |

---

# PHẦN 4: CHECKLIST SẢN PHẨM PHẢI NỘP TRƯỚC GIỜ THI

* [x] **1. Báo cáo kỹ thuật:** Đã có khung chi tiết 5 chương, độ dài 10-15 trang.
* [x] **2. Mã nguồn hoàn chỉnh:** Biên dịch thành công với Maven Wrapper (`.\mvnw.cmd clean compile`).
* [x] **3. Cấu hình khởi chạy:** File cấu hình tập trung [.env](file:///c:/Users/abinh/Downloads/football-booking-service/.env) quản lý toàn bộ hệ thống.
* [x] **4. Script Cơ sở dữ liệu:** File [init_football_system.sql](file:///c:/Users/abinh/Downloads/football-booking-service/database/init_football_system.sql) tạo CSDL và chèn dữ liệu mẫu.
* [x] **5. Tài liệu API:** Swagger UI tự động tích hợp tại `/swagger-ui/index.html`.
* [x] **6. Bộ kiểm thử tự động:** File Postman Collection [postman-collection.json](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/postman-collection.json) và [test-results.md](file:///c:/Users/abinh/Downloads/football-booking-service/security-design/test-results.md).
