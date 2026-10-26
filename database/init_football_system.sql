-- =====================================================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT LỊCH SÂN BÓNG ĐÁ (MICROSERVICES ARCHITECTURE)
-- BUỔI THỰC HÀNH 4: QUẢN LÝ DỮ LIỆU VÀ KẾT NỐI DỊCH VỤ (DATABASE-PER-SERVICE)
-- Hệ quản trị CSDL: MySQL 8.x
-- =====================================================================================

-- =====================================================================================
-- 1. DATABASE DÀNH CHO BOOKING SERVICE: football_booking_db
-- Cổng dịch vụ sở hữu: Booking Service (Port 8081)
-- =====================================================================================
CREATE DATABASE IF NOT EXISTS `football_booking_db` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `football_booking_db`;

-- Xóa bảng cũ nếu tồn tại để reset dữ liệu sạch
DROP TABLE IF EXISTS `bookings`;

CREATE TABLE `bookings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `customer_name` VARCHAR(100) NOT NULL COMMENT 'Họ và tên khách hàng (Người thuê)',
    `phone` VARCHAR(15) NOT NULL COMMENT 'Số điện thoại liên hệ (10 chữ số)',
    `field_name` VARCHAR(100) NOT NULL COMMENT 'Tên sân bóng (San 7 nguoi so 1, ...)',
    `booking_date` DATE NOT NULL COMMENT 'Ngày đặt sân (yyyy-MM-dd)',
    `start_time` TIME NOT NULL COMMENT 'Giờ bắt đầu thuê sân (HH:mm)',
    `end_time` TIME NOT NULL COMMENT 'Giờ kết thúc thuê sân (HH:mm)',
    `price` DOUBLE NOT NULL COMMENT 'Giá thuê sân (VNĐ), bắt buộc > 0',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái: PENDING, CONFIRMED, PAID, CANCELLED',
    `username` VARCHAR(50) DEFAULT NULL COMMENT 'Tài khoản người dùng sở hữu lịch đặt (Phục vụ Resource Ownership)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm tạo bản ghi',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời điểm cập nhật cuối',
    PRIMARY KEY (`id`),
    INDEX `idx_booking_customer` (`customer_name`),
    INDEX `idx_booking_username` (`username`),
    INDEX `idx_booking_date_field` (`booking_date`, `field_name`),
    INDEX `idx_booking_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng lưu thông tin lịch đặt sân bóng';

-- Bảng tài khoản người dùng (Buổi thực hành 5: Dịch vụ xác thực và bảo mật)
DROP TABLE IF EXISTS `users`;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng tài khoản người dùng hệ thống';

-- Chèn dữ liệu tài khoản mẫu (Mật khẩu mặc định: password123 đã băm BCrypt)
-- 1. customer01 (Người thuê sân 1)
-- 2. customer02 (Người thuê sân 2)
-- 3. owner01 (Chủ sân bóng)
-- 4. admin01 (Quản trị viên hệ thống)
INSERT INTO `users` (`id`, `username`, `password_hash`, `role`, `full_name`, `phone`, `active`) VALUES
(1, 'customer01', '$2a$10$fS0cZc9oI6m2UqQ6h2iHse1Bv0a/lXfXkY0o.L2U9m2kPq9XW6m2K', 'ROLE_CUSTOMER', 'Nguyen Van An', '0901234567', TRUE),
(2, 'customer02', '$2a$10$fS0cZc9oI6m2UqQ6h2iHse1Bv0a/lXfXkY0o.L2U9m2kPq9XW6m2K', 'ROLE_CUSTOMER', 'Tran Thi Binh', '0912345678', TRUE),
(3, 'owner01', '$2a$10$fS0cZc9oI6m2UqQ6h2iHse1Bv0a/lXfXkY0o.L2U9m2kPq9XW6m2K', 'ROLE_FIELD_OWNER', 'Le Chu San', '0988888888', TRUE),
(4, 'admin01', '$2a$10$fS0cZc9oI6m2UqQ6h2iHse1Bv0a/lXfXkY0o.L2U9m2kPq9XW6m2K', 'ROLE_ADMIN', 'Admin Tong', '0999999999', TRUE);

