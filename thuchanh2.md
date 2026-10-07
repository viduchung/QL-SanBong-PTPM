# BUỔI THỰC HÀNH 2: GIAO TIẾP GIỮA CÁC DỊCH VỤ (SERVICE-TO-SERVICE COMMUNICATION)
## Học phần: Phát triển phần mềm hướng dịch vụ (SOA)

* **Thời lượng:** 150 phút | **Đối tượng:** Sinh viên năm thứ 3
* **Kiến trúc:** Microservices độc lập (Service-to-Service Communication)
* **Đề tài áp dụng:** Hệ thống Quản lý đặt lịch sân bóng đá (Football Pitch Booking System)
* **Công nghệ:** Java 17, Spring Boot 3.2.5, Node.js, Maven, Postman

---

## 1. Mục tiêu buổi thực hành

1. **Hiểu mô hình giao tiếp Service-to-Service:** Một dịch vụ hiếm khi đứng độc lập; các microservice phải gọi và trao đổi dữ liệu với nhau qua HTTP/REST hoặc Message Broker để hoàn thành một quy trình nghiệp vụ.
2. **Xây dựng Service Provider:** Dịch vụ cung cấp dữ liệu qua REST API (`booking-service` tại cổng 8081).
3. **Xây dựng Service Consumer:** Dịch vụ gọi và tiêu thụ REST API của dịch vụ khác (`payment-service` tại cổng 8082 sử dụng Spring `RestClient`).
4. **Trải nghiệm đa nền tảng (Multi-platform):** Sử dụng Node.js (`notification-service` tại cổng 8083) để tiêu thụ REST API của Spring Boot, chứng minh tính độc lập nền tảng của REST/JSON.
5. **Xử lý các sự cố mạng đặc thù trong hệ thống phân tán:**
   - Resource không tồn tại $\rightarrow$ `404 Not Found`.
   - Dịch vụ đối tác bị sập/tắt kết nối (Connection Refused) $\rightarrow$ `503 Service Unavailable`.
   - Dịch vụ đối tác phản hồi chậm (Timeout > 3s) $\rightarrow$ `504 Gateway Timeout`.
6. **Phân biệt giao tiếp đồng bộ (Synchronous) và bất đồng bộ (Asynchronous):** Phân tích kịch bản gửi nhận Message qua Message Broker (RabbitMQ / Event Queue).

---

## 2. Kiến trúc Hệ sinh thái Microservices

```
                        ┌────────────────────────────────────────────────────────┐
                        │             HỆ THỐNG SÂN BÓNG ĐÁ (MICROSERVICES)       │
                        └────────────────────────────────────────────────────────┘

          [Client: Postman / Browser]
                     │
                     │ POST /api/payments
                     ▼
         ┌───────────────────────┐   HTTP GET /api/bookings/{id}   ┌───────────────────────┐
         │    Payment Service    │ ──────────────────────────────> │    Booking Service    │
         │  (Spring Boot @ 8082) │ <────────────────────────────── │  (Spring Boot @ 8081) │
         │  [Service Consumer]   │           JSON Booking          │   [Service Provider]  │
         └──────────┬────────────┘                                 │    (Đã có từ Buổi 1)  │
                    │                                              └───────────────────────┘
                    │ Event: PaymentCreated                                    ▲
                    ▼                                                          │ HTTP GET
         ┌───────────────────────┐                                 ┌───────────┴───────────┐
         │    RabbitMQ Queue     │                                 │ Notification Service  │
         │    (Message Broker)   │                                 │  (Node.js @ Port 8083)│
         └──────────┬────────────┘                                 │  [Multi-platform Test]│
                    │ Consume event                                └───────────────────────┘
                    ▼
         ┌───────────────────────┐
         │  SMS / Email Service  │
         └───────────────────────┘
```

---

## 3. Chi tiết nội dung thực hiện (Đã làm gì và làm như thế nào)

### BÀI 1: Khảo sát giao tiếp giữa các service (20 phút)

