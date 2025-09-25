-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 25/09/2025 às 19:24
-- Versão do servidor: 10.4.32-MariaDB
-- Versão do PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `aurora_commerce`
--

-- --------------------------------------------------------

--
-- Estrutura para tabela `address`
--

CREATE TABLE `address` (
  `id_address` bigint(20) NOT NULL,
  `id_customer` bigint(20) NOT NULL,
  `label` varchar(40) DEFAULT NULL,
  `recipient_name` varchar(120) DEFAULT NULL,
  `phone` varchar(25) DEFAULT NULL,
  `street` varchar(160) NOT NULL,
  `number` varchar(20) DEFAULT NULL,
  `complement` varchar(80) DEFAULT NULL,
  `district` varchar(80) DEFAULT NULL,
  `postal_code` varchar(12) NOT NULL,
  `id_city` int(11) NOT NULL,
  `address_type` enum('billing','shipping','both') DEFAULT 'both',
  `is_default_billing` tinyint(1) DEFAULT 0,
  `is_default_shipping` tinyint(1) DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `brand`
--

CREATE TABLE `brand` (
  `id_brand` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `website` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `cart`
--

CREATE TABLE `cart` (
  `id_cart` bigint(20) NOT NULL,
  `id_customer` bigint(20) DEFAULT NULL,
  `session_id` varchar(64) DEFAULT NULL,
  `currency` char(3) NOT NULL DEFAULT 'BRL',
  `region` varchar(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `cart_item`
--

CREATE TABLE `cart_item` (
  `id_cart_item` bigint(20) NOT NULL,
  `id_cart` bigint(20) NOT NULL,
  `id_sku` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `unit_price_cents` int(11) NOT NULL,
  `added_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `category`
--

CREATE TABLE `category` (
  `id_category` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `slug` varchar(140) NOT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `city`
--

CREATE TABLE `city` (
  `id_city` int(11) NOT NULL,
  `id_state` int(11) NOT NULL,
  `name` varchar(120) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `country`
--

CREATE TABLE `country` (
  `id_country` int(11) NOT NULL,
  `name` varchar(80) NOT NULL,
  `iso2` char(2) NOT NULL,
  `iso3` char(3) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `country`
--

INSERT INTO `country` (`id_country`, `name`, `iso2`, `iso3`) VALUES
(1, 'Brasil', 'BR', 'BRA'),
(2, 'Estados Unidos', 'US', 'USA'),
(3, 'Argentina', 'AR', 'ARG');

-- --------------------------------------------------------

--
-- Estrutura para tabela `coupon`
--

CREATE TABLE `coupon` (
  `id_coupon` int(11) NOT NULL,
  `code` varchar(40) NOT NULL,
  `id_promotion` int(11) NOT NULL,
  `max_redemptions` int(11) DEFAULT NULL,
  `redemptions` int(11) DEFAULT 0,
  `starts_at` datetime NOT NULL,
  `ends_at` datetime NOT NULL,
  `active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `coupon_redemption`
--

CREATE TABLE `coupon_redemption` (
  `id_redemption` bigint(20) NOT NULL,
  `id_coupon` int(11) NOT NULL,
  `id_customer` bigint(20) NOT NULL,
  `id_order` bigint(20) DEFAULT NULL,
  `redeemed_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `customer`
--

CREATE TABLE `customer` (
  `id_customer` bigint(20) NOT NULL,
  `name` varchar(120) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(25) DEFAULT NULL,
  `cpf` varchar(14) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL,
  `status` enum('active','blocked','deleted') NOT NULL DEFAULT 'active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `event_log`
--

CREATE TABLE `event_log` (
  `id_event` bigint(20) NOT NULL,
  `scope` enum('order','payment','stock','customer','coupon','shipment','system') NOT NULL,
  `ref_id` bigint(20) DEFAULT NULL,
  `level` enum('info','warning','error') NOT NULL DEFAULT 'info',
  `message` varchar(255) NOT NULL,
  `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`payload`)),
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `orders`
--

CREATE TABLE `orders` (
  `id_order` bigint(20) NOT NULL,
  `id_customer` bigint(20) NOT NULL,
  `status` enum('pending','processing','paid','shipped','delivered','canceled','refunded') NOT NULL DEFAULT 'pending',
  `currency` char(3) NOT NULL DEFAULT 'BRL',
  `region` varchar(20) DEFAULT NULL,
  `subtotal_cents` int(11) NOT NULL DEFAULT 0,
  `discount_cents` int(11) NOT NULL DEFAULT 0,
  `shipping_cents` int(11) NOT NULL DEFAULT 0,
  `tax_cents` int(11) NOT NULL DEFAULT 0,
  `total_cents` int(11) NOT NULL DEFAULT 0,
  `coupon_code` varchar(40) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `order_address`
--

CREATE TABLE `order_address` (
  `id_order_address` bigint(20) NOT NULL,
  `id_order` bigint(20) NOT NULL,
  `kind` enum('billing','shipping') NOT NULL,
  `recipient_name` varchar(120) DEFAULT NULL,
  `phone` varchar(25) DEFAULT NULL,
  `street` varchar(160) NOT NULL,
  `number` varchar(20) DEFAULT NULL,
  `complement` varchar(80) DEFAULT NULL,
  `district` varchar(80) DEFAULT NULL,
  `postal_code` varchar(12) NOT NULL,
  `country_name` varchar(80) DEFAULT NULL,
  `state_name` varchar(80) DEFAULT NULL,
  `city_name` varchar(120) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `order_item`
--

CREATE TABLE `order_item` (
  `id_order_item` bigint(20) NOT NULL,
  `id_order` bigint(20) NOT NULL,
  `id_sku` bigint(20) NOT NULL,
  `product_name` varchar(180) NOT NULL,
  `sku_code` varchar(100) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `unit_price_cents` int(11) NOT NULL,
  `discount_cents` int(11) NOT NULL DEFAULT 0,
  `total_cents` int(11) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `payment`
--

CREATE TABLE `payment` (
  `id_payment` bigint(20) NOT NULL,
  `id_order` bigint(20) NOT NULL,
  `provider` varchar(40) NOT NULL,
  `provider_txid` varchar(120) DEFAULT NULL,
  `method` enum('card','pix','boleto','paypal','transfer') NOT NULL,
  `amount_cents` int(11) NOT NULL,
  `status` enum('pending','processing','approved','declined','refunded','chargeback') NOT NULL DEFAULT 'pending',
  `paid_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `payment_method_token`
--

CREATE TABLE `payment_method_token` (
  `id_token` bigint(20) NOT NULL,
  `id_customer` bigint(20) NOT NULL,
  `provider` varchar(40) NOT NULL,
  `provider_ref` varchar(120) NOT NULL,
  `brand` varchar(30) DEFAULT NULL,
  `last4` char(4) DEFAULT NULL,
  `exp_month` tinyint(4) DEFAULT NULL,
  `exp_year` smallint(6) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `price_list`
--

CREATE TABLE `price_list` (
  `id_price_list` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `currency` char(3) NOT NULL DEFAULT 'BRL',
  `region` varchar(20) DEFAULT NULL,
  `active` tinyint(1) DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `price_list`
--

INSERT INTO `price_list` (`id_price_list`, `name`, `currency`, `region`, `active`, `created_at`) VALUES
(1, 'Lista Padrão BR', 'BRL', 'BR', 1, '2025-09-25 14:22:59');

-- --------------------------------------------------------

--
-- Estrutura para tabela `price_list_item`
--

CREATE TABLE `price_list_item` (
  `id_price_list_item` bigint(20) NOT NULL,
  `id_price_list` int(11) NOT NULL,
  `id_sku` bigint(20) NOT NULL,
  `unit_amount_cents` int(11) NOT NULL,
  `valid_from` datetime NOT NULL DEFAULT current_timestamp(),
  `valid_to` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `product`
--

CREATE TABLE `product` (
  `id_product` bigint(20) NOT NULL,
  `id_brand` int(11) DEFAULT NULL,
  `name` varchar(180) NOT NULL,
  `slug` varchar(200) NOT NULL,
  `short_desc` varchar(255) DEFAULT NULL,
  `long_desc` mediumtext DEFAULT NULL,
  `active` tinyint(1) DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `product_category`
--

CREATE TABLE `product_category` (
  `id_product` bigint(20) NOT NULL,
  `id_category` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `product_media`
--

CREATE TABLE `product_media` (
  `id_media` bigint(20) NOT NULL,
  `id_product` bigint(20) NOT NULL,
  `kind` enum('image','video') NOT NULL,
  `url` varchar(255) NOT NULL,
  `position` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `promotion`
--

CREATE TABLE `promotion` (
  `id_promotion` int(11) NOT NULL,
  `name` varchar(160) NOT NULL,
  `description` text DEFAULT NULL,
  `scope` enum('product','category','order') NOT NULL,
  `kind` enum('percent','fixed','free_shipping') NOT NULL,
  `value` decimal(10,2) DEFAULT NULL,
  `currency` char(3) DEFAULT 'BRL',
  `starts_at` datetime NOT NULL,
  `ends_at` datetime NOT NULL,
  `min_order_cents` int(11) DEFAULT NULL,
  `active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `promotion_category`
--

CREATE TABLE `promotion_category` (
  `id_promotion` int(11) NOT NULL,
  `id_category` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `promotion_product`
--

CREATE TABLE `promotion_product` (
  `id_promotion` int(11) NOT NULL,
  `id_product` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `shipment`
--

CREATE TABLE `shipment` (
  `id_shipment` bigint(20) NOT NULL,
  `id_order` bigint(20) NOT NULL,
  `id_shipping_method` int(11) NOT NULL,
  `carrier` varchar(120) DEFAULT NULL,
  `tracking_code` varchar(120) DEFAULT NULL,
  `status` enum('ready','in_transit','delivered','exception','returned','canceled') NOT NULL DEFAULT 'ready',
  `shipped_at` datetime DEFAULT NULL,
  `delivered_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `shipping_method`
--

CREATE TABLE `shipping_method` (
  `id_shipping_method` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `carrier` varchar(120) DEFAULT NULL,
  `active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `shipping_method`
--

INSERT INTO `shipping_method` (`id_shipping_method`, `name`, `carrier`, `active`) VALUES
(1, 'Motoboy', 'Local', 1),
(2, 'PAC', 'Correios', 1),
(3, 'SEDEX', 'Correios', 1);

-- --------------------------------------------------------

--
-- Estrutura para tabela `sku`
--

CREATE TABLE `sku` (
  `id_sku` bigint(20) NOT NULL,
  `id_product` bigint(20) NOT NULL,
  `sku_code` varchar(100) NOT NULL,
  `gtin` varchar(20) DEFAULT NULL,
  `option_summary` varchar(180) DEFAULT NULL,
  `attributes_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`attributes_json`)),
  `price_cents` int(11) NOT NULL,
  `cost_cents` int(11) DEFAULT NULL,
  `currency` char(3) NOT NULL DEFAULT 'BRL',
  `weight_grams` int(11) DEFAULT NULL,
  `width_mm` int(11) DEFAULT NULL,
  `height_mm` int(11) DEFAULT NULL,
  `depth_mm` int(11) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `state`
--

CREATE TABLE `state` (
  `id_state` int(11) NOT NULL,
  `id_country` int(11) NOT NULL,
  `name` varchar(80) NOT NULL,
  `uf` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `stock`
--

CREATE TABLE `stock` (
  `id_stock` bigint(20) NOT NULL,
  `id_sku` bigint(20) NOT NULL,
  `id_warehouse` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `stock_movement`
--

CREATE TABLE `stock_movement` (
  `id_movement` bigint(20) NOT NULL,
  `id_sku` bigint(20) NOT NULL,
  `id_warehouse` int(11) NOT NULL,
  `kind` enum('in','out','adjust') NOT NULL,
  `quantity` int(11) NOT NULL,
  `reason` varchar(160) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Acionadores `stock_movement`
--
DELIMITER $$
CREATE TRIGGER `trg_stock_movement_after_insert` AFTER INSERT ON `stock_movement` FOR EACH ROW BEGIN
  INSERT INTO event_log(scope, ref_id, level, message, payload)
  VALUES('stock', NEW.id_movement, 'info', CONCAT('Mov. estoque ', NEW.kind, ' SKU ', NEW.id_sku, ' qty ', NEW.quantity), NULL);

  -- Atualiza tabela stock conforme movimento
  IF NEW.kind = 'in' THEN
    INSERT INTO stock(id_sku, id_warehouse, quantity, updated_at)
      VALUES (NEW.id_sku, NEW.id_warehouse, NEW.quantity, NOW())
    ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity), updated_at = NOW();
  ELSEIF NEW.kind = 'out' THEN
    INSERT INTO stock(id_sku, id_warehouse, quantity, updated_at)
      VALUES (NEW.id_sku, NEW.id_warehouse, -NEW.quantity, NOW())
    ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity), updated_at = NOW();
  ELSEIF NEW.kind = 'adjust' THEN
    INSERT INTO stock(id_sku, id_warehouse, quantity, updated_at)
      VALUES (NEW.id_sku, NEW.id_warehouse, NEW.quantity, NOW())
    ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), updated_at = NOW();
  END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_order_totals`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_order_totals` (
`id_order` bigint(20)
,`created_at` datetime
,`status` enum('pending','processing','paid','shipped','delivered','canceled','refunded')
,`currency` char(3)
,`subtotal_cents` int(11)
,`discount_cents` int(11)
,`shipping_cents` int(11)
,`tax_cents` int(11)
,`total_cents` int(11)
,`id_customer` bigint(20)
,`customer_name` varchar(120)
,`email` varchar(150)
);

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_product_active_skus`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_product_active_skus` (
`id_product` bigint(20)
,`product_name` varchar(180)
,`slug` varchar(200)
,`id_sku` bigint(20)
,`sku_code` varchar(100)
,`option_summary` varchar(180)
,`price_cents` int(11)
,`currency` char(3)
,`sku_active` tinyint(1)
);

-- --------------------------------------------------------

--
-- Estrutura para tabela `warehouse`
--

CREATE TABLE `warehouse` (
  `id_warehouse` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `code` varchar(40) NOT NULL,
  `address_text` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `warehouse`
--

INSERT INTO `warehouse` (`id_warehouse`, `name`, `code`, `address_text`) VALUES
(1, 'Centro SP', 'WH-SP-01', NULL);

-- --------------------------------------------------------

--
-- Estrutura para view `vw_order_totals`
--
DROP TABLE IF EXISTS `vw_order_totals`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_order_totals`  AS SELECT `o`.`id_order` AS `id_order`, `o`.`created_at` AS `created_at`, `o`.`status` AS `status`, `o`.`currency` AS `currency`, `o`.`subtotal_cents` AS `subtotal_cents`, `o`.`discount_cents` AS `discount_cents`, `o`.`shipping_cents` AS `shipping_cents`, `o`.`tax_cents` AS `tax_cents`, `o`.`total_cents` AS `total_cents`, `c`.`id_customer` AS `id_customer`, `c`.`name` AS `customer_name`, `c`.`email` AS `email` FROM (`orders` `o` join `customer` `c` on(`c`.`id_customer` = `o`.`id_customer`)) ;

-- --------------------------------------------------------

--
-- Estrutura para view `vw_product_active_skus`
--
DROP TABLE IF EXISTS `vw_product_active_skus`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_product_active_skus`  AS SELECT `p`.`id_product` AS `id_product`, `p`.`name` AS `product_name`, `p`.`slug` AS `slug`, `s`.`id_sku` AS `id_sku`, `s`.`sku_code` AS `sku_code`, `s`.`option_summary` AS `option_summary`, `s`.`price_cents` AS `price_cents`, `s`.`currency` AS `currency`, `s`.`active` AS `sku_active` FROM (`product` `p` join `sku` `s` on(`s`.`id_product` = `p`.`id_product`)) WHERE `p`.`active` = 1 ;

--
-- Índices para tabelas despejadas
--

--
-- Índices de tabela `address`
--
ALTER TABLE `address`
  ADD PRIMARY KEY (`id_address`),
  ADD KEY `id_city` (`id_city`),
  ADD KEY `idx_address_customer` (`id_customer`);

--
-- Índices de tabela `brand`
--
ALTER TABLE `brand`
  ADD PRIMARY KEY (`id_brand`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Índices de tabela `cart`
--
ALTER TABLE `cart`
  ADD PRIMARY KEY (`id_cart`),
  ADD KEY `idx_cart_customer` (`id_customer`);

--
-- Índices de tabela `cart_item`
--
ALTER TABLE `cart_item`
  ADD PRIMARY KEY (`id_cart_item`),
  ADD KEY `id_cart` (`id_cart`),
  ADD KEY `id_sku` (`id_sku`);

--
-- Índices de tabela `category`
--
ALTER TABLE `category`
  ADD PRIMARY KEY (`id_category`),
  ADD UNIQUE KEY `slug` (`slug`),
  ADD KEY `idx_category_parent` (`parent_id`);

--
-- Índices de tabela `city`
--
ALTER TABLE `city`
  ADD PRIMARY KEY (`id_city`),
  ADD KEY `id_state` (`id_state`);

--
-- Índices de tabela `country`
--
ALTER TABLE `country`
  ADD PRIMARY KEY (`id_country`);

--
-- Índices de tabela `coupon`
--
ALTER TABLE `coupon`
  ADD PRIMARY KEY (`id_coupon`),
  ADD UNIQUE KEY `code` (`code`),
  ADD KEY `id_promotion` (`id_promotion`),
  ADD KEY `idx_coupon_code` (`code`);

--
-- Índices de tabela `coupon_redemption`
--
ALTER TABLE `coupon_redemption`
  ADD PRIMARY KEY (`id_redemption`),
  ADD KEY `id_coupon` (`id_coupon`),
  ADD KEY `id_customer` (`id_customer`);

--
-- Índices de tabela `customer`
--
ALTER TABLE `customer`
  ADD PRIMARY KEY (`id_customer`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `cpf` (`cpf`),
  ADD KEY `idx_customer_email` (`email`);

--
-- Índices de tabela `event_log`
--
ALTER TABLE `event_log`
  ADD PRIMARY KEY (`id_event`);

--
-- Índices de tabela `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id_order`),
  ADD KEY `idx_orders_customer_date` (`id_customer`,`created_at`);

--
-- Índices de tabela `order_address`
--
ALTER TABLE `order_address`
  ADD PRIMARY KEY (`id_order_address`),
  ADD KEY `id_order` (`id_order`);

--
-- Índices de tabela `order_item`
--
ALTER TABLE `order_item`
  ADD PRIMARY KEY (`id_order_item`),
  ADD KEY `id_sku` (`id_sku`),
  ADD KEY `idx_order_item_order` (`id_order`);

--
-- Índices de tabela `payment`
--
ALTER TABLE `payment`
  ADD PRIMARY KEY (`id_payment`),
  ADD KEY `idx_payment_order_status` (`id_order`,`status`);

--
-- Índices de tabela `payment_method_token`
--
ALTER TABLE `payment_method_token`
  ADD PRIMARY KEY (`id_token`),
  ADD KEY `id_customer` (`id_customer`);

--
-- Índices de tabela `price_list`
--
ALTER TABLE `price_list`
  ADD PRIMARY KEY (`id_price_list`),
  ADD KEY `idx_price_list_active` (`active`);

--
-- Índices de tabela `price_list_item`
--
ALTER TABLE `price_list_item`
  ADD PRIMARY KEY (`id_price_list_item`),
  ADD UNIQUE KEY `uq_pl_sku` (`id_price_list`,`id_sku`,`valid_from`),
  ADD KEY `id_sku` (`id_sku`),
  ADD KEY `idx_price_item_period` (`id_price_list`,`id_sku`,`valid_from`,`valid_to`);

--
-- Índices de tabela `product`
--
ALTER TABLE `product`
  ADD PRIMARY KEY (`id_product`),
  ADD UNIQUE KEY `slug` (`slug`),
  ADD KEY `id_brand` (`id_brand`),
  ADD KEY `idx_product_slug` (`slug`);

--
-- Índices de tabela `product_category`
--
ALTER TABLE `product_category`
  ADD PRIMARY KEY (`id_product`,`id_category`),
  ADD KEY `id_category` (`id_category`);

--
-- Índices de tabela `product_media`
--
ALTER TABLE `product_media`
  ADD PRIMARY KEY (`id_media`),
  ADD KEY `id_product` (`id_product`);

--
-- Índices de tabela `promotion`
--
ALTER TABLE `promotion`
  ADD PRIMARY KEY (`id_promotion`),
  ADD KEY `idx_promotion_active` (`active`,`starts_at`,`ends_at`);

--
-- Índices de tabela `promotion_category`
--
ALTER TABLE `promotion_category`
  ADD PRIMARY KEY (`id_promotion`,`id_category`),
  ADD KEY `id_category` (`id_category`);

--
-- Índices de tabela `promotion_product`
--
ALTER TABLE `promotion_product`
  ADD PRIMARY KEY (`id_promotion`,`id_product`),
  ADD KEY `id_product` (`id_product`);

--
-- Índices de tabela `shipment`
--
ALTER TABLE `shipment`
  ADD PRIMARY KEY (`id_shipment`),
  ADD KEY `id_shipping_method` (`id_shipping_method`),
  ADD KEY `idx_shipment_order_status` (`id_order`,`status`);

--
-- Índices de tabela `shipping_method`
--
ALTER TABLE `shipping_method`
  ADD PRIMARY KEY (`id_shipping_method`);

--
-- Índices de tabela `sku`
--
ALTER TABLE `sku`
  ADD PRIMARY KEY (`id_sku`),
  ADD UNIQUE KEY `sku_code` (`sku_code`),
  ADD KEY `idx_sku_product` (`id_product`);

--
-- Índices de tabela `state`
--
ALTER TABLE `state`
  ADD PRIMARY KEY (`id_state`),
  ADD KEY `id_country` (`id_country`);

--
-- Índices de tabela `stock`
--
ALTER TABLE `stock`
  ADD PRIMARY KEY (`id_stock`),
  ADD UNIQUE KEY `uq_sku_wh` (`id_sku`,`id_warehouse`),
  ADD KEY `id_warehouse` (`id_warehouse`),
  ADD KEY `idx_stock_sku_wh` (`id_sku`,`id_warehouse`);

--
-- Índices de tabela `stock_movement`
--
ALTER TABLE `stock_movement`
  ADD PRIMARY KEY (`id_movement`),
  ADD KEY `id_sku` (`id_sku`),
  ADD KEY `id_warehouse` (`id_warehouse`);

--
-- Índices de tabela `warehouse`
--
ALTER TABLE `warehouse`
  ADD PRIMARY KEY (`id_warehouse`),
  ADD UNIQUE KEY `code` (`code`);

--
-- AUTO_INCREMENT para tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `address`
--
ALTER TABLE `address`
  MODIFY `id_address` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `brand`
--
ALTER TABLE `brand`
  MODIFY `id_brand` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `cart`
--
ALTER TABLE `cart`
  MODIFY `id_cart` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `cart_item`
--
ALTER TABLE `cart_item`
  MODIFY `id_cart_item` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `category`
--
ALTER TABLE `category`
  MODIFY `id_category` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `city`
--
ALTER TABLE `city`
  MODIFY `id_city` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `country`
--
ALTER TABLE `country`
  MODIFY `id_country` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `coupon`
--
ALTER TABLE `coupon`
  MODIFY `id_coupon` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `coupon_redemption`
--
ALTER TABLE `coupon_redemption`
  MODIFY `id_redemption` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `customer`
--
ALTER TABLE `customer`
  MODIFY `id_customer` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `event_log`
--
ALTER TABLE `event_log`
  MODIFY `id_event` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `orders`
--
ALTER TABLE `orders`
  MODIFY `id_order` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `order_address`
--
ALTER TABLE `order_address`
  MODIFY `id_order_address` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `order_item`
--
ALTER TABLE `order_item`
  MODIFY `id_order_item` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `payment`
--
ALTER TABLE `payment`
  MODIFY `id_payment` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `payment_method_token`
--
ALTER TABLE `payment_method_token`
  MODIFY `id_token` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `price_list`
--
ALTER TABLE `price_list`
  MODIFY `id_price_list` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de tabela `price_list_item`
--
ALTER TABLE `price_list_item`
  MODIFY `id_price_list_item` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `product`
--
ALTER TABLE `product`
  MODIFY `id_product` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `product_media`
--
ALTER TABLE `product_media`
  MODIFY `id_media` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `promotion`
--
ALTER TABLE `promotion`
  MODIFY `id_promotion` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `shipment`
--
ALTER TABLE `shipment`
  MODIFY `id_shipment` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `shipping_method`
--
ALTER TABLE `shipping_method`
  MODIFY `id_shipping_method` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `sku`
--
ALTER TABLE `sku`
  MODIFY `id_sku` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `state`
--
ALTER TABLE `state`
  MODIFY `id_state` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `stock`
--
ALTER TABLE `stock`
  MODIFY `id_stock` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `stock_movement`
--
ALTER TABLE `stock_movement`
  MODIFY `id_movement` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `warehouse`
--
ALTER TABLE `warehouse`
  MODIFY `id_warehouse` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Restrições para tabelas despejadas
--

--
-- Restrições para tabelas `address`
--
ALTER TABLE `address`
  ADD CONSTRAINT `address_ibfk_1` FOREIGN KEY (`id_customer`) REFERENCES `customer` (`id_customer`),
  ADD CONSTRAINT `address_ibfk_2` FOREIGN KEY (`id_city`) REFERENCES `city` (`id_city`);

--
-- Restrições para tabelas `cart`
--
ALTER TABLE `cart`
  ADD CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`id_customer`) REFERENCES `customer` (`id_customer`);

--
-- Restrições para tabelas `cart_item`
--
ALTER TABLE `cart_item`
  ADD CONSTRAINT `cart_item_ibfk_1` FOREIGN KEY (`id_cart`) REFERENCES `cart` (`id_cart`),
  ADD CONSTRAINT `cart_item_ibfk_2` FOREIGN KEY (`id_sku`) REFERENCES `sku` (`id_sku`);

--
-- Restrições para tabelas `category`
--
ALTER TABLE `category`
  ADD CONSTRAINT `category_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id_category`);

--
-- Restrições para tabelas `city`
--
ALTER TABLE `city`
  ADD CONSTRAINT `city_ibfk_1` FOREIGN KEY (`id_state`) REFERENCES `state` (`id_state`);

--
-- Restrições para tabelas `coupon`
--
ALTER TABLE `coupon`
  ADD CONSTRAINT `coupon_ibfk_1` FOREIGN KEY (`id_promotion`) REFERENCES `promotion` (`id_promotion`);

--
-- Restrições para tabelas `coupon_redemption`
--
ALTER TABLE `coupon_redemption`
  ADD CONSTRAINT `coupon_redemption_ibfk_1` FOREIGN KEY (`id_coupon`) REFERENCES `coupon` (`id_coupon`),
  ADD CONSTRAINT `coupon_redemption_ibfk_2` FOREIGN KEY (`id_customer`) REFERENCES `customer` (`id_customer`);

--
-- Restrições para tabelas `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`id_customer`) REFERENCES `customer` (`id_customer`);

--
-- Restrições para tabelas `order_address`
--
ALTER TABLE `order_address`
  ADD CONSTRAINT `order_address_ibfk_1` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id_order`);

--
-- Restrições para tabelas `order_item`
--
ALTER TABLE `order_item`
  ADD CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id_order`),
  ADD CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`id_sku`) REFERENCES `sku` (`id_sku`);

--
-- Restrições para tabelas `payment`
--
ALTER TABLE `payment`
  ADD CONSTRAINT `payment_ibfk_1` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id_order`);

--
-- Restrições para tabelas `payment_method_token`
--
ALTER TABLE `payment_method_token`
  ADD CONSTRAINT `payment_method_token_ibfk_1` FOREIGN KEY (`id_customer`) REFERENCES `customer` (`id_customer`);

--
-- Restrições para tabelas `price_list_item`
--
ALTER TABLE `price_list_item`
  ADD CONSTRAINT `price_list_item_ibfk_1` FOREIGN KEY (`id_price_list`) REFERENCES `price_list` (`id_price_list`),
  ADD CONSTRAINT `price_list_item_ibfk_2` FOREIGN KEY (`id_sku`) REFERENCES `sku` (`id_sku`);

--
-- Restrições para tabelas `product`
--
ALTER TABLE `product`
  ADD CONSTRAINT `product_ibfk_1` FOREIGN KEY (`id_brand`) REFERENCES `brand` (`id_brand`);

--
-- Restrições para tabelas `product_category`
--
ALTER TABLE `product_category`
  ADD CONSTRAINT `product_category_ibfk_1` FOREIGN KEY (`id_product`) REFERENCES `product` (`id_product`),
  ADD CONSTRAINT `product_category_ibfk_2` FOREIGN KEY (`id_category`) REFERENCES `category` (`id_category`);

--
-- Restrições para tabelas `product_media`
--
ALTER TABLE `product_media`
  ADD CONSTRAINT `product_media_ibfk_1` FOREIGN KEY (`id_product`) REFERENCES `product` (`id_product`);

--
-- Restrições para tabelas `promotion_category`
--
ALTER TABLE `promotion_category`
  ADD CONSTRAINT `promotion_category_ibfk_1` FOREIGN KEY (`id_promotion`) REFERENCES `promotion` (`id_promotion`),
  ADD CONSTRAINT `promotion_category_ibfk_2` FOREIGN KEY (`id_category`) REFERENCES `category` (`id_category`);

--
-- Restrições para tabelas `promotion_product`
--
ALTER TABLE `promotion_product`
  ADD CONSTRAINT `promotion_product_ibfk_1` FOREIGN KEY (`id_promotion`) REFERENCES `promotion` (`id_promotion`),
  ADD CONSTRAINT `promotion_product_ibfk_2` FOREIGN KEY (`id_product`) REFERENCES `product` (`id_product`);

--
-- Restrições para tabelas `shipment`
--
ALTER TABLE `shipment`
  ADD CONSTRAINT `shipment_ibfk_1` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id_order`),
  ADD CONSTRAINT `shipment_ibfk_2` FOREIGN KEY (`id_shipping_method`) REFERENCES `shipping_method` (`id_shipping_method`);

--
-- Restrições para tabelas `sku`
--
ALTER TABLE `sku`
  ADD CONSTRAINT `sku_ibfk_1` FOREIGN KEY (`id_product`) REFERENCES `product` (`id_product`);

--
-- Restrições para tabelas `state`
--
ALTER TABLE `state`
  ADD CONSTRAINT `state_ibfk_1` FOREIGN KEY (`id_country`) REFERENCES `country` (`id_country`);

--
-- Restrições para tabelas `stock`
--
ALTER TABLE `stock`
  ADD CONSTRAINT `stock_ibfk_1` FOREIGN KEY (`id_sku`) REFERENCES `sku` (`id_sku`),
  ADD CONSTRAINT `stock_ibfk_2` FOREIGN KEY (`id_warehouse`) REFERENCES `warehouse` (`id_warehouse`);

--
-- Restrições para tabelas `stock_movement`
--
ALTER TABLE `stock_movement`
  ADD CONSTRAINT `stock_movement_ibfk_1` FOREIGN KEY (`id_sku`) REFERENCES `sku` (`id_sku`),
  ADD CONSTRAINT `stock_movement_ibfk_2` FOREIGN KEY (`id_warehouse`) REFERENCES `warehouse` (`id_warehouse`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