-- Chèn dữ liệu mẫu ban đầu cho bảng bookings (kèm username sở hữu)
INSERT INTO `bookings` (`id`, `customer_name`, `phone`, `field_name`, `booking_date`, `start_time`, `end_time`, `price`, `status`, `username`) VALUES
(1, 'Nguyen Van An', '0901234567', 'San 7 nguoi so 1', '2026-10-10', '18:00:00', '19:30:00', 300000.0, 'CONFIRMED', 'customer01'),
(2, 'Tran Thi Binh', '0912345678', 'San 7 nguoi so 2', '2026-10-11', '20:00:00', '21:30:00', 350000.0, 'PENDING', 'customer02'),
(3, 'Le Van Cuong', '0923456789', 'San 7 nguoi so 1', '2026-10-12', '17:30:00', '19:00:00', 280000.0, 'PAID', 'customer01'),
(4, 'Pham Minh Duc', '0934567890', 'San 11 nguoi VIP', '2026-10-13', '19:00:00', '20:30:00', 400000.0, 'CONFIRMED', 'customer02'),
(5, 'Hoang Thu Ha', '0945678901', 'San 5 nguoi so 2', '2026-10-14', '16:00:00', '17:30:00', 250000.0, 'CANCELLED', 'customer01'),
(6, 'Do Van Hung', '0956789012', 'San 7 nguoi so 1', '2026-10-15', '20:30:00', '22:00:00', 450000.0, 'PAID', 'customer02');


-- =====================================================================================
-- 2. DATABASE DÀNH CHO PAYMENT SERVICE: football_payment_db
-- Cổng dịch vụ sở hữu: Payment Service (Port 8082)
-- LƯU Ý KIẾN TRÚC:
--   - Theo chuẩn Microservices (Database-per-Service), database này hoàn toàn độc lập với football_booking_db.
--   - Cột booking_id là LOGICAL ID, KHÔNG tạo FOREIGN KEY vật lý sang bảng bookings ở database khác.
--   - Tính toàn vẹn dữ liệu được kiểm tra qua REST API giữa Payment Service và Booking Service.
-- =====================================================================================
CREATE DATABASE IF NOT EXISTS `football_payment_db` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `football_payment_db`;

DROP TABLE IF EXISTS `payments`;

CREATE TABLE `payments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `booking_id` BIGINT NOT NULL COMMENT 'Logical ID tham chiếu sang Booking Service qua REST API',
    `amount` DOUBLE NOT NULL COMMENT 'Số tiền thanh toán (VNĐ), bắt buộc > 0',
    `payment_method` VARCHAR(20) NOT NULL COMMENT 'Phương thức: VNPAY, MOMO, CASH, BANKING',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái: SUCCESS, PENDING, FAILED',
    `transaction_code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã giao dịch duy nhất (TXN_...)',
    `payment_time` DATETIME NOT NULL COMMENT 'Thời gian thanh toán',
    `note` VARCHAR(255) DEFAULT NULL COMMENT 'Ghi chú giao dịch',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm ghi nhận hệ thống',
    PRIMARY KEY (`id`),
    INDEX `idx_payment_booking_id` (`booking_id`),
    INDEX `idx_payment_status` (`status`),
    INDEX `idx_payment_txn` (`transaction_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng lưu thông tin giao dịch thanh toán';

-- Chèn dữ liệu mẫu cho Payment Service
INSERT INTO `payments` (`id`, `booking_id`, `amount`, `payment_method`, `status`, `transaction_code`, `payment_time`, `note`) VALUES
(1, 3, 280000.0, 'VNPAY', 'SUCCESS', 'TXN_1728300000001', '2026-10-12 17:00:00', 'Thanh toán tiền sân Le Van Cuong qua VNPAY'),
(2, 6, 450000.0, 'BANKING', 'SUCCESS', 'TXN_1728300000002', '2026-10-15 20:00:00', 'Chuyển khoản thanh toán toàn phần Do Van Hung'),
(3, 1, 100000.0, 'MOMO', 'SUCCESS', 'TXN_1728300000003', '2026-10-10 16:30:00', 'Thanh toán cọc sân Nguyen Van An qua Momo');

-- =====================================================================================
-- KẾT THÚC SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU
-- =====================================================================================