* **Đã làm gì:**
  - Khảo sát các phụ thuộc (dependencies) trong hệ thống sân bóng: Khi một giao dịch thanh toán tiền sân diễn ra (`Payment Service`), hệ thống **bắt buộc phải có thông tin lịch đặt sân** từ `Booking Service` để đối soát số tiền và tình trạng sân.
  - Phân tích luồng request/response:
    - **Bên gửi (Consumer):** `Payment Service` gửi request HTTP GET sang `http://localhost:8081/api/bookings/{id}`.
    - **Bên nhận (Provider):** `Booking Service` tiếp nhận request, tra cứu trong bộ nhớ và phản hồi JSON.
    - **Định dạng dữ liệu:** JSON (`application/json`).
    - **Phân tích rủi ro:** Nếu `Booking Service` bị tắt hoặc mất mạng, `Payment Service` không thể kiểm tra lịch đặt sân. Cần có cơ chế bắt lỗi mất kết nối (Connection Refused), xử lý timeout và fallback thay vì để sập hệ thống.

---

### BÀI 2: Service-to-Service Communication bằng Spring Boot (35 phút)

* **Đã làm gì:**
  - Giữ nguyên `booking-service` chạy tại cổng `8081` làm Service Provider.
  - Khởi tạo mới `payment-service` chạy tại cổng `8082` làm Service Consumer.
  - Định nghĩa thực thể `Payment`, enum `PaymentMethod` (VNPAY, MOMO, CASH, BANKING) và `PaymentStatus` (SUCCESS, PENDING, FAILED).
  - Sử dụng công nghệ **`RestClient`** (chuẩn HTTP Client hiện đại nhất của Spring Boot 3.x) để gọi liên dịch vụ.
