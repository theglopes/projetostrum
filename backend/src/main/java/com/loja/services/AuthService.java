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
import java.util.regex.Pattern;

public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long SESSION_TTL_SECONDS = 60L * 60L * 24L; // 24h
    private static final String DEFAULT_ROLE = "USER";
    private static final String DEFAULT_PLAN = "FREE";
    private static final Pattern GAMERTAG_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,20}$");

    public Optional<User> register(String email, String password, String gamertag) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Informe um e-mail valido.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("A senha precisa ter pelo menos 4 caracteres.");
        }
        String normalizedGamertag = normalizeGamertag(gamertag);
        if (normalizedGamertag == null) {
            throw new IllegalArgumentException("Gamertag invalida. Use entre 3 e 20 caracteres contendo letras, numeros, '.', '-' ou '_'.");
        }
        if (!isGamertagAvailableNormalized(normalizedGamertag)) {
            throw new IllegalArgumentException("Gamertag ja esta em uso.");
        }

        String hashed = PasswordUtils.hashPassword(password);
        String sql = "INSERT INTO users(email, gamertag, password_hash, role, plan, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        long now = Database.unixNow();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, normalizedEmail);
            ps.setString(2, normalizedGamertag);
            ps.setString(3, hashed);
            ps.setString(4, DEFAULT_ROLE);
            ps.setString(5, DEFAULT_PLAN);
            ps.setLong(6, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return Optional.of(new User(id, normalizedEmail, normalizedGamertag, hashed, now, DEFAULT_ROLE, DEFAULT_PLAN));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            if (isConstraintViolation(e, "users.email")) {
                throw new IllegalArgumentException("E-mail ja cadastrado.");
            }
            if (isConstraintViolation(e, "users.gamertag")) {
                throw new IllegalArgumentException("Gamertag ja esta em uso.");
            }
            return Optional.empty();
        }
    }

    public Optional<User> authenticate(String email, String password) {
        String normalized = normalizeEmail(email);
        if (normalized == null || password == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, email, gamertag, password_hash, role, plan, created_at FROM users WHERE email = ?";
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
                            rs.getString("gamertag"),
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
            SELECT u.id, u.email, u.gamertag, u.password_hash, u.created_at, u.role, u.plan
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
                        rs.getString("gamertag"),
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
        String sql = "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalStateException("Nenhum administrador cadastrado. Cadastre manualmente no banco.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar administrador padrao", e);
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

    private String normalizeGamertag(String gamertag) {
        if (gamertag == null) {
            return null;
        }
        String trimmed = gamertag.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return GAMERTAG_PATTERN.matcher(trimmed).matches() ? trimmed : null;
    }

    private boolean isGamertagAvailableNormalized(String normalizedGamertag) {
        if (normalizedGamertag == null) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE gamertag = ? COLLATE NOCASE";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedGamertag);
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar gamertag", e);
        }
    }

    private boolean isConstraintViolation(SQLException e, String token) {
        if (e == null || token == null) {
            return false;
        }
        String message = e.getMessage();
        return message != null && message.contains(token);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
