-- =====================================================================================
-- DATABASE SCHEMA: Payment Service (football_payment_db)
-- Buổi thực hành 4: Quản lý dữ liệu và kết nối dịch vụ
-- =====================================================================================
CREATE TABLE IF NOT EXISTS `payments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `booking_id` BIGINT NOT NULL COMMENT 'Logical ID tham chiếu sang Booking Service qua REST API',
    `amount` DOUBLE NOT NULL COMMENT 'Số tiền thanh toán (VNĐ)',
    `payment_method` VARCHAR(20) NOT NULL COMMENT 'VNPAY, MOMO, CASH, BANKING',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'SUCCESS, PENDING, FAILED',
    `transaction_code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã giao dịch',
    `payment_time` DATETIME NOT NULL COMMENT 'Thời gian thanh toán',
    `note` VARCHAR(255) DEFAULT NULL COMMENT 'Ghi chú',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_payment_booking_id` (`booking_id`),
    INDEX `idx_payment_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
