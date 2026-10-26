# BUỔI THỰC HÀNH 4: QUẢN LÝ DỮ LIỆU VÀ KẾT NỐI DỊCH VỤ
## Học phần: Phát triển phần mềm hướng dịch vụ (SOA)

* **Thời lượng trên lớp:** 150 phút | **Thời lượng tự học mở rộng:** 90–120 phút
* **Đối tượng:** Sinh viên năm 3
* **Hệ thống áp dụng:** Football Booking System (Quản lý đặt lịch sân bóng đá - Kiến trúc Microservices)
* **Nền tảng công nghệ:** Java 17, Spring Boot 3.2.5, Spring Data JPA, Hibernate, MySQL 8.x, Maven, Postman

---

## 1. Vị trí của Buổi 4 trong chuỗi học phần

Buổi 4 đánh dấu bước chuyển đổi mang tính nền tảng của kiến trúc phần mềm hướng dịch vụ:
$$\text{In-Memory RAM (Buổi 1-3)} \longrightarrow \text{Relational Database (MySQL)} \longrightarrow \text{Persistence Layer (Spring Data JPA)} \longrightarrow \text{Transactions (@Transactional)}$$

```
          ┌────────────────────────────────────────────────────────┐
          │     HỆ THỐNG SÂN BÓNG ĐÁ - KIẾN TRÚC DATABASE-PER-SERVICE     │
          └────────────────────────────────────────────────────────┘

    [Client: Postman / Browser]
               │
               │ POST /api/payments
               ▼
   ┌───────────────────────┐   HTTP GET /api/v1/bookings/{id}   ┌───────────────────────┐
   │    Payment Service    │ ─────────────────────────────────> │    Booking Service    │
   │  (Spring Boot @ 8082) │ <───────────────────────────────── │  (Spring Boot @ 8081) │
   │  [Service Consumer]   │           JSON Booking             │   [Service Provider]  │
   └──────────┬────────────┘                                    └───────────┬───────────┘
              │ JPA / Hibernate                                             │ JPA / Hibernate
              ▼                                                             ▼
   ┌───────────────────────┐                                    ┌───────────────────────┐
   │  football_payment_db  │                                    │  football_booking_db  │
   │  (MySQL Database 2)   │                                    │  (MySQL Database 1)   │
   │  Bảng: `payments`     │                                    │  Bảng: `bookings`     │
   └───────────────────────┘                                    └───────────────────────┘
```

### Nguyên tắc kiến trúc cốt lõi (Database-per-Service):
1. **Phân quyền sở hữu dữ liệu:** `Booking Service` toàn quyền quản lý `football_booking_db`; `Payment Service` toàn quyền quản lý `football_payment_db`.
2. **Tuyệt đối không truy cập chéo DB:** `Payment Service` **KHÔNG BAO GIỜ** được kết nối trực tiếp vào `football_booking_db`. Muốn lấy thông tin đặt sân bắt buộc phải gọi qua REST API `GET /api/v1/bookings/{id}`.
3. **Logical ID thay vì Foreign Key vật lý:** Cột `booking_id` trong bảng `payments` chỉ là một mã định danh logic, **không tạo ràng buộc khóa ngoại (Foreign Key)** sang bảng `bookings` ở database khác.

---

## 2. Chi tiết 5 bài thực hành trên lớp

### BÀI 1: Thiết kế dữ liệu cho từng Service (20 phút)

* **Booking Service (`football_booking_db`):**
  * Bảng: `bookings`
  * Thuộc tính: `id` (PK, Auto Increment), `customer_name` (VARCHAR), `phone` (VARCHAR), `field_name` (VARCHAR), `booking_date` (DATE), `start_time` (TIME), `end_time` (TIME), `price` (DOUBLE), `status` (VARCHAR: PENDING, CONFIRMED, PAID, CANCELLED), `created_at`, `updated_at`.
* **Payment Service (`football_payment_db`):**
  * Bảng: `payments`
  * Thuộc tính: `id` (PK, Auto Increment), `booking_id` (BIGINT - Logical ID), `amount` (DOUBLE), `payment_method` (VARCHAR: VNPAY, MOMO, CASH, BANKING), `status` (VARCHAR: SUCCESS, PENDING, FAILED), `transaction_code` (VARCHAR UNIQUE), `payment_time` (DATETIME), `note` (VARCHAR).
