-- ============================================================================
-- Restaurant Management System Database Schema & Seed Data
-- Database Engine: MySQL
-- Charset: utf8mb4 / utf8mb4_unicode_ci
-- ============================================================================

DROP DATABASE IF EXISTS `restaurant_db`;
CREATE DATABASE `restaurant_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `restaurant_db`;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS `bills`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `restaurant_tables`;
DROP TABLE IF EXISTS `menu_items`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `employees`;
DROP TABLE IF EXISTS `inventory`;

-- 1. Employees Table
CREATE TABLE `employees` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `role` VARCHAR(20) NOT NULL, -- 'ADMIN', 'KITCHEN', 'WAITER'
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(100) NOT NULL,
  `salary` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `contact` VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Categories Table
CREATE TABLE `categories` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Menu Items Table
CREATE TABLE `menu_items` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(150) NOT NULL,
  `category_id` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `description` VARCHAR(255) DEFAULT '',
  CONSTRAINT `fk_menu_category` FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Restaurant Tables Table
CREATE TABLE `restaurant_tables` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `table_number` VARCHAR(50) NOT NULL UNIQUE,
  `capacity` INT NOT NULL DEFAULT 4,
  `status` VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE' -- 'AVAILABLE', 'OCCUPIED', 'RESERVED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Customers Table
CREATE TABLE `customers` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `contact` VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Orders Table
CREATE TABLE `orders` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `table_id` INT NOT NULL,
  `customer_id` INT NOT NULL,
  `status` VARCHAR(30) NOT NULL DEFAULT 'Placed', -- 'Placed', 'Preparing', 'Served', 'Completed'
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_order_table` FOREIGN KEY (`table_id`) REFERENCES `restaurant_tables`(`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Order Items Table
CREATE TABLE `order_items` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `menu_item_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `subtotal` DECIMAL(10,2) NOT NULL,
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_menu` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_items`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bills Table
CREATE TABLE `bills` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL UNIQUE,
  `subtotal` DECIMAL(10,2) NOT NULL,
  `discount_percent` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `tax_percent` DECIMAL(5,2) NOT NULL DEFAULT 5.00,
  `grand_total` DECIMAL(10,2) NOT NULL,
  `payment_method` VARCHAR(30) NOT NULL, -- 'CASH', 'CARD', 'UPI'
  `payment_reference` TEXT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_bill_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Inventory Table
CREATE TABLE `inventory` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `item_name` VARCHAR(150) NOT NULL,
  `unit` VARCHAR(50) NOT NULL,
  `quantity` DECIMAL(10,2) NOT NULL,
  `low_stock_threshold` DECIMAL(10,2) NOT NULL,
  `cost_per_unit` DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SEED DATA
-- ============================================================================

-- Seed Employees (Sample logins for the three roles)
INSERT INTO `employees` (`name`, `role`, `username`, `password`, `salary`, `contact`) VALUES
('Administrator', 'ADMIN', 'admin', 'admin123', 55000.00, '9876543210'),
('Chef Rajesh', 'KITCHEN', 'chef1', 'chef123', 38000.00, '9876543211'),
('Waiter Suresh', 'WAITER', 'waiter1', 'waiter123', 24000.00, '9876543212');

-- Seed Categories (5 required categories)
INSERT INTO `categories` (`name`) VALUES
('Starters'),
('Main Course'),
('Breads'),
('Desserts'),
('Beverages');

-- Seed Menu Items (Spread across categories, includes Paneer item)
INSERT INTO `menu_items` (`name`, `category_id`, `price`, `description`) VALUES
('Paneer Tikka', 1, 240.00, 'Charcoal grilled cottage cheese cubes with spices'),
('Crispy Corn', 1, 180.00, 'Golden fried spiced sweet corn niblets'),
('Paneer Butter Masala', 2, 290.00, 'Rich tomato and butter gravy with soft paneer'),
('Dal Makhani', 2, 220.00, 'Slow cooked black lentils with cream and butter'),
('Butter Naan', 3, 45.00, 'Traditional tandoori flatbread with butter glaze'),
('Garlic Roti', 3, 35.00, 'Whole wheat tandoori roti infused with garlic'),
('Gulab Jamun', 4, 90.00, 'Warm milk dumplings soaked in cardamom sugar syrup'),
('Kesar Kulfi', 4, 110.00, 'Traditional saffron and pistachio frozen dessert'),
('Masala Chai', 5, 40.00, 'Aromatic spiced Indian milk tea'),
('Fresh Lime Soda', 5, 75.00, 'Refreshing chilled lime soda (sweet/salted)');

-- Seed Tables (6 tables)
INSERT INTO `restaurant_tables` (`table_number`, `capacity`, `status`) VALUES
('Table 1', 2, 'AVAILABLE'),
('Table 2', 4, 'AVAILABLE'),
('Table 3', 4, 'AVAILABLE'),
('Table 4', 6, 'AVAILABLE'),
('Table 5', 8, 'AVAILABLE'),
('Table 6', 4, 'AVAILABLE');

-- Seed Customers
INSERT INTO `customers` (`name`, `contact`) VALUES
('Amit Sharma', '9811122233'),
('Priya Patel', '9822233344');

-- Seed Inventory Items
-- Notice: 'Fresh Paneer' quantity (2.50) is below threshold (5.00), triggering immediate low-stock alert
INSERT INTO `inventory` (`item_name`, `unit`, `quantity`, `low_stock_threshold`, `cost_per_unit`) VALUES
('Fresh Paneer', 'kg', 2.50, 5.00, 320.00),
('Basmati Rice', 'kg', 30.00, 10.00, 85.00),
('Cooking Oil', 'litres', 25.00, 12.00, 140.00),
('Dairy Milk', 'litres', 15.00, 8.00, 60.00);

-- Seed Sample Active Orders for Kitchen Display System demonstration
INSERT INTO `orders` (`id`, `table_id`, `customer_id`, `status`, `total_amount`) VALUES
(1, 1, 1, 'Placed', 595.00),
(2, 2, 2, 'Preparing', 380.00);

-- Mark Tables 1 and 2 Occupied
UPDATE `restaurant_tables` SET `status` = 'OCCUPIED' WHERE `id` IN (1, 2);

-- Seed Order Items for Order #1
INSERT INTO `order_items` (`order_id`, `menu_item_id`, `quantity`, `price`, `subtotal`) VALUES
(1, 1, 1, 240.00, 240.00), -- Paneer Tikka
(1, 4, 1, 220.00, 220.00), -- Dal Makhani
(1, 5, 3, 45.00, 135.00);   -- Butter Naan x3

-- Seed Order Items for Order #2
INSERT INTO `order_items` (`order_id`, `menu_item_id`, `quantity`, `price`, `subtotal`) VALUES
(2, 3, 1, 290.00, 290.00), -- Paneer Butter Masala
(2, 7, 1, 90.00, 90.00);    -- Gulab Jamun