* **Làm như thế nào:**
  1. Cấu hình `RestClient` bean với timeout trong [RestClientConfig.java](file:///c:/Users/abinh/Downloads/football-booking-service/payment-service/src/main/java/com/football/paymentservice/config/RestClientConfig.java):
     ```java
     SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
     factory.setConnectTimeout(Duration.ofMillis(2000));
     factory.setReadTimeout(Duration.ofMillis(3000));

     return RestClient.builder()
             .baseUrl(bookingServiceBaseUrl)
             .requestFactory(factory)
             .build();
     ```
  2. Tạo [BookingClient.java](file:///c:/Users/abinh/Downloads/football-booking-service/payment-service/src/main/java/com/football/paymentservice/client/BookingClient.java) bọc việc gọi API:
     ```java
     public BookingDTO getBookingById(Long bookingId) {
         return bookingRestClient.get()
                 .uri("/api/bookings/{id}", bookingId)
                 .retrieve()
                 .onStatus(status -> status.value() == 404, (req, res) -> {
                     throw new ResourceNotFoundException("Không tìm thấy lịch đặt sân id=" + bookingId);
                 })
                 .body(BookingDTO.class);
     }
     ```
  3. Xây dựng API `POST /api/payments`:
     - Tiếp nhận `PaymentRequest` (gồm `bookingId`, `amount`, `paymentMethod`).
     - Gọi `BookingClient` để xác thực lịch đặt sân.
     - Nếu tồn tại $\rightarrow$ Tạo giao dịch thanh toán thành công, trả về HTTP `201 Created` kèm thông tin kết hợp của cả Payment và Booking.
     - Nếu không tồn tại $\rightarrow$ Trả về HTTP `404 Not Found`.

---

### BÀI 3: Tiêu thụ REST API bằng Framework khác (Node.js) (35 phút)

* **Mục tiêu:** Chứng minh REST API là độc lập nền tảng, không bị trói buộc vào Spring Boot hay Java.
* **Đã làm gì:**
  - Xây dựng `notification-service` hoàn toàn bằng **Node.js** chạy tại cổng `8083`.
  - Service này cung cấp API `GET /api/notifications/verify-booking/:id`: Khi nhận yêu cầu, Node.js sẽ dùng hàm `fetch()` gọi trực tiếp sang Spring Boot `booking-service` ở cổng `8081` để lấy thông tin đặt sân và tạo nội dung tin nhắn SMS xác nhận.
* **Bảng so sánh Spring Boot vs Node.js:**

| Tiêu chí | Spring Boot (Java) | Node.js (JavaScript) |
| :--- | :--- | :--- |
| **Ngôn ngữ** | Java (Static typing, hướng đối tượng chặt chẽ) | JavaScript (Dynamic typing, Event-driven) |
| **Tạo REST API** | `@RestController`, `@GetMapping`, Spring MVC | `http.createServer()` hoặc framework Express |
| **HTTP Client** | `RestClient`, `WebClient`, `RestTemplate` | `fetch()`, `axios`, module `http` |
| **Quản lý Dependency** | Maven (`pom.xml`) / Gradle | npm (`package.json`) |
| **Thời gian khởi động** | Vài giây (2 - 4 giây do nạp IoC Container) | Gần như tức thì (< 0.5 giây) |
| **Mức độ phức tạp** | Cao hơn, kiến trúc chặt chẽ, phù hợp hệ thống lớn | Nhẹ, linh hoạt, viết nhanh cho microservice nhỏ |

---

### BÀI 4: Xử lý lỗi phân tán, Timeout và Giao tiếp bất đồng bộ (40 phút)

#### 4.1. Xử lý lỗi gọi liên dịch vụ (Error Handling)
Được quản lý tập trung qua [GlobalExceptionHandler.java](file:///c:/Users/abinh/Downloads/football-booking-service/payment-service/src/main/java/com/football/paymentservice/exception/GlobalExceptionHandler.java):
* **Tình huống 1 — Lịch đặt không tồn tại (404):** Bắt `ResourceNotFoundException`, trả về HTTP `404 Not Found`.
* **Tình huống 2 — Booking Service bị tắt (Connection Refused):**
  - Khi tắt `booking-service` ở cổng 8081, `RestClient` ném ra `ResourceAccessException`.
  - Bắt lỗi và chuyển đổi thành HTTP `503 Service Unavailable`:
    ```json
    {
      "status": 503,
      "error": "Service Unavailable",
      "message": "Không thể kết nối tới Booking Service tại http://localhost:8081 (Dịch vụ đang tắt hoặc mất kết nối)",
      "suggestion": "Vui lòng kiểm tra Booking Service đã khởi động tại cổng 8081 chưa"
    }
    ```

#### 4.2. Xử lý Timeout (504 Gateway Timeout)
* Cấu hình `readTimeout = 3000ms` (3 giây).
* Nếu `Booking Service` bị treo hoặc xử lý quá 3 giây, `SocketTimeoutException` sẽ được bắt và chuyển đổi thành HTTP `504 Gateway Timeout`:
  ```json
  {
    "status": 504,
    "error": "Gateway Timeout",
    "message": "Booking Service phản hồi quá thời gian quy định (Timeout > 3s)"
  }
  ```

#### 4.3. Mô hình Giao tiếp Bất đồng bộ (RabbitMQ / Message Broker)
* **Khác biệt cốt lõi:**
  - *Đồng bộ (Synchronous):* `Payment Service` $\rightarrow$ `Booking Service` (bắt buộc phải chờ có kết quả xác thực đặt sân thì mới tạo thanh toán được).
  - *Bất đồng bộ (Asynchronous):* Sau khi thanh toán xong, việc gửi hóa đơn hay SMS cho khách hàng **không cần bắt khách hàng phải chờ**. `Payment Service` chỉ cần đẩy 1 Event `PaymentCompleted` vào Message Broker (RabbitMQ), sau đó trả về kết quả thành công ngay cho khách. `Notification Service` ở phía sau sẽ nhận message và gửi SMS độc lập.
* **Cấu trúc Message:**
  ```json
  {
    "event": "PaymentCompleted",
    "transactionCode": "TXN_1728300000",
    "bookingId": 1,
    "amount": 300000.0,
    "customerPhone": "0901234567"
  }
  ```

---

### BÀI 5: Thử thách mở rộng (20 phút)

1. **Config-driven URL (Cấu hình linh hoạt):**
   - Không hard-code `http://localhost:8081` trong code Java.
   - Định nghĩa trong `application.properties`:
     ```properties
     booking.service.base-url=http://localhost:8081
     booking.service.connect-timeout=2000
     booking.service.read-timeout=3000
     ```
   - Nạp vào ứng dụng thông qua `@Value("${booking.service.base-url}")`. Khi triển khai sang môi trường Docker hoặc máy chủ khác, chỉ cần đổi file config.
2. **Cơ chế Fallback (Xử lý suy giảm nhẹ):**
   - Cung cấp endpoint dự phòng: `POST /api/payments/fallback`.
   - Khi `Booking Service` bị mất kết nối, hệ thống không làm gián đoạn người dùng mà lưu giao dịch ở trạng thái `PENDING` (chờ đối soát) và trả về HTTP `202 Accepted`:
     ```json
     {
       "payment": {
         "id": 1,
         "bookingId": 1,
         "status": "PENDING",
         "transactionCode": "TXN_FALLBACK_1728300000",
         "note": "Thanh toán tạm ghi nhận (Booking Service ngoại tuyến, chờ đối soát)"
       },
       "message": "Booking Service hiện chưa phản hồi. Giao dịch đã được lưu tạm ở trạng thái PENDING để xử lý sau."
     }
     ```

---

## 4. Hướng dẫn khởi chạy toàn bộ hệ thống

Hệ thống gồm 3 microservice độc lập, chạy trên 3 terminal riêng biệt:

### Bước 1: Chạy Service Provider (`booking-service` - Port 8081)
```bash
cd football-booking-service/football-booking-service
.\mvnw.cmd spring-boot:run
```

### Bước 2: Chạy Service Consumer (`payment-service` - Port 8082)
```bash
cd football-booking-service/payment-service
.\mvnw.cmd spring-boot:run
```

### Bước 3: Chạy Multi-platform Service (`notification-service` - Port 8083)
```bash
cd football-booking-service/notification-service
node server.js
```

---

## 5. Kịch bản kiểm thử toàn diện (Postman Collection)

### TC01: Kiểm tra trạng thái các service
* `GET http://localhost:8081/api/bookings/info` $\rightarrow$ `200 OK` (Booking Service UP)
* `GET http://localhost:8082/api/payments/info` $\rightarrow$ `200 OK` (Payment Service UP)
* `GET http://localhost:8083/api/notifications/info` $\rightarrow$ `200 OK` (Notification Service UP)

### TC02: Thanh toán thành công (Service-to-Service hoạt động chuẩn)
* **Method:** `POST http://localhost:8082/api/payments`
* **Request Body:**
  ```json
  {
    "bookingId": 1,
    "amount": 300000.0,
    "paymentMethod": "VNPAY",
    "note": "Thanh toán cọc sân số 1"
  }
  ```
* **Kết quả:** HTTP `201 Created`
  ```json
  {
    "payment": {
      "id": 1,
      "bookingId": 1,
      "amount": 300000.0,
      "paymentMethod": "VNPAY",
      "status": "SUCCESS",
      "transactionCode": "TXN_1728312345"
    },
    "booking": {
      "id": 1,
      "customerName": "Nguyen Van An",
      "phone": "0901234567",
      "fieldName": "San so 1",
      "price": 300000.0
    },
    "message": "Thanh toán thành công! Lịch đặt sân đã được xác minh qua Booking Service."
  }
  ```

### TC03: Lịch đặt sân không tồn tại (404 Not Found)
* **Method:** `POST http://localhost:8082/api/payments`
* **Body:** `"bookingId": 9999`
* **Kết quả:** HTTP `404 Not Found`
  ```json
  {
    "status": 404,
    "message": "Không tìm thấy lịch đặt sân id=9999 để thanh toán"
  }
  ```

### TC04: Booking Service bị tắt (503 Service Unavailable)
* **Thực hiện:** Tắt terminal của `booking-service` (cổng 8081), sau đó gửi lại POST thanh toán.
* **Kết quả:** HTTP `503 Service Unavailable`
  ```json
  {
    "status": 503,
    "error": "Service Unavailable",
    "message": "Không thể kết nối tới Booking Service tại http://localhost:8081 (Dịch vụ đang tắt hoặc mất kết nối)"
  }
  ```

### TC05: Node.js tiêu thụ Spring Boot API (Đa nền tảng)
* **Method:** `GET http://localhost:8083/api/notifications/verify-booking/1`
* **Kết quả:** Node.js gọi thành công sang Spring Boot và trả về:
  ```json
  {
    "service": "Notification Service (Node.js)",
    "communication": "HTTP GET -> Spring Boot (Port 8081)",
    "success": true,
    "verifiedBooking": {
      "customerName": "Nguyen Van An",
      "fieldName": "San so 1"
    },
    "notificationPreview": "Gui SMS den 0901234567: Ban da dat thanh cong san San so 1!"
  }
  ```

### TC06: Kịch bản Fallback khi Booking Service gián đoạn
* **Method:** `POST http://localhost:8082/api/payments/fallback`
* **Kết quả:** HTTP `202 Accepted`, thanh toán được ghi nhận tạm thời ở trạng thái `PENDING`.

---

## 6. Exit Ticket — Trả lời 5 câu hỏi cốt lõi cuối buổi

**Câu 1: REST API và Spring Boot có phải là một khái niệm không? Giải thích.**  
* **Trả lời:** **Không.** REST API là một phong cách kiến trúc phần mềm (Architectural Style) quy định cách giao tiếp qua giao thức HTTP và dữ liệu JSON. Spring Boot chỉ là một trong rất nhiều framework/nền tảng dùng để triển khai REST API. Ta hoàn toàn có thể tạo REST API bằng Node.js, Quarkus, Python (FastAPI), Go, v.v.

**Câu 2: Trong mô hình `Payment -> Booking`, service nào là consumer, service nào là provider?**  
* **Trả lời:** `Payment Service` là **Consumer** (bên chủ động gửi request gọi và tiêu thụ API), còn `Booking Service` là **Provider** (bên cung cấp dữ liệu API).

**Câu 3: Điều gì xảy ra nếu Booking Service bị ngừng hoạt động?**  
* **Trả lời:** `Payment Service` sẽ không thể xác thực lịch đặt sân và gặp lỗi kết nối (`Connection Refused`). Nếu không có cơ chế xử lý lỗi (Exception Handler, Timeout, Fallback), request từ người dùng sẽ bị treo hoặc sập dịch vụ (lỗi 500). Khi đã cấu hình chuẩn, hệ thống sẽ trả về mã `503 Service Unavailable` hoặc kích hoạt cơ chế `Fallback`.

**Câu 4: Phân biệt Synchronous communication và Asynchronous communication?**  
* **Trả lời:**
  * **Synchronous (Đồng bộ):** Bên gọi gửi request và **buộc phải chờ** bên nhận xử lý xong và phản hồi kết quả mới có thể tiếp tục công việc (chặn luồng - blocking).
  * **Asynchronous (Bất đồng bộ):** Bên gọi gửi thông điệp vào hàng đợi (Message Broker) rồi **tiếp tục xử lý ngay lập tức** mà không cần chờ bên nhận xử lý xong (non-blocking).

**Câu 5: Khi nào nên cân nhắc giao tiếp bất đồng bộ thay vì gọi REST trực tiếp?**  
* **Trả lời:** Nên dùng khi:
  1. Nghiệp vụ không cần kết quả phản hồi ngay lập tức (gửi email/SMS thông báo, ghi log kiểm toán, sinh báo cáo).
  2. Cần tách rời (decouple) mức độ phụ thuộc giữa các service: Ngay cả khi service nhận bị sập, message vẫn được lưu an toàn trong Broker và sẽ xử lý sau khi service khởi động lại.
  3. Cần làm mượt tải (load leveling) trong các đợt cao điểm lưu lượng truy cập.

---

## 7. Bảng nghiệm thu năng lực (Learning Evidence)

| Tiêu chí đánh giá | Kết quả đạt được | Trạng thái |
| :--- | :--- | :--- |
| **Mô hình Service-to-Service** | Sơ đồ và luồng giao tiếp giữa `payment-service` và `booking-service` | **Đạt** |
| **Xây dựng Service Consumer (Spring Boot)** | Triển khai `RestClient` trong `payment-service` tại cổng 8082 | **Đạt** |
| **Đa nền tảng (Multi-platform)** | `notification-service` viết bằng Node.js gọi thành công sang Spring Boot | **Đạt** |
| **Xử lý lỗi phân tán** | Phân biệt và xử lý rõ các mã: `201`, `404`, `503`, `504 Timeout` | **Đạt** |
| **Giao tiếp bất đồng bộ** | Phân tích rõ vai trò Message Broker và thiết kế Event Message | **Đạt** |
| **Mở rộng nâng cao** | Cấu hình Config-driven URL, cơ chế Fallback xử lý khi gián đoạn | **Vượt chỉ tiêu** |
| **Exit Ticket** | Hoàn thành đầy đủ 5/5 câu hỏi lý thuyết kiến trúc dịch vụ | **Đạt** |
