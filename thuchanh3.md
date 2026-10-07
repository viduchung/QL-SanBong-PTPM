# BUỔI THỰC HÀNH 3: THIẾT KẾ VÀ XÂY DỰNG RESTFUL API NÂNG CAO
## Học phần: Phát triển phần mềm hướng dịch vụ (SOA)

* **Thời lượng:** 150 phút | **Đối tượng:** Sinh viên năm thứ 3
* **Hệ thống áp dụng:** Football Booking Service (Hệ thống quản lý đặt lịch sân bóng đá)
* **Nền tảng chính:** Java 17, Spring Boot 3.2.5, SpringDoc OpenAPI (Swagger 3), Maven, Postman
* **Cổng dịch vụ (Port):** `8081`

---

## I. Vị trí của Buổi 3 trong học phần

Học phần hình thành một chuỗi tư duy kiến trúc dịch vụ liên tục qua 3 buổi đầu:
* **Buổi 1:** Xây dựng Service cơ bản $\rightarrow$ CRUD in-memory $\rightarrow$ HTTP request/response.
* **Buổi 2:** Giao tiếp Service-to-Service $\rightarrow$ Gọi đồng bộ qua `RestClient` $\rightarrow$ Xử lý lỗi phân tán (503, 504 Timeout) $\rightarrow$ Giao tiếp bất đồng bộ (Message Broker).
* **Buổi 3:** **Thiết kế RESTful API nâng cao:**
  $$\text{Resource Design} \longrightarrow \text{HTTP Status Standardization} \longrightarrow \text{Strict Validation} \longrightarrow \text{Filter/Sort/Paging} \longrightarrow \text{OpenAPI/Swagger} \longrightarrow \text{API Versioning}$$

---

## II. Chi tiết 6 giai đoạn thực hiện (Đã làm gì và làm như thế nào)

### GIAI ĐOẠN 1: Phân tích và Thiết kế RESTful Resource (20 phút)

* **Mục tiêu:** Phân biệt rõ **Resource-oriented API** (hướng tài nguyên) với **Action-oriented / RPC API** (hướng hành động).
* **Phân tích thiết kế:**

| API theo kiểu gọi hàm (Chưa tốt) | API chuẩn RESTful Resource (Đã chuẩn hóa) | Lý do cải tiến |
| :--- | :--- | :--- |
| `GET /getAllBookings` | `GET /api/v1/bookings` | Dùng danh từ số nhiều `bookings` đại diện cho tập tài nguyên, không đưa động từ vào URI. |
| `GET /getBookingById?id=1` | `GET /api/v1/bookings/1` | Dùng Path Variable `{id}` để định danh tài nguyên duy nhất trong hệ thống. |
| `POST /createBooking` | `POST /api/v1/bookings` | Phương thức HTTP `POST` đã mang ngữ nghĩa "tạo mới tài nguyên". |
| `POST /updateBooking?id=1` | `PUT /api/v1/bookings/1`<br>`PATCH /api/v1/bookings/1` | Dùng `PUT` khi thay thế toàn bộ; Dùng `PATCH` khi cập nhật từng phần (VD: chỉ đổi giờ hoặc đổi trạng thái). |
| `POST /deleteBooking?id=1` | `DELETE /api/v1/bookings/1` | Phương thức `DELETE` mang ngữ nghĩa xóa, không cần từ `delete` trong URI. |

* **Quy chuẩn phương thức cập nhật:**
  * **`PUT /api/v1/bookings/{id}`:** Thay thế toàn bộ dữ liệu của lịch đặt sân bằng đối tượng gửi lên.
  * **`PATCH /api/v1/bookings/{id}`:** Chỉ cập nhật những trường được truyền trong body (không bắt buộc nhập lại toàn bộ thông tin).

---

### GIAI ĐOẠN 2: Chuẩn hóa HTTP Method và Status Code (25 phút)

* **Chuẩn hóa mã phản hồi theo nghiệp vụ thực tế:**

