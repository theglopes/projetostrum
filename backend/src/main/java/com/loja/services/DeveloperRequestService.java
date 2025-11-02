package com.loja.services;

import com.loja.db.Database;
import com.loja.models.DeveloperRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DeveloperRequestService {
    private static final Set<String> VALID_STATUS = Set.of("PENDING", "APPROVED", "REJECTED");

    public Optional<DeveloperRequest> findByUserId(int userId) {
        try (Connection conn = Database.getConnection()) {
            return findByUserId(conn, userId);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar solicitacao de desenvolvedor", e);
        }
    }

    public Optional<DeveloperRequest> findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar solicitacao de desenvolvedor", e);
        }
    }

    public DeveloperRequest createOrReset(int userId, String studioName, String cnpj) {
        String normalizedStudio = normalize(studioName);
        String normalizedCnpj = normalize(cnpj);
        if (normalizedStudio == null || normalizedCnpj == null) {
            throw new IllegalArgumentException("Estudio e CNPJ sao obrigatorios.");
        }
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<DeveloperRequest> existingOpt = findByUserId(conn, userId);
                long now = Database.unixNow();
                int requestId;
                if (existingOpt.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO developer_requests(user_id, studio_name, cnpj, status, created_at, updated_at) VALUES (?, ?, ?, 'PENDING', ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                    )) {
                        ps.setInt(1, userId);
                        ps.setString(2, normalizedStudio);
                        ps.setString(3, normalizedCnpj);
                        ps.setLong(4, now);
                        ps.setLong(5, now);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                requestId = keys.getInt(1);
                            } else {
                                throw new SQLException("Falha ao recuperar ID da solicitacao de desenvolvedor.");
                            }
                        }
                    }
                } else {
                    requestId = existingOpt.get().getId();
                    try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE developer_requests SET studio_name = ?, cnpj = ?, status = 'PENDING', updated_at = ?, resolved_by = NULL, resolved_at = NULL WHERE id = ?"
                    )) {
                        ps.setString(1, normalizedStudio);
                        ps.setString(2, normalizedCnpj);
                        ps.setLong(3, now);
                        ps.setInt(4, requestId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return findById(conn, requestId).orElseThrow(() -> new SQLException("Solicitacao nao encontrada apos insercao."));
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new RuntimeException("Falha ao registrar solicitacao de desenvolvedor", ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao registrar solicitacao de desenvolvedor", e);
        }
    }

    public List<DeveloperRequest> listAll() {
        String sql = """
            SELECT
                dr.id,
                dr.user_id,
                u.email AS user_email,
                dr.studio_name,
                dr.cnpj,
                dr.status,
                dr.created_at,
                dr.updated_at,
                dr.resolved_by,
                dr.resolved_at,
                resolver.email AS resolved_email
            FROM developer_requests dr
            JOIN users u ON u.id = dr.user_id
            LEFT JOIN users resolver ON resolver.id = dr.resolved_by
            ORDER BY CASE WHEN dr.status = 'PENDING' THEN 0 ELSE 1 END, dr.created_at ASC
        """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<DeveloperRequest> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapDeveloperRequest(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar solicitacoes de desenvolvedor", e);
        }
    }

    public Optional<DeveloperRequest> updateStatus(int requestId, String newStatus, Integer resolverId) {
        String normalized = normalizeStatus(newStatus);
        if (normalized == null || !VALID_STATUS.contains(normalized)) {
            throw new IllegalArgumentException("Status invalido.");
        }
        long now = Database.unixNow();
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE developer_requests SET status = ?, updated_at = ?, resolved_by = ?, resolved_at = ? WHERE id = ?"
                )) {
                    ps.setString(1, normalized);
                    ps.setLong(2, now);
                    if (resolverId == null) {
                        ps.setNull(3, java.sql.Types.INTEGER);
                        ps.setNull(4, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(3, resolverId);
                        ps.setLong(4, now);
                    }
                    ps.setInt(5, requestId);
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        return Optional.empty();
                    }
                }
                conn.commit();
                return findById(conn, requestId);
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new RuntimeException("Falha ao atualizar solicitacao de desenvolvedor", ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar solicitacao de desenvolvedor", e);
        }
    }

    private Optional<DeveloperRequest> findByUserId(Connection conn, int userId) throws SQLException {
        String sql = """
            SELECT
                dr.id,
                dr.user_id,
                u.email AS user_email,
                dr.studio_name,
                dr.cnpj,
                dr.status,
                dr.created_at,
                dr.updated_at,
                dr.resolved_by,
                dr.resolved_at,
                resolver.email AS resolved_email
            FROM developer_requests dr
            JOIN users u ON u.id = dr.user_id
            LEFT JOIN users resolver ON resolver.id = dr.resolved_by
            WHERE dr.user_id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDeveloperRequest(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<DeveloperRequest> findById(Connection conn, int id) throws SQLException {
        String sql = """
            SELECT
                dr.id,
                dr.user_id,
                u.email AS user_email,
                dr.studio_name,
                dr.cnpj,
                dr.status,
                dr.created_at,
                dr.updated_at,
                dr.resolved_by,
                dr.resolved_at,
                resolver.email AS resolved_email
            FROM developer_requests dr
            JOIN users u ON u.id = dr.user_id
            LEFT JOIN users resolver ON resolver.id = dr.resolved_by
            WHERE dr.id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDeveloperRequest(rs));
                }
            }
        }
        return Optional.empty();
    }

    private DeveloperRequest mapDeveloperRequest(ResultSet rs) throws SQLException {
        int resolvedBy = rs.getInt("resolved_by");
        Integer resolvedByValue = rs.wasNull() ? null : resolvedBy;
        long resolvedAtRaw = rs.getLong("resolved_at");
        Long resolvedAtValue = rs.wasNull() ? null : resolvedAtRaw;
        return new DeveloperRequest(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("user_email"),
            rs.getString("studio_name"),
            rs.getString("cnpj"),
            rs.getString("status"),
            rs.getLong("created_at"),
            rs.getLong("updated_at"),
            resolvedByValue,
            rs.getString("resolved_email"),
            resolvedAtValue
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        String trimmed = status.trim().toUpperCase();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
