# BUỔI THỰC HÀNH 1: LÀM QUEN VỚI DỊCH VỤ VÀ XÂY DỰNG RESTFUL SERVICE ĐẦU TIÊN
## Học phần: Phát triển phần mềm hướng dịch vụ (SOA)

* **Thời lượng:** 150 phút | **Đối tượng:** Sinh viên năm thứ 3
* **Dự án áp dụng:** Football Booking Service (Hệ thống quản lý đặt lịch sân bóng đá)
* **Công nghệ:** Java 17, Spring Boot 3.2.5, Maven, Postman
* **Cổng dịch vụ (Port):** `8081`

---

## 1. Mục tiêu buổi thực hành

1. Nhận diện được chức năng nghiệp vụ để tổ chức thành một dịch vụ độc lập.
2. Khởi tạo và cấu hình hoàn chỉnh một RESTful Service với Spring Boot.
3. Xây dựng và kiểm thử các REST API theo chuẩn HTTP Method và định dạng dữ liệu JSON.
4. Hiểu rõ luồng request: `Client` $\rightarrow$ `Controller` $\rightarrow$ `Service` $\rightarrow$ `Dữ liệu` $\rightarrow$ `Response`.
5. Xử lý chuẩn xác các mã trạng thái HTTP (`200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`) và validation dữ liệu đầu vào.
6. Mở rộng các tính năng nghiệp vụ nâng cao: Tìm kiếm, phân trang và thống kê doanh thu.

---

## 2. Chi tiết nội dung thực hiện (Đã làm gì và làm như thế nào)

### BÀI 1: Nhận diện dịch vụ từ bài toán (20 phút)

* **Mục tiêu:** Hình thành tư duy hướng dịch vụ, chia nhỏ hệ thống thành các dịch vụ độc lập có trách nhiệm rõ ràng.
* **Đã làm gì:**
  1. **Xác định các dịch vụ trong hệ thống quản lý sân bóng:**
     - **Booking Service:** Quản lý lịch đặt sân, thời gian, trạng thái và giá thuê.
     - **Customer Service:** Quản lý thông tin khách hàng, số điện thoại, lịch sử đặt sân.
     - **Field Service:** Quản lý danh sách sân, loại sân (5 người, 7 người), tình trạng sân.
     - **Payment Service:** Tiếp nhận và xử lý thanh toán, hóa đơn.
     - **Notification Service:** Gửi tin nhắn SMS/Email thông báo lịch đặt thành công.
  2. **Xác định cụ thể trách nhiệm của `Booking Service`:**
     - *Dữ liệu quản lý:* `id`, `customerName`, `phone`, `fieldName`, `bookingDate`, `startTime`, `endTime`, `price`, `status`.
     - *Chức năng:* Tạo mới, xem danh sách, cập nhật, hủy/xóa lịch, tìm kiếm theo tên, phân trang, thống kê.
     - *Dữ liệu đầu vào (Input):* Dữ liệu JSON gửi lên từ Client qua HTTP request body hoặc query parameters.
     - *Dữ liệu đầu ra (Output):* JSON Object / Array kèm HTTP Status Code thích hợp.
  3. **Sơ đồ kiến trúc mức đơn giản:**
     ```
     +------------------+
     |      Client      | (Postman / Browser)
     +--------+---------+
              | (HTTP Request / JSON)
              v
     +------------------+
     |  Booking Service | (Spring Boot @ Port 8081)
     +--------+---------+
              | (In-Memory Access)
              v
     +------------------+
     |  Bookings Store  | (List<Booking> + AtomicLong)
     +------------------+
     ```

---

### BÀI 2: Khởi tạo RESTful Service (30 phút)

* **Mục tiêu:** Khởi tạo project Spring Boot, cấu hình ứng dụng và kiểm thử API trả về JSON.
* **Đã làm gì:**
  - Khởi tạo project Maven `booking-service` với các dependency:
    - `spring-boot-starter-web`: Xây dựng REST API.
    - `spring-boot-starter-validation`: Validate dữ liệu đầu vào.
  - Cấu hình file `src/main/resources/application.properties`:
    - `server.port=8081` (dùng port 8081 để tránh xung đột với cổng 8080).
    - `spring.jackson.serialization.write-dates-as-timestamps=false` (hiển thị thời gian định dạng chuỗi ISO).
* **Làm như thế nào:**
  - Viết `BookingController` với 2 endpoint mở đầu:
    - `GET /api/bookings/hello` $\rightarrow$ Trả về JSON:
      ```json
      { "message": "Welcome to Booking Service" }
      ```
    - `GET /api/bookings/info` $\rightarrow$ Trả về JSON thông tin service:
      ```json
      {
        "service": "Booking Service",
        "version": "1.0",
        "status": "UP"
      }
      ```

