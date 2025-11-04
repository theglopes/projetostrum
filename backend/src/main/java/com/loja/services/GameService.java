package com.loja.services;

import com.loja.db.Database;
import com.loja.models.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameService {

    public List<Game> listarGames() {
        String sql = "SELECT id, name, price, image, promo FROM games ORDER BY name";
        List<Game> jogos = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                jogos.add(mapGame(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar jogos", e);
        }
        return jogos;
    }

    public Optional<Game> buscarPorId(int id) {
        String sql = "SELECT id, name, price, image, promo FROM games WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapGame(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar jogo por id", e);
        }
        return Optional.empty();
    }

    public int adicionarGame(Game g) {
        String sql = "INSERT INTO games(name, price, image, promo) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getNome());
            ps.setDouble(2, g.getPreco());
            ps.setString(3, g.getImagem());
            ps.setInt(4, g.isPromocao() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    g.setId(id);
                    return id;
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar jogo", e);
        }
    }

    public boolean removerGame(int id) {
        String sql = "DELETE FROM games WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover jogo", e);
        }
    }

    public List<Game> buscarPorPlataforma(String plataforma) {
        // Como ainda não persistimos a plataforma, retornamos todos os jogos para fins de demonstração.
        return listarGames();
    }

    public List<Game> emPromocao() {
        String sql = "SELECT id, name, price, image, promo FROM games WHERE promo = 1";
        List<Game> jogos = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                jogos.add(mapGame(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar jogos em promoção", e);
        }
        return jogos;
    }

    private Game mapGame(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        String image = rs.getString("image");
        boolean promo = rs.getInt("promo") == 1;
        Game g = new Game(id, name, price, "PC", promo, image);
        return g;
    }
}