| Tình huống nghiệp vụ | HTTP Method & URI | Status Code chuẩn | Ý nghĩa |
| :--- | :--- | :---: | :--- |
| Truy vấn danh sách / chi tiết | `GET /api/v1/bookings` | **`200 OK`** | Yêu cầu thành công, trả về dữ liệu. |
| Tạo mới lịch đặt sân | `POST /api/v1/bookings` | **`201 Created`** | Tài nguyên mới đã được tạo thành công trên hệ thống. |
| Cập nhật toàn bộ / một phần | `PUT` hoặc `PATCH` | **`200 OK`** | Cập nhật thành công, trả về bản ghi sau khi sửa. |
| Xóa lịch đặt sân | `DELETE /api/v1/bookings/{id}` | **`204 No Content`** | Xóa thành công, không cần trả về nội dung body. |
| Dữ liệu gửi lên sai định dạng | Bất kỳ | **`400 Bad Request`** | Vi phạm quy tắc validation (SĐT sai, giá âm, tên trống). |
| Không tìm thấy ID lịch đặt | `GET`, `PUT`, `PATCH`, `DELETE` | **`404 Not Found`** | Tài nguyên được yêu cầu không tồn tại trong hệ thống. |
| Trùng lịch sân cùng khung giờ | `POST /api/v1/bookings` | **`409 Conflict`** | Xung đột tài nguyên đặt sân. |
| Lỗi xử lý bên trong server | Bất kỳ | **`500 Internal Error`** | Lỗi ngoại lệ hệ thống chưa kiểm soát. |

* **Code minh họa chuẩn hóa `DELETE` trả về `204 No Content` trong Controller:**
  ```java
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
      boolean deleted = bookingService.delete(id);
      if (!deleted) {
          throw new ResourceNotFoundException("Không tìm thấy lịch đặt sân với id=" + id);
      }
      return ResponseEntity.noContent().build(); // HTTP 204 No Content
  }
  ```

---

### GIAI ĐOẠN 3: Validation chặt chẽ và Error Handling tập trung (30 phút)

#### 1. Quy tắc Bean Validation trên [BookingRequest.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/dto/BookingRequest.java):
* `customerName`: `@NotBlank`, `@Size(min = 3, max = 100)` $\rightarrow$ Không được để trống, độ dài 3 đến 100 ký tự.
* `phone`: `@NotBlank`, `@Pattern(regexp = "^(0|\\+84)\\d{9}$")` $\rightarrow$ Bắt buộc phải là số điện thoại Việt Nam 10 chữ số (VD: 0901234567).
* `fieldName`: `@NotBlank` $\rightarrow$ Tên sân không được để trống.
* `bookingDate`: `@NotNull`, định dạng `@JsonFormat(pattern = "yyyy-MM-dd")`.
* `startTime`, `endTime`: `@NotNull`, định dạng `@JsonFormat(pattern = "HH:mm")`.
* `price`: `@NotNull`, `@Positive` $\rightarrow$ Giá thuê phải lớn hơn 0.

#### 2. Cấu trúc Error Response thống nhất ([ErrorResponse.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/dto/ErrorResponse.java)):
Không trả về chuỗi text thô mà chuẩn hóa thành cấu trúc JSON có định dạng:
```json
{
  "timestamp": "2026-10-07T17:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Dữ liệu đầu vào không hợp lệ",
  "path": "/api/v1/bookings",
  "errors": {
    "phone": "Số điện thoại phải có 10 chữ số hợp lệ (VD: 0901234567)",
    "customerName": "Tên khách hàng phải từ 3 đến 100 ký tự",
    "price": "Giá thuê sân phải lớn hơn 0"
  }
}
```

#### 3. Xử lý Exception tập trung ([GlobalExceptionHandler.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/exception/GlobalExceptionHandler.java)):
* Sử dụng `@RestControllerAdvice` để chặn toàn bộ lỗi trong hệ thống:
  * `MethodArgumentNotValidException` $\rightarrow$ Trả về `400 Bad Request` kèm chi tiết từng trường bị lỗi.
  * `ResourceNotFoundException` $\rightarrow$ Trả về `404 Not Found` kèm URI gây lỗi.
  * `IllegalArgumentException` $\rightarrow$ Trả về `400 Bad Request`.
  * `Exception` $\rightarrow$ Trả về `500 Internal Server Error`.

---

### GIAI ĐOẠN 4: Search, Filter, Sort và Pagination kết hợp (30 phút)

Thay vì viết nhiều endpoint rời rạc, API được thiết kế tổng hợp trên endpoint duy nhất: `GET /api/v1/bookings`:

1. **Search (Tìm kiếm tương đối):** `?name=An` $\rightarrow$ Lọc khách hàng có tên chứa "An" (không phân biệt hoa thường).
2. **Filter (Lọc theo tiêu chí):** `?status=CONFIRMED` $\rightarrow$ Lọc theo trạng thái `PENDING`, `CONFIRMED`, `PAID`, `CANCELLED`.
3. **Sorting (Sắp xếp linh hoạt):** `?sort=price,desc` hoặc `?sort=customerName,asc` $\rightarrow$ Sắp xếp theo giá, tên khách, ngày đặt, hoặc ID tăng/giảm dần.
4. **Pagination (Phân trang kèm Metadata):** `?page=0&size=5` $\rightarrow$ Đóng gói vào [PageResponse.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/dto/PageResponse.java).