---

### BÀI 3: Xây dựng API Quản lý đặt sân (CRUD) (45 phút)

* **Mục tiêu:** Thiết kế resource, thực hiện đầy đủ các phương thức HTTP cho thao tác CRUD lưu trữ tạm thời trong RAM.
* **Đã làm gì:**
  - Tạo Model `Booking` và Enum `BookingStatus`.
  - Tạo `BookingService` sử dụng `List<Booking>` để lưu trữ và `AtomicLong` để tự động tăng `id`.
  - Khởi tạo sẵn 2 bản ghi mẫu trong constructor để test nhanh.
* **Làm như thế nào:**
  - **Trạng thái đặt sân (`BookingStatus`):** `PENDING` (Chờ duyệt), `CONFIRMED` (Đã duyệt), `PAID` (Đã thanh toán), `CANCELLED` (Đã hủy).
  - **Xây dựng 5 API CRUD:**
    1. `GET /api/bookings`: Lấy toàn bộ danh sách đặt sân.
    2. `GET /api/bookings/{id}`: Lấy chi tiết lịch đặt sân theo ID.
    3. `POST /api/bookings`: Thêm mới lịch đặt sân (tự sinh ID, mặc định trạng thái `PENDING`, trả về mã `201 Created`).
    4. `PUT /api/bookings/{id}`: Cập nhật thông tin lịch đặt sân theo ID.
    5. `DELETE /api/bookings/{id}`: Xóa lượt đặt sân theo ID.

---

### BÀI 4: Kiểm thử và Xử lý lỗi API (30 phút)

* **Mục tiêu:** Sử dụng đúng mã HTTP Status Code, kiểm tra dữ liệu đầu vào và trả về JSON lỗi có cấu trúc.
* **Đã làm gì:**
  - Định nghĩa rõ các tình huống phản hồi:
    - `200 OK`: Truy vấn, cập nhật, xóa thành công.
    - `201 Created`: Thêm mới thành công.
    - `400 Bad Request`: Dữ liệu đầu vào không hợp lệ.
    - `404 Not Found`: Không tìm thấy tài nguyên theo ID.
* **Làm như thế nào:**
  1. **Bean Validation:** Sử dụng các annotation trên `Booking.java`:
     - `@NotBlank`: `customerName`, `phone`, `fieldName` không được để trống.
     - `@NotNull`: `bookingDate`, `startTime`, `endTime`, `price` không được null.
     - `@Positive`: `price` phải lớn hơn 0.
     - Kèm theo `@Valid` ở `@RequestBody` trong `BookingController`.
  2. **Ngoại lệ tùy biến (Custom Exception):** Tạo `ResourceNotFoundException`.
  3. **Xử lý ngoại lệ toàn cục (`GlobalExceptionHandler`):**
     - Bắt `ResourceNotFoundException`: Trả về HTTP 404:
       ```json
       {
         "status": 404,
         "message": "Khong tim thay dat lich id=999",
         "timestamp": "2026-10-07T15:30:00"
       }
       ```
     - Bắt `MethodArgumentNotValidException`: Trả về HTTP 400:
       ```json
       {
         "status": 400,
         "message": "Du lieu khong hop le",
         "errors": {
           "customerName": "Ten khach hang khong duoc de trong",
           "price": "Gia thue phai lon hon 0"
         },
         "timestamp": "2026-10-07T15:30:00"
       }
       ```

---

### BÀI 5: Thử thách mở rộng (20 phút)

Dự án đã **hoàn thành toàn bộ cả 3 nhiệm vụ mở rộng** (A, B, C):

1. **Nhiệm vụ A – Tìm kiếm theo tên khách hàng:**
   - Endpoint: `GET /api/bookings/search?name=An`
   - Cách làm: Sử dụng Java Stream `filter` kiểm tra chuỗi không phân biệt hoa thường (`toLowerCase().contains(...)`).
2. **Nhiệm vụ B – Phân trang danh sách (Pagination):**
   - Endpoint: `GET /api/bookings/paged?page=0&size=10`
   - Cách làm: Dùng `subList(from, to)` kết hợp kiểm tra biên an toàn `Math.min(from + size, total)`.
3. **Nhiệm vụ C – Thống kê doanh thu và trạng thái:**
   - Endpoint: `GET /api/bookings/statistics`
   - Cách làm: Tạo DTO `StatisticsResponse`, đếm số lượng từng trạng thái và tính tổng doanh thu từ các đơn có trạng thái `PAID`:
     ```json
     {
       "total": 2,
       "pending": 1,
       "confirmed": 1,
       "paid": 0,
       "cancelled": 0,
       "totalRevenue": 0.0
     }
     ```

---

## 3. Cấu trúc mã nguồn

