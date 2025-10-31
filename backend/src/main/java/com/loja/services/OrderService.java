package com.loja.services;

import com.loja.db.Database;
import com.loja.models.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public record CheckoutItem(int gameId, int quantity) {}

    public record CheckoutResult(int orderId, double total, List<Game> games) {}

    public CheckoutResult checkout(int userId, List<CheckoutItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Carrinho vazio");
        }

        double total = 0.0;
        List<Game> games = new ArrayList<>();

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (CheckoutItem item : items) {
                    Game game = fetchGame(conn, item.gameId());
                    if (game == null) {
                        throw new IllegalArgumentException("Jogo não encontrado: " + item.gameId());
                    }
                    int qty = Math.max(1, item.quantity());
                    total += game.getPreco() * qty;
                    games.add(new Game(game.getId(), game.getNome(), game.getPreco(), game.getPlataforma(), game.isPromocao(), game.getImagem()));
                }

                int orderId = insertOrder(conn, userId, total);
                insertOrderItems(conn, orderId, items, games);
                conn.commit();
                return new CheckoutResult(orderId, total, games);
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao processar checkout", e);
        }
    }

    private Game fetchGame(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, name, price, image, promo FROM games WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Game(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        "PC",
                        rs.getInt("promo") == 1,
                        rs.getString("image")
                    );
                }
            }
        }
        return null;
    }

    private int insertOrder(Connection conn, int userId, double total) throws SQLException {
        String sql = "INSERT INTO orders(user_id, total, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setDouble(2, total);
            ps.setLong(3, Database.unixNow());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Não foi possível criar pedido");
    }

    private void insertOrderItems(Connection conn, int orderId, List<CheckoutItem> items, List<Game> games) throws SQLException {
        String sql = "INSERT INTO order_items(order_id, game_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < items.size(); i++) {
                CheckoutItem item = items.get(i);
                Game game = games.get(i);
                ps.setInt(1, orderId);
                ps.setInt(2, game.getId());
                ps.setInt(3, Math.max(1, item.quantity()));
                ps.setDouble(4, game.getPreco());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
