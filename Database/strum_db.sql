-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 26/09/2025 às 18:37
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
-- Banco de dados: `strum_db`
--

-- --------------------------------------------------------

--
-- Estrutura para tabela `app_store`
--

CREATE TABLE `app_store` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(120) NOT NULL,
  `owner_id` int(10) UNSIGNED NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `app_store`
--

INSERT INTO `app_store` (`id`, `name`, `owner_id`, `website`, `created_at`) VALUES
(1, 'Strum', 1, 'https://strum.example.com', '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `company`
--

CREATE TABLE `company` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(120) NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `company`
--

INSERT INTO `company` (`id`, `name`, `website`, `created_at`) VALUES
(1, 'Aurora Dev', 'https://auroradev.example.com', '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `developers`
--

CREATE TABLE `developers` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(160) NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `developers`
--

INSERT INTO `developers` (`id`, `name`, `website`, `created_at`) VALUES
(1, 'Supergiant Games', 'https://www.supergiantgames.com', '2025-09-26 16:36:40'),
(2, 'CD Projekt Red', 'https://www.cdprojekt.com', '2025-09-26 16:36:40'),
(3, 'Aurora Dev Studios', 'https://auroradev.example.com/studios', '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `dlcs`
--

CREATE TABLE `dlcs` (
  `id` int(10) UNSIGNED NOT NULL,
  `game_id` int(10) UNSIGNED NOT NULL,
  `title` varchar(160) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL DEFAULT 0.00,
  `release_date` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `dlcs`
--

INSERT INTO `dlcs` (`id`, `game_id`, `title`, `description`, `price`, `release_date`, `created_at`) VALUES
(1, 2, 'Hearts of Stone', 'Expansão com novas quests e áreas.', 49.90, '2015-10-13', '2025-09-26 16:36:40'),
(2, 2, 'Blood and Wine', 'Grande expansão em Toussaint.', 79.90, '2016-05-31', '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `games`
--

CREATE TABLE `games` (
  `id` int(10) UNSIGNED NOT NULL,
  `title` varchar(160) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL DEFAULT 0.00,
  `release_date` date DEFAULT NULL,
  `developer_id` int(10) UNSIGNED DEFAULT NULL,
  `publisher_id` int(10) UNSIGNED DEFAULT NULL,
  `is_free_to_play` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `games`
--

INSERT INTO `games` (`id`, `title`, `description`, `price`, `release_date`, `developer_id`, `publisher_id`, `is_free_to_play`, `created_at`) VALUES
(1, 'Hades II', 'Sequência do aclamado roguelike.', 129.90, '2024-12-14', 1, 1, 0, '2025-09-26 16:36:40'),
(2, 'The Witcher 3: Wild Hunt', 'RPG em mundo aberto.', 59.90, '2015-05-19', 2, 2, 0, '2025-09-26 16:36:40'),
(3, 'Strum: Creator Tools', 'Ferramentas oficiais do Strum para criadores (modding, SDK).', 0.00, '2025-01-10', 3, 3, 1, '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `game_genres`
--

CREATE TABLE `game_genres` (
  `game_id` int(10) UNSIGNED NOT NULL,
  `genre_id` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `game_genres`
--

INSERT INTO `game_genres` (`game_id`, `genre_id`) VALUES
(1, 1),
(1, 2),
(1, 5),
(2, 3),
(2, 4),
(2, 6),
(3, 5),
(3, 6);

-- --------------------------------------------------------

--
-- Estrutura para tabela `game_platforms`
--

CREATE TABLE `game_platforms` (
  `game_id` int(10) UNSIGNED NOT NULL,
  `platform_id` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `game_platforms`
--

INSERT INTO `game_platforms` (`game_id`, `platform_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 1),
(2, 2),
(2, 3),
(3, 1);

-- --------------------------------------------------------

--
-- Estrutura para tabela `game_tags`
--

CREATE TABLE `game_tags` (
  `game_id` int(10) UNSIGNED NOT NULL,
  `tag_id` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `game_tags`
--

INSERT INTO `game_tags` (`game_id`, `tag_id`) VALUES
(1, 1),
(1, 2),
(1, 5),
(2, 1),
(2, 4),
(3, 1);

-- --------------------------------------------------------

--
-- Estrutura para tabela `genres`
--

CREATE TABLE `genres` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(60) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `genres`
--

INSERT INTO `genres` (`id`, `name`) VALUES
(2, 'Ação'),
(6, 'Aventura'),
(5, 'Indie'),
(4, 'Mundo Aberto'),
(1, 'Rogue-like'),
(3, 'RPG');

-- --------------------------------------------------------

--
-- Estrutura para tabela `gift_cards`
--

CREATE TABLE `gift_cards` (
  `code` varchar(24) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `is_redeemed` tinyint(1) NOT NULL DEFAULT 0,
  `redeemed_by` int(10) UNSIGNED DEFAULT NULL,
  `redeemed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `gift_cards`
--

INSERT INTO `gift_cards` (`code`, `amount`, `is_redeemed`, `redeemed_by`, `redeemed_at`, `created_at`) VALUES
('GC-AB12CD34EF56', 50.00, 0, NULL, NULL, '2025-09-26 16:36:40'),
('GC-XY98ZT76UV54', 100.00, 0, NULL, NULL, '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `orders`
--

CREATE TABLE `orders` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  `status` enum('PENDING','PAID','CANCELLED','REFUNDED') NOT NULL DEFAULT 'PAID',
  `payment_method` enum('WALLET','CREDIT_CARD','PIX') NOT NULL DEFAULT 'WALLET',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `orders`
--

INSERT INTO `orders` (`id`, `user_id`, `total_amount`, `status`, `payment_method`, `created_at`) VALUES
(1, 1, 139.80, 'PAID', 'WALLET', '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `order_items`
--

CREATE TABLE `order_items` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `order_id` bigint(20) UNSIGNED NOT NULL,
  `item_type` enum('GAME','DLC','SUBSCRIPTION') NOT NULL,
  `item_id` int(10) UNSIGNED NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `qty` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `order_items`
--

INSERT INTO `order_items` (`id`, `order_id`, `item_type`, `item_id`, `unit_price`, `qty`) VALUES
(1, 1, 'GAME', 2, 59.90, 1),
(2, 1, 'DLC', 2, 79.90, 1);

-- --------------------------------------------------------

--
-- Estrutura para tabela `platforms`
--

CREATE TABLE `platforms` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(60) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `platforms`
--

INSERT INTO `platforms` (`id`, `name`) VALUES
(3, 'Linux'),
(2, 'macOS'),
(1, 'Windows');

-- --------------------------------------------------------

--
-- Estrutura para tabela `publishers`
--

CREATE TABLE `publishers` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(160) NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `publishers`
--

INSERT INTO `publishers` (`id`, `name`, `website`, `created_at`) VALUES
(1, 'Supergiant Games', 'https://www.supergiantgames.com', '2025-09-26 16:36:40'),
(2, 'CD Projekt', 'https://www.cdprojekt.com', '2025-09-26 16:36:40'),
(3, 'Aurora Dev Publishing', 'https://auroradev.example.com/publishing', '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `reviews`
--

CREATE TABLE `reviews` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL,
  `game_id` int(10) UNSIGNED NOT NULL,
  `rating` tinyint(4) NOT NULL CHECK (`rating` between 1 and 5),
  `comment` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `reviews`
--

INSERT INTO `reviews` (`id`, `user_id`, `game_id`, `rating`, `comment`, `created_at`) VALUES
(1, 1, 2, 5, 'Obra-prima de RPG.', '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `subscriptions`
--

CREATE TABLE `subscriptions` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(120) NOT NULL,
  `duration_months` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `subscriptions`
--

INSERT INTO `subscriptions` (`id`, `name`, `duration_months`, `price`) VALUES
(1, 'Strum Plus - Mensal', 1, 29.90),
(2, 'Strum Plus - Anual', 12, 299.90);

-- --------------------------------------------------------

--
-- Estrutura para tabela `tags`
--

CREATE TABLE `tags` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(60) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `tags`
--

INSERT INTO `tags` (`id`, `name`) VALUES
(2, 'Controller Support'),
(3, 'Dificuldade'),
(4, 'História Rica'),
(5, 'Replayability'),
(1, 'Singleplayer');

-- --------------------------------------------------------

--
-- Estrutura para tabela `users`
--

CREATE TABLE `users` (
  `id` int(10) UNSIGNED NOT NULL,
  `email` varchar(160) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `display_name` varchar(80) NOT NULL,
  `country` varchar(2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `users`
--

INSERT INTO `users` (`id`, `email`, `password_hash`, `display_name`, `country`, `created_at`) VALUES
(1, 'guilherme@example.com', '$2b$10$hashGuilherme', 'Guilherme', 'BR', '2025-09-26 16:36:40'),
(2, 'jorge@example.com', '$2b$10$hashJorge', 'Jorge', 'BR', '2025-09-26 16:36:40'),
(3, 'paulo@example.com', '$2b$10$hashPaulo', 'Paulo', 'BR', '2025-09-26 16:36:40'),
(4, 'thiago@example.com', '$2b$10$hashThiago', 'Thiago', 'BR', '2025-09-26 16:36:40'),
(5, 'gabriel@example.com', '$2b$10$hashGabriel', 'Gabriel', 'BR', '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `user_library`
--

CREATE TABLE `user_library` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL,
  `item_type` enum('GAME','DLC') NOT NULL,
  `item_id` int(10) UNSIGNED NOT NULL,
  `acquired_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `user_library`
--

INSERT INTO `user_library` (`id`, `user_id`, `item_type`, `item_id`, `acquired_at`) VALUES
(1, 1, 'GAME', 2, '2025-09-26 16:36:40'),
(2, 1, 'DLC', 2, '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `user_subscriptions`
--

CREATE TABLE `user_subscriptions` (
  `id` int(10) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL,
  `subscription_id` int(10) UNSIGNED NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `auto_renew` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `user_subscriptions`
--

INSERT INTO `user_subscriptions` (`id`, `user_id`, `subscription_id`, `start_date`, `end_date`, `auto_renew`, `created_at`) VALUES
(1, 3, 1, '2025-09-01', '2025-10-01', 1, '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `user_wallet`
--

CREATE TABLE `user_wallet` (
  `user_id` int(10) UNSIGNED NOT NULL,
  `balance` decimal(12,2) NOT NULL DEFAULT 0.00,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `user_wallet`
--

INSERT INTO `user_wallet` (`user_id`, `balance`, `updated_at`) VALUES
(1, 150.00, '2025-09-26 16:36:40'),
(2, 75.00, '2025-09-26 16:36:40'),
(3, 0.00, '2025-09-26 16:36:40'),
(4, 20.00, '2025-09-26 16:36:40'),
(5, 10.00, '2025-09-26 16:36:40');

-- --------------------------------------------------------

--
-- Estrutura para tabela `wishlists`
--

CREATE TABLE `wishlists` (
  `user_id` int(10) UNSIGNED NOT NULL,
  `game_id` int(10) UNSIGNED NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Despejando dados para a tabela `wishlists`
--

INSERT INTO `wishlists` (`user_id`, `game_id`, `created_at`) VALUES
(2, 1, '2025-09-26 16:36:40');

--
-- Índices para tabelas despejadas
--

--
-- Índices de tabela `app_store`
--
ALTER TABLE `app_store`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`),
  ADD KEY `fk_store_company` (`owner_id`);

--
-- Índices de tabela `company`
--
ALTER TABLE `company`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Índices de tabela `developers`
--
ALTER TABLE `developers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Índices de tabela `dlcs`
--
ALTER TABLE `dlcs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_dlcs_game` (`game_id`),
  ADD KEY `idx_dlcs_title` (`title`);

--
-- Índices de tabela `games`
--
ALTER TABLE `games`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_games_dev` (`developer_id`),
  ADD KEY `fk_games_pub` (`publisher_id`),
  ADD KEY `idx_games_title` (`title`);

--
-- Índices de tabela `game_genres`
--
ALTER TABLE `game_genres`
  ADD PRIMARY KEY (`game_id`,`genre_id`),
  ADD KEY `fk_gg_genre` (`genre_id`);

--
-- Índices de tabela `game_platforms`
--
ALTER TABLE `game_platforms`
  ADD PRIMARY KEY (`game_id`,`platform_id`),
  ADD KEY `fk_gp_platform` (`platform_id`);

--
-- Índices de tabela `game_tags`
--
ALTER TABLE `game_tags`
  ADD PRIMARY KEY (`game_id`,`tag_id`),
  ADD KEY `fk_gt_tag` (`tag_id`);

--
-- Índices de tabela `genres`
--
ALTER TABLE `genres`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Índices de tabela `gift_cards`
--
ALTER TABLE `gift_cards`
  ADD PRIMARY KEY (`code`),
  ADD KEY `fk_gc_user` (`redeemed_by`);

--
-- Índices de tabela `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_orders_user` (`user_id`,`created_at`);

--
-- Índices de tabela `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_oi_order` (`order_id`),
  ADD KEY `idx_item_lookup` (`item_type`,`item_id`);

--
-- Índices de tabela `platforms`
--
ALTER TABLE `platforms`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Índices de tabela `publishers`
--
ALTER TABLE `publishers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Índices de tabela `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_user_game_review` (`user_id`,`game_id`),
  ADD KEY `fk_rev_game` (`game_id`);

--
-- Índices de tabela `subscriptions`
--
ALTER TABLE `subscriptions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Índices de tabela `tags`
--
ALTER TABLE `tags`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Índices de tabela `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Índices de tabela `user_library`
--
ALTER TABLE `user_library`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_user_item` (`user_id`,`item_type`,`item_id`);

--
-- Índices de tabela `user_subscriptions`
--
ALTER TABLE `user_subscriptions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_us_sub` (`subscription_id`),
  ADD KEY `idx_user_sub_unique` (`user_id`,`subscription_id`,`start_date`);

--
-- Índices de tabela `user_wallet`
--
ALTER TABLE `user_wallet`
  ADD PRIMARY KEY (`user_id`);

--
-- Índices de tabela `wishlists`
--
ALTER TABLE `wishlists`
  ADD PRIMARY KEY (`user_id`,`game_id`),
  ADD KEY `fk_wl_game` (`game_id`);

--
-- AUTO_INCREMENT para tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `app_store`
--
ALTER TABLE `app_store`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de tabela `company`
--
ALTER TABLE `company`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de tabela `developers`
--
ALTER TABLE `developers`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `dlcs`
--
ALTER TABLE `dlcs`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de tabela `games`
--
ALTER TABLE `games`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `genres`
--
ALTER TABLE `genres`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de tabela `orders`
--
ALTER TABLE `orders`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de tabela `order_items`
--
ALTER TABLE `order_items`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de tabela `platforms`
--
ALTER TABLE `platforms`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `publishers`
--
ALTER TABLE `publishers`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de tabela `subscriptions`
--
ALTER TABLE `subscriptions`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de tabela `tags`
--
ALTER TABLE `tags`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de tabela `users`
--
ALTER TABLE `users`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de tabela `user_library`
--
ALTER TABLE `user_library`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de tabela `user_subscriptions`
--
ALTER TABLE `user_subscriptions`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Restrições para tabelas despejadas
--

--
-- Restrições para tabelas `app_store`
--
ALTER TABLE `app_store`
  ADD CONSTRAINT `fk_store_company` FOREIGN KEY (`owner_id`) REFERENCES `company` (`id`);

--
-- Restrições para tabelas `dlcs`
--
ALTER TABLE `dlcs`
  ADD CONSTRAINT `fk_dlcs_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `games`
--
ALTER TABLE `games`
  ADD CONSTRAINT `fk_games_dev` FOREIGN KEY (`developer_id`) REFERENCES `developers` (`id`),
  ADD CONSTRAINT `fk_games_pub` FOREIGN KEY (`publisher_id`) REFERENCES `publishers` (`id`);

--
-- Restrições para tabelas `game_genres`
--
ALTER TABLE `game_genres`
  ADD CONSTRAINT `fk_gg_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_gg_genre` FOREIGN KEY (`genre_id`) REFERENCES `genres` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `game_platforms`
--
ALTER TABLE `game_platforms`
  ADD CONSTRAINT `fk_gp_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_gp_platform` FOREIGN KEY (`platform_id`) REFERENCES `platforms` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `game_tags`
--
ALTER TABLE `game_tags`
  ADD CONSTRAINT `fk_gt_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_gt_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `gift_cards`
--
ALTER TABLE `gift_cards`
  ADD CONSTRAINT `fk_gc_user` FOREIGN KEY (`redeemed_by`) REFERENCES `users` (`id`);

--
-- Restrições para tabelas `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `fk_oi_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `fk_rev_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_rev_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `user_library`
--
ALTER TABLE `user_library`
  ADD CONSTRAINT `fk_lib_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `user_subscriptions`
--
ALTER TABLE `user_subscriptions`
  ADD CONSTRAINT `fk_us_sub` FOREIGN KEY (`subscription_id`) REFERENCES `subscriptions` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_us_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `user_wallet`
--
ALTER TABLE `user_wallet`
  ADD CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Restrições para tabelas `wishlists`
--
ALTER TABLE `wishlists`
  ADD CONSTRAINT `fk_wl_game` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_wl_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