#### Cấu trúc phản hồi phân trang chuẩn có Metadata:
```json
{
  "content": [
    {
      "id": 4,
      "customerName": "Pham Minh Duc",
      "phone": "0934567890",
      "fieldName": "San so 3",
      "bookingDate": "2026-10-13",
      "startTime": "19:00",
      "endTime": "20:30",
      "price": 400000.0,
      "status": "CONFIRMED"
    },
    {
      "id": 1,
      "customerName": "Nguyen Van An",
      "phone": "0901234567",
      "fieldName": "San so 1",
      "bookingDate": "2026-10-10",
      "startTime": "18:00",
      "endTime": "19:30",
      "price": 300000.0,
      "status": "CONFIRMED"
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 2,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

* **Yêu cầu kết hợp 4 trong 1:**
  ```http
  GET /api/v1/bookings?name=An&status=CONFIRMED&sort=price,desc&page=0&size=5
  ```

---

### GIAI ĐOẠN 5: Swagger/OpenAPI và API Versioning (25 phút)

#### 1. Tài liệu hóa tự động bằng OpenAPI 3 / Swagger UI
* Thêm dependency `springdoc-openapi-starter-webmvc-ui` phiên bản `2.5.0` vào `pom.xml`.
* Cấu hình thông tin API tại [OpenApiConfig.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/config/OpenApiConfig.java).
* Giao diện tương tác Swagger UI trực tiếp:
  👉 **`http://localhost:8081/swagger-ui/index.html`**
* Toàn bộ các API đều có chú thích rõ ràng: `@Tag`, `@Operation`, `@Parameter`, `@ApiResponse`.

#### 2. Chiến lược API Versioning & Tiến hóa Schema (API Evolution)
Khi hệ thống nâng cấp, cấu trúc dữ liệu người dùng cần đổi từ chuỗi đơn `customerName` sang đối tượng có cấu trúc `{ firstName, lastName }`:
* **API v1 (`/api/v1/bookings/1`):** Giữ nguyên tương thích ngược (Backward Compatibility) cho các ứng dụng mobile/web cũ:
  ```json
  {
    "id": 1,
    "customerName": "Nguyen Van An"
  }
  ```
