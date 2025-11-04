package com.loja.services;

import com.loja.db.Database;
import com.loja.models.GameSubmission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GameSubmissionService {
    private static final Set<String> VALID_STATUS = Set.of("PENDING", "APPROVED", "REJECTED");

    public GameSubmission submit(int userId, String nome, double preco, String imagem, boolean promocao) {
        String normalizedName = normalizeString(nome);
        if (normalizedName == null) {
            throw new IllegalArgumentException("Nome do jogo obrigatorio.");
        }
        if (!Double.isFinite(preco) || preco < 0) {
            throw new IllegalArgumentException("Preco do jogo invalido.");
        }
        long now = Database.unixNow();
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            int submissionId;
            try {
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO game_submissions (user_id, name, price, image, promo, status, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, 'PENDING', ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setString(2, normalizedName);
                    ps.setDouble(3, preco);
                    ps.setString(4, normalizeString(imagem));
                    ps.setInt(5, promocao ? 1 : 0);
                    ps.setLong(6, now);
                    ps.setLong(7, now);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            throw new SQLException("Falha ao obter id da submissao.");
                        }
                        submissionId = keys.getInt(1);
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
            return findById(submissionId).orElseThrow(() -> new SQLException("Submissao nao encontrada apos inserir."));
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao registrar submissao de jogo", e);
        }
    }

    public List<GameSubmission> listAll() {
        String sql = """
            SELECT
                gs.id,
                gs.user_id,
                u.email AS user_email,
                gs.name,
                gs.price,
                gs.image,
                gs.promo,
                gs.status,
                gs.created_at,
                gs.updated_at,
                gs.approved_game_id,
                gs.resolved_by,
                gs.resolved_at,
                resolver.email AS resolved_email
            FROM game_submissions gs
            JOIN users u ON u.id = gs.user_id
            LEFT JOIN users resolver ON resolver.id = gs.resolved_by
            ORDER BY
                CASE UPPER(gs.status)
                    WHEN 'PENDING' THEN 0
                    WHEN 'APPROVED' THEN 1
                    ELSE 2
                END,
                gs.created_at DESC
        """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<GameSubmission> submissions = new ArrayList<>();
            while (rs.next()) {
                submissions.add(mapSubmission(rs));
            }
            return submissions;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar submissões de jogo", e);
        }
    }

    public List<GameSubmission> listByUser(int userId) {
        String sql = """
            SELECT
                gs.id,
                gs.user_id,
                u.email AS user_email,
                gs.name,
                gs.price,
                gs.image,
                gs.promo,
                gs.status,
                gs.created_at,
                gs.updated_at,
                gs.approved_game_id,
                gs.resolved_by,
                gs.resolved_at,
                resolver.email AS resolved_email
            FROM game_submissions gs
            JOIN users u ON u.id = gs.user_id
            LEFT JOIN users resolver ON resolver.id = gs.resolved_by
            WHERE gs.user_id = ?
            ORDER BY gs.created_at DESC
        """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<GameSubmission> submissions = new ArrayList<>();
                while (rs.next()) {
                    submissions.add(mapSubmission(rs));
                }
                return submissions;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar submissões do desenvolvedor", e);
        }
    }

    public Optional<GameSubmission> findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar submissao de jogo", e);
        }
    }

    public Optional<GameSubmission> updateStatus(int submissionId, String newStatus, Integer resolverId) {
        String normalized = normalizeStatus(newStatus);
        if (normalized == null || !VALID_STATUS.contains(normalized)) {
            throw new IllegalArgumentException("Status invalido.");
        }
        long now = Database.unixNow();
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<GameSubmission> existingOpt = findById(conn, submissionId);
                if (existingOpt.isEmpty()) {
                    conn.rollback();
                    return Optional.empty();
                }
                GameSubmission existing = existingOpt.get();
                Integer approvedGameId = existing.getApprovedGameId();
                if ("APPROVED".equals(normalized) && approvedGameId == null) {
                    approvedGameId = ensureGameExists(conn, existing);
                }

                Integer resolvedByValue = null;
                Long resolvedAtValue = null;
                if (!"PENDING".equals(normalized) && resolverId != null) {
                    resolvedByValue = resolverId;
                    resolvedAtValue = now;
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE game_submissions
                        SET status = ?, updated_at = ?, resolved_by = ?, resolved_at = ?, approved_game_id = ?
                        WHERE id = ?
                    """)) {
                    ps.setString(1, normalized);
                    ps.setLong(2, now);
                    if (resolvedByValue == null) {
                        ps.setNull(3, java.sql.Types.INTEGER);
                        ps.setNull(4, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(3, resolvedByValue);
                        ps.setLong(4, resolvedAtValue);
                    }
                    if ("APPROVED".equals(normalized) && approvedGameId != null) {
                        ps.setInt(5, approvedGameId);
                    } else if ("APPROVED".equals(normalized)) {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    }
                    ps.setInt(6, submissionId);
                    ps.executeUpdate();
                }
                conn.commit();
                return findById(conn, submissionId);
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw new RuntimeException("Falha ao atualizar submissao de jogo", ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar submissao de jogo", e);
        }
    }

    private Optional<GameSubmission> findById(Connection conn, int id) throws SQLException {
        String sql = """
            SELECT
                gs.id,
                gs.user_id,
                u.email AS user_email,
                gs.name,
                gs.price,
                gs.image,
                gs.promo,
                gs.status,
                gs.created_at,
                gs.updated_at,
                gs.approved_game_id,
                gs.resolved_by,
                gs.resolved_at,
                resolver.email AS resolved_email
            FROM game_submissions gs
            JOIN users u ON u.id = gs.user_id
            LEFT JOIN users resolver ON resolver.id = gs.resolved_by
            WHERE gs.id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSubmission(rs));
                }
            }
        }
        return Optional.empty();
    }

    private int ensureGameExists(Connection conn, GameSubmission submission) throws SQLException {
        int existingId = findGameIdByName(conn, submission.getNome());
        if (existingId > 0) {
            return existingId;
        }
        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO games(name, price, image, promo)
                VALUES (?, ?, ?, ?)
            """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, submission.getNome());
            ps.setDouble(2, submission.getPreco());
            ps.setString(3, submission.getImagem());
            ps.setInt(4, submission.isPromocao() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Falha ao criar jogo aprovado.");
    }

    private int findGameIdByName(Connection conn, String nome) throws SQLException {
        if (nome == null || nome.isBlank()) {
            return 0;
        }
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT id FROM games WHERE LOWER(name) = LOWER(?) LIMIT 1
            """)) {
            ps.setString(1, nome.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return 0;
    }

    private GameSubmission mapSubmission(ResultSet rs) throws SQLException {
        int resolvedBy = rs.getInt("resolved_by");
        Integer resolvedByValue = rs.wasNull() ? null : resolvedBy;
        long resolvedAtRaw = rs.getLong("resolved_at");
        Long resolvedAtValue = rs.wasNull() ? null : resolvedAtRaw;
        int approvedGameIdRaw = rs.getInt("approved_game_id");
        Integer approvedGameId = rs.wasNull() ? null : approvedGameIdRaw;

        return new GameSubmission(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("user_email"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getString("image"),
            rs.getInt("promo") == 1,
            rs.getString("status"),
            rs.getLong("created_at"),
            rs.getLong("updated_at"),
            resolvedByValue,
            rs.getString("resolved_email"),
            resolvedAtValue,
            approvedGameId
        );
    }

    private String normalizeString(String value) {
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
        String trimmed = status.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase();
    }
}
