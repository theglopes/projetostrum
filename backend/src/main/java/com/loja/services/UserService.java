package com.loja.services;

import com.loja.db.Database;
import com.loja.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UserService {
    private static final Set<String> VALID_ROLES = Set.of("USER", "DEVELOPER", "MODERATOR", "ADMIN");
    private static final Set<String> VALID_PLANS = Set.of("FREE", "PREMIUM");

    public List<User> listUsers() {
        String sql = "SELECT id, email, gamertag, password_hash, role, plan, created_at FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar usuários", e);
        }
        return users;
    }

    public Optional<User> findById(int userId) {
        String sql = "SELECT id, email, gamertag, password_hash, role, plan, created_at FROM users WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar usuário", e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String normalized = email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT id, email, gamertag, password_hash, role, plan, created_at FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar usuário por e-mail", e);
        }
        return Optional.empty();
    }

    public Optional<String> normalizeRoleValue(String role) {
        String normalized = normalizeRole(role);
        if (normalized == null || !VALID_ROLES.contains(normalized)) {
            return Optional.empty();
        }
        return Optional.of(normalized);
    }

    public boolean hasOtherAdmins(int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE UPPER(role) = 'ADMIN' AND id <> ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao verificar administradores ativos", e);
        }
        return false;
    }

    public Optional<User> updateRole(int userId, String newRole) {
        Optional<String> normalizedRoleOpt = normalizeRoleValue(newRole);
        if (normalizedRoleOpt.isEmpty()) {
            return Optional.empty();
        }
        String normalizedRole = normalizedRoleOpt.get();

        Optional<User> current = findById(userId);
        if (current.isEmpty()) {
            return Optional.empty();
        }
        User existing = current.get();
        if (existing.isAdmin() && !"ADMIN".equalsIgnoreCase(normalizedRole) && !hasOtherAdmins(userId)) {
            throw new IllegalArgumentException("Nao e possivel remover o ultimo administrador");
        }

        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedRole);
            ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar papel do usuário", e);
        }
        return findById(userId);
    }

    public Optional<User> updatePlan(int userId, String newPlan) {
        String normalizedPlan = normalizePlan(newPlan);
        if (normalizedPlan == null || !VALID_PLANS.contains(normalizedPlan)) {
            return Optional.empty();
        }
        String sql = "UPDATE users SET plan = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedPlan);
            ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar plano do usuário", e);
        }
        return findById(userId);
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        String trimmed = role.trim().toUpperCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizePlan(String plan) {
        if (plan == null) {
            return null;
        }
        String trimmed = plan.trim().toUpperCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("email"),
            rs.getString("gamertag"),
            rs.getString("password_hash"),
            rs.getLong("created_at"),
            rs.getString("role"),
            rs.getString("plan")
        );
    }
}