```
football-booking-service/
├── pom.xml                                               # Cấu hình Maven dependencies
├── thuchanh1.md                                          # Tài liệu báo cáo kết quả thực hành 1
└── src/
    └── main/
        ├── java/com/football/bookingservice/
        │   ├── BookingServiceApplication.java            # Class khởi chạy Spring Boot
        │   ├── controller/
        │   │   └── BookingController.java                # REST Controller cung cấp các endpoint
        │   ├── service/
        │   │   └── BookingService.java                   # Xử lý logic và lưu trữ in-memory
        │   ├── model/
        │   │   ├── Booking.java                          # Entity đặt sân với Bean Validation
        │   │   └── BookingStatus.java                    # Enum trạng thái đặt sân
        │   ├── dto/
        │   │   └── StatisticsResponse.java               # DTO trả về số liệu thống kê
        │   └── exception/
        │       ├── ResourceNotFoundException.java        # Exception 404
        │       └── GlobalExceptionHandler.java           # Bắt lỗi toàn cục (400, 404)
        └── resources/
            └── application.properties                    # Cấu hình port 8081 và Jackson ISO date
```

---

## 4. Hướng dẫn khởi chạy ứng dụng

1. **Yêu cầu:** Máy tính đã cài đặt Java 17+ và Maven.
2. **Khởi chạy:**
   ```bash
   cd football-booking-service
   mvn spring-boot:run
   ```
3. **Địa chỉ truy cập:** `http://localhost:8081/api/bookings`

---

## 5. Bảng danh mục API & Dữ liệu kiểm thử (Postman)

| STT | Method | Endpoint | Mục đích | Mã HTTP |
| :-- | :--- | :--- | :--- | :--- |
| 1 | `GET` | `/api/bookings/hello` | Chào mừng | `200 OK` |
| 2 | `GET` | `/api/bookings/info` | Kiểm tra service status | `200 OK` |
| 3 | `GET` | `/api/bookings` | Lấy tất cả lịch đặt sân | `200 OK` |
| 4 | `GET` | `/api/bookings/{id}` | Lấy chi tiết lịch đặt sân theo ID | `200 OK` / `404 Not Found` |
| 5 | `POST` | `/api/bookings` | Tạo mới lịch đặt sân | `201 Created` / `400 Bad Request` |
| 6 | `PUT` | `/api/bookings/{id}` | Cập nhật lịch đặt sân | `200 OK` / `404 Not Found` / `400 Bad Request` |
| 7 | `DELETE` | `/api/bookings/{id}` | Xóa lịch đặt sân | `200 OK` / `404 Not Found` |
| 8 | `GET` | `/api/bookings/search?name=An` | Tìm kiếm theo tên khách hàng | `200 OK` |
| 9 | `GET` | `/api/bookings/paged?page=0&size=5` | Phân trang danh sách | `200 OK` |
| 10 | `GET` | `/api/bookings/statistics` | Thống kê số lượng & doanh thu | `200 OK` |

### Mẫu Body JSON kiểm thử:

* **Tạo mới hợp lệ (POST - 201 Created):**
  ```json
  {
    "customerName": "Nguyen Van An",
    "phone": "0901234567",
    "fieldName": "San 7 nguoi so 1",
    "bookingDate": "2026-10-15",
    "startTime": "18:00",
    "endTime": "19:30",
    "price": 350000.0,
    "status": "CONFIRMED"
  }
  ```

* **Dữ liệu vi phạm validation (POST - 400 Bad Request):**
  ```json
  {
    "customerName": "",
    "phone": "",
    "fieldName": "San so 1",
    "bookingDate": "2026-10-15",
    "startTime": "18:00",
    "endTime": "19:30",
    "price": -10000.0
  }
  ```

---

## 6. Bảng nghiệm thu kết quả (Learning Evidence)

| Nội dung kiểm tra | Kết quả đạt được | Trạng thái |
| :--- | :--- | :--- |
| **Phân tích nhận diện dịch vụ** | Xác định 5 dịch vụ, mô tả I/O và vẽ sơ đồ kiến trúc | **Đạt** |
| **Khởi tạo Spring Boot** | Cấu hình chạy ổn định trên cổng 8081 | **Đạt** |
| **REST API CRUD** | Đầy đủ 5 endpoint chuẩn RESTful | **Đạt** |
| **Định dạng dữ liệu JSON** | Chuẩn format, ngày giờ ISO | **Đạt** |
| **Xử lý lỗi (Error Handling)** | Quản lý tập trung, trả về 200, 201, 400, 404 chuẩn | **Đạt** |
| **Thử thách mở rộng** | Hoàn thành cả 3/3 tính năng: Search, Pagination, Statistics | **Vượt chỉ tiêu** |