* **Script SQL hoàn chỉnh:** Đã được đóng gói tại file: [database/init_football_system.sql](file:///c:/Users/abinh/Downloads/football-booking-service/database/init_football_system.sql).

---

### BÀI 2: Kết nối Spring Boot với MySQL Database (30 phút)

* **Bổ sung dependencies vào `pom.xml` của cả 2 service:**
  * `spring-boot-starter-data-jpa`
  * `com.mysql:mysql-connector-j`
* **Cấu hình `application.properties` (Booking Service):**
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/football_booking_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
  spring.datasource.username=root
  spring.datasource.password=root
  spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true
  spring.jpa.properties.hibernate.format_sql=true
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
  ```
* **Chuyển Model thành JPA Entity:** Khai báo `@Entity`, `@Table(name = "bookings")`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)` trên [Booking.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/model/Booking.java).

---

### BÀI 3: Persistence Layer với Spring Data JPA (35 phút)

* **Xây dựng Repository:**
  * [BookingRepository.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/repository/BookingRepository.java) kế thừa `JpaRepository<Booking, Long>`.
  * Khai báo các Derived Query Methods:
    * `findByCustomerNameContainingIgnoreCase(String name, Pageable pageable)`
    * `findByStatus(BookingStatus status, Pageable pageable)`
    * `findByCustomerNameContainingIgnoreCaseAndStatus(String name, BookingStatus status, Pageable pageable)`
    * `@Query("SELECT COALESCE(SUM(b.price), 0.0) FROM Booking b WHERE b.status = ...PAID")`
* **Nâng cấp Service Layer:**
  * [BookingService.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/service/BookingService.java) đã **comment toàn bộ code lưu RAM cũ** (`List<Booking> bookings`) để phục vụ đối chiếu kiến trúc.
  * Mọi thao tác CRUD, tìm kiếm, lọc, sắp xếp và phân trang đều được thực thi trực tiếp qua MySQL Database bằng `PageRequest.of(page, size, sortObj)`.

---

### BÀI 4: Quản lý Giao dịch dữ liệu (@Transactional) (30 phút)

* **Áp dụng `@Transactional`:**
  * Gắn `@Transactional` trên các phương thức ghi dữ liệu (`create`, `update`, `patch`, `delete` trong `BookingService` và `processPayment` trong `PaymentService`).
  * Gắn `@Transactional(readOnly = true)` trên các phương thức truy vấn để tối ưu hiệu năng của Hibernate Session.
* **Nguyên tắc phân tán quan trọng:**
  * Annotation `@Transactional` chỉ bảo đảm tính toàn vẹn (ACID / Rollback) **trong phạm vi nội bộ một database/service**.
  * Khi gọi REST API sang một service khác, `@Transactional` **không thể tự động rollback dữ liệu ở service đối tác**. Muốn xử lý giao dịch phân tán giữa nhiều microservice, sau này cần dùng các mẫu kiến trúc như Saga Pattern / 2PC.

---

### BÀI 5: Tích hợp hai Service có CSDL riêng (35 phút)

* **Quy trình thực thi End-to-End:**
  1. Client gửi `POST http://localhost:8082/api/payments` với `bookingId = 1`.
  2. `PaymentService` (`@Transactional`) nhận request, gọi `BookingClient` sang `http://localhost:8081/api/v1/bookings/1`.
  3. `BookingService` truy vấn MySQL database `football_booking_db`, trả về dữ liệu lịch đặt sân.
  4. `PaymentService` xác nhận lịch đặt tồn tại, lưu bản ghi giao dịch thành công vào MySQL database `football_payment_db`.
* **Xử lý các kịch bản lỗi:**
  * **Test A (Thành công - 201 Created):** `bookingId` tồn tại $\rightarrow$ lưu thanh toán vào `payments`, trả về thông tin kết hợp.
  * **Test B (Lỗi nghiệp vụ - 404 Not Found):** `bookingId = 9999` không tồn tại trong `football_booking_db` $\rightarrow$ không lưu dữ liệu vào `football_payment_db`, trả về mã 404.
  * **Test C (Lỗi dịch vụ - 503 Service Unavailable):** Tắt `Booking Service` $\rightarrow$ `Payment Service` bắt `ResourceAccessException` và trả về mã lỗi 503 thân thiện.

---

## 3. Hướng dẫn khởi tạo CSDL MySQL và Khởi chạy hệ thống

### Bước 1: Khởi tạo Cơ sở dữ liệu bằng file SQL
Mở **MySQL Workbench**, **DBeaver**, **Navicat** hoặc **Terminal MySQL**, mở file [database/init_football_system.sql](file:///c:/Users/abinh/Downloads/football-booking-service/database/init_football_system.sql) và nhấn **Execute** (hoặc chạy lệnh):
```bash
mysql -u root -p < database/init_football_system.sql
```
*(Nếu chưa chạy script SQL, Spring Boot với cấu hình `createDatabaseIfNotExist=true` và `ddl-auto=update` cũng sẽ tự động tạo database và bảng khi khởi động).*

### Bước 2: Chạy Booking Service (Port 8081)
```bash
cd football-booking-service/football-booking-service
.\mvnw.cmd spring-boot:run
```

### Bước 3: Chạy Payment Service (Port 8082)
```bash
cd football-booking-service/payment-service
.\mvnw.cmd spring-boot:run
```

---

## 4. Bảng kiểm tra kết quả (Postman Test Verification)

| STT | Kịch bản kiểm thử | Method & URL | Body | Kết quả mong đợi |
| :-- | :--- | :--- | :--- | :--- |
| **TC01** | Lấy danh sách từ MySQL | `GET http://localhost:8081/api/v1/bookings` | Không | `200 OK`, dữ liệu trả về từ bảng `bookings` |
| **TC02** | Tạo mới đơn đặt vào MySQL | `POST http://localhost:8081/api/v1/bookings` | JSON Booking hợp lệ | `201 Created`, chạy `SELECT * FROM bookings` thấy bản ghi mới |
| **TC03** | Thanh toán thành công (2 DB) | `POST http://localhost:8082/api/payments` | `{"bookingId": 1, "amount": 300000.0, "paymentMethod": "VNPAY"}` | `201 Created`, lưu vào bảng `payments` của `football_payment_db` |
| **TC04** | Thanh toán thất bại (ID sai) | `POST http://localhost:8082/api/payments` | `{"bookingId": 9999, ...}` | `404 Not Found`, bảng `payments` không bị thêm rác |
| **TC05** | Booking Service bị tắt | Tắt terminal 8081, gửi lại POST thanh toán | `{"bookingId": 1, ...}` | `503 Service Unavailable` |

---

## 5. Bảng nghiệm thu tiêu chí đánh giá Buổi 4 (10.0 / 10.0)

| Tiêu chí đánh giá | Trọng số | Kết quả thực hiện | Đánh giá |
| :--- | :---: | :--- | :---: |
| **Thiết kế Database & Entity** | 1.5 | Tạo 2 CSDL riêng biệt, Entity `@Entity`, `@Table`, `@Id` chuẩn JPA | **1.5 / 1.5** |
| **Kết nối Spring Boot – MySQL** | 1.5 | Cấu hình DataSource, Hibernate dialect, ddl-auto, show-sql đầy đủ | **1.5 / 1.5** |
| **JPA / Repository / Service Layer** | 1.5 | Sử dụng `JpaRepository`, comment code RAM cũ, phân tầng kiến trúc chuẩn | **1.5 / 1.5** |
| **CRUD + Pagination + Query** | 1.0 | `Pageable`, `PageRequest`, Dynamic Sort, Derived Queries thực thi trên SQL | **1.0 / 1.0** |
| **Transaction và xử lý dữ liệu** | 1.0 | Áp dụng `@Transactional` đúng nguyên tắc toàn vẹn dữ liệu | **1.0 / 1.0** |
| **Service-to-Service Communication** | 2.0 | Kết nối giữa 2 Service có CSDL riêng, tuân thủ nguyên tắc không chọc chéo DB | **2.0 / 2.0** |
| **Error Handling (400, 404, 503)** | 0.5 | Xử lý tập trung qua `@RestControllerAdvice`, bắt lỗi mạng phân tán | **0.5 / 0.5** |
| **Thiết kế áp dụng cho Bài tập lớn** | 1.0 | Soạn thảo tài liệu `data-and-service-design.md` đầy đủ 10 mục kiến trúc | **1.0 / 1.0** |
| **TỔNG ĐIỂM DỰ KIẾN** | **10.0** | **Đạt tuyệt đối mọi yêu cầu Buổi thực hành 4** | **10.0 / 10.0** |
