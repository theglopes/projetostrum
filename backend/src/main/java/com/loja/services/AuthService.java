package com.loja.services;

import com.loja.db.Database;
import com.loja.models.User;
import com.loja.utils.PasswordUtils;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long SESSION_TTL_SECONDS = 60L * 60L * 24L; // 24h
    private static final String DEFAULT_ROLE = "USER";
    private static final String DEFAULT_PLAN = "FREE";

    public Optional<User> register(String email, String password) {
        String normalized = normalizeEmail(email);
        if (normalized == null || password == null || password.length() < 4) {
            return Optional.empty();
        }

        String hashed = PasswordUtils.hashPassword(password);
        String sql = "INSERT INTO users(email, password_hash, role, plan, created_at) VALUES (?, ?, ?, ?, ?)";
        long now = Database.unixNow();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, normalized);
            ps.setString(2, hashed);
            ps.setString(3, DEFAULT_ROLE);
            ps.setString(4, DEFAULT_PLAN);
            ps.setLong(5, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return Optional.of(new User(id, normalized, hashed, now, DEFAULT_ROLE, DEFAULT_PLAN));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            // email duplicado retorna empty
            return Optional.empty();
        }
    }

    public Optional<User> authenticate(String email, String password) {
        String normalized = normalizeEmail(email);
        if (normalized == null || password == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, email, password_hash, role, plan, created_at FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (PasswordUtils.verifyPassword(password, hash)) {
                        return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            hash,
                            rs.getLong("created_at"),
                            rs.getString("role"),
                            rs.getString("plan")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário", e);
        }
        return Optional.empty();
    }

    public String createSession(int userId) {
        String token = generateToken();
        String sql = "INSERT INTO sessions(token, user_id, expires_at, created_at) VALUES (?, ?, ?, ?)";
        long now = Database.unixNow();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, userId);
            ps.setLong(3, now + SESSION_TTL_SECONDS);
            ps.setLong(4, now);
            ps.executeUpdate();
            return token;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar sessão", e);
        }
    }

    public Optional<User> findUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String sql = """
            SELECT u.id, u.email, u.password_hash, u.created_at, u.role, u.plan
            FROM sessions s
            JOIN users u ON u.id = s.user_id
            WHERE s.token = ? AND s.expires_at > ?
        """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setLong(2, Database.unixNow());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getLong("created_at"),
                        rs.getString("role"),
                        rs.getString("plan")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar sessão", e);
        }
        return Optional.empty();
    }

    public void ensureDefaultAdmin() {
        String normalized = normalizeEmail(DEFAULT_ADMIN_EMAIL);
        if (normalized == null) {
            return;
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            Integer userId = null;
            String currentRole = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, role FROM users WHERE email = ?")) {
                ps.setString(1, normalized);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                        currentRole = rs.getString("role");
                    }
                }
            }

            if (userId == null) {
                String hashed = PasswordUtils.hashPassword(DEFAULT_ADMIN_PASSWORD);
                long now = Database.unixNow();
                try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO users(email, password_hash, role, plan, created_at) VALUES (?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
                )) {
                    insert.setString(1, normalized);
                    insert.setString(2, hashed);
                    insert.setString(3, "ADMIN");
                    insert.setString(4, "PREMIUM");
                    insert.setLong(5, now);
                    insert.executeUpdate();
                }
            } else if (!"ADMIN".equalsIgnoreCase(currentRole)) {
                try (PreparedStatement update = conn.prepareStatement("UPDATE users SET role = 'ADMIN' WHERE id = ?")) {
                    update.setInt(1, userId);
                    update.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao garantir administrador padrão", e);
        }
    }

    public void invalidateToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String sql = "DELETE FROM sessions WHERE token = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            // ignore
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim().toLowerCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