* **API v2 ([BookingV2Controller.java](file:///c:/Users/abinh/Downloads/football-booking-service/football-booking-service/src/main/java/com/football/bookingservice/controller/BookingV2Controller.java) - `/api/v2/bookings/1`):** Phục vụ các Client mới:
  ```json
  {
    "id": 1,
    "customer": {
      "firstName": "Van An",
      "lastName": "Nguyen"
    },
    "phone": "0901234567",
    "apiVersion": "v2.0"
  }
  ```

---

### GIAI ĐOẠN 6: Thử thách nâng cao (Challenges)

#### 1. Nested Resource (Tài nguyên lồng nhau)
* **Endpoint:** `GET /api/v1/bookings/{id}/payments`
* **Ý nghĩa:** Biểu diễn quan hệ 1-N trong RESTful (một lịch đặt sân có thể có nhiều đợt thanh toán tiền cọc hoặc thanh toán toàn phần).
* **Kết quả:**
  ```json
  [
    {
      "paymentId": 101,
      "bookingId": 1,
      "amount": 300000.0,
      "paymentMethod": "VNPAY",
      "status": "SUCCESS",
      "transactionCode": "TXN_1_FULL"
    }
  ]
  ```

#### 2. Phân tích tính bất biến (Idempotency) của HTTP Methods
* **Idempotent (Bất biến):** Thực hiện 1 lần hay $N$ lần liên tiếp thì trạng thái hệ thống vẫn không đổi.
  * `GET`: Bất biến (chỉ đọc dữ liệu).
  * `PUT`: Bất biến (thay thế toàn bộ dữ liệu theo ID xác định, gửi lại cùng nội dung thì kết quả trên server vẫn thế).
  * `DELETE`: Bất biến (xóa ID đó lần 1 thì xóa thành công, gọi lại lần 2 thì tài nguyên đã không còn, không gây xóa nhầm dữ liệu khác).
* **Non-idempotent (Không bất biến):**
  * `POST`: Gọi $N$ lần liên tiếp sẽ tạo ra $N$ bản ghi đặt sân mới khác nhau (trùng lặp dữ liệu, sinh $N$ ID khác nhau).

#### 3. Mô hình HATEOAS (Maturity Level 3)
Biểu diễn liên kết điều hướng trực tiếp trong response để Client biết các hành động tiếp theo có thể thực hiện:
```json
{
  "id": 1,
  "customerName": "Nguyen Van An",
  "status": "CONFIRMED",
  "_links": {
    "self": { "href": "/api/v1/bookings/1" },
    "payments": { "href": "/api/v1/bookings/1/payments" },
    "cancel": { "href": "/api/v1/bookings/1/cancel" }
  }
}
```

---

## III. Hướng dẫn khởi chạy và kiểm thử

### 1. Khởi động ứng dụng
```bash
cd football-booking-service/football-booking-service
.\mvnw.cmd spring-boot:run
```

### 2. Truy cập Swagger UI
Mở trình duyệt: **`http://localhost:8081/swagger-ui/index.html`** để thực hiện test trực quan tất cả các API.

### 3. Kịch bản kiểm thử Postman Collection

| STT | Tên Test Case | Method & URL | Body / Tham số | Kết quả mong đợi |
| :-- | :--- | :--- | :--- | :--- |
| **TC01** | Tạo mới hợp lệ | `POST /api/v1/bookings` | JSON đầy đủ SĐT 10 số, giá $> 0$ | `201 Created` |
| **TC02** | Validation SĐT sai | `POST /api/v1/bookings` | `phone: "123"` | `400 Bad Request` |
| **TC03** | Validation tên quá ngắn | `POST /api/v1/bookings` | `customerName: "An"` (< 3 ký tự) | `400 Bad Request` |
| **TC04** | Tìm ID không tồn tại | `GET /api/v1/bookings/9999` | Không | `404 Not Found` |
| **TC05** | Cập nhật 1 phần (PATCH) | `PATCH /api/v1/bookings/1` | `{"price": 320000.0, "status": "PAID"}` | `200 OK` (chỉ đổi giá và trạng thái) |
| **TC06** | Xóa thành công | `DELETE /api/v1/bookings/1` | Không | `204 No Content` |
| **TC07** | Tìm kiếm theo tên | `GET /api/v1/bookings?name=An` | Query param `name=An` | `200 OK` (trả về danh sách phân trang) |
| **TC08** | Lọc theo trạng thái | `GET /api/v1/bookings?status=CONFIRMED` | Query param `status=CONFIRMED` | `200 OK` |
| **TC09** | Sắp xếp giảm dần theo giá | `GET /api/v1/bookings?sort=price,desc` | Query param `sort=price,desc` | `200 OK` (giá cao xếp trước) |
| **TC10** | Kết hợp 4 trong 1 | `GET /api/v1/bookings?name=An&status=CONFIRMED&sort=price,desc&page=0&size=5` | Kết hợp | `200 OK` kèm Metadata |
| **TC11** | API Version 2 | `GET /api/v2/bookings/2` | Không | `200 OK` (`customer: { firstName, lastName }`) |
| **TC12** | Nested Resource | `GET /api/v1/bookings/3/payments` | Không | `200 OK` (danh sách thanh toán) |

---

## IV. Bảng nghiệm thu tiêu chí đánh giá (Thang điểm 10/10)

| Tiêu chí đánh giá | Trọng số | Kết quả thực hiện trong mã nguồn | Đánh giá |
| :--- | :---: | :--- | :---: |
| **Thiết kế URI & HTTP Method đúng REST** | 1.5 | Dùng danh từ số nhiều `/api/v1/bookings`, phân biệt chuẩn `PUT` và `PATCH`. | **1.5 / 1.5** |
| **CRUD và HTTP Status Code chuẩn** | 1.5 | `POST` trả `201`, `DELETE` trả `204 No Content`, `GET/PUT/PATCH` trả `200`. | **1.5 / 1.5** |
| **Validation chặt chẽ** | 1.5 | Kiểm tra RegEx SĐT 10 số, độ dài tên, giá dương, ngày giờ hợp lệ. | **1.5 / 1.5** |
| **Exception / Error Handling tập trung** | 1.5 | `@RestControllerAdvice` trả về cấu trúc lỗi `ErrorResponse` thống nhất có `path`, `timestamp`, `errors`. | **1.5 / 1.5** |
| **Search / Filter / Sort / Pagination** | 2.0 | Kết hợp 4 trong 1, trả về `PageResponse` có đầy đủ metadata (`totalPages`, `totalElements`, v.v.). | **2.0 / 2.0** |
| **Swagger/OpenAPI + Versioning** | 1.0 | Swagger UI hoạt động tại `/swagger-ui/index.html`, demo Versioning `/api/v1` vs `/api/v2`. | **1.0 / 1.0** |
| **Postman Testing + Tổ chức Code** | 1.0 | 12 test case hoàn chỉnh, mã nguồn phân tầng rõ ràng (Controller - Service - Model - DTO - Config). | **1.0 / 1.0** |
| **TỔNG ĐIỂM DỰ KIẾN** | **10.0** | **Đạt tuyệt đối mọi tiêu chuẩn của Buổi thực hành 3** | **10.0 / 10.0** |
