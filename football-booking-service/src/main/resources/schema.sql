-- =====================================================================================
-- DATABASE SCHEMA: Booking Service (football_booking_db)
-- Buổi thực hành 4: Quản lý dữ liệu và kết nối dịch vụ
-- =====================================================================================
CREATE TABLE IF NOT EXISTS `bookings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `customer_name` VARCHAR(100) NOT NULL COMMENT 'Họ và tên khách hàng',
    `phone` VARCHAR(15) NOT NULL COMMENT 'Số điện thoại 10 số',
    `field_name` VARCHAR(100) NOT NULL COMMENT 'Tên sân bóng',
    `booking_date` DATE NOT NULL COMMENT 'Ngày đặt sân (yyyy-MM-dd)',
    `start_time` TIME NOT NULL COMMENT 'Giờ bắt đầu',
    `end_time` TIME NOT NULL COMMENT 'Giờ kết thúc',
    `price` DOUBLE NOT NULL COMMENT 'Giá thuê sân (VNĐ)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, CONFIRMED, PAID, CANCELLED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_booking_customer` (`customer_name`),
    INDEX `idx_booking_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
