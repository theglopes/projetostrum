package com.loja.models;

public class GameSubmission {
    private final int id;
    private final int userId;
    private final String userEmail;
    private final String nome;
    private final double preco;
    private final String imagem;
    private final boolean promocao;
    private final String status;
    private final long createdAt;
    private final long updatedAt;
    private final Integer resolvedBy;
    private final String resolvedByEmail;
    private final Long resolvedAt;
    private final Integer approvedGameId;

    public GameSubmission(
        int id,
        int userId,
        String userEmail,
        String nome,
        double preco,
        String imagem,
        boolean promocao,
        String status,
        long createdAt,
        long updatedAt,
        Integer resolvedBy,
        String resolvedByEmail,
        Long resolvedAt,
        Integer approvedGameId
    ) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.nome = nome;
        this.preco = preco;
        this.imagem = imagem;
        this.promocao = promocao;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedBy = resolvedBy;
        this.resolvedByEmail = resolvedByEmail;
        this.resolvedAt = resolvedAt;
        this.approvedGameId = approvedGameId;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public String getImagem() {
        return imagem;
    }

    public boolean isPromocao() {
        return promocao;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public Integer getResolvedBy() {
        return resolvedBy;
    }

    public String getResolvedByEmail() {
        return resolvedByEmail;
    }

    public Long getResolvedAt() {
        return resolvedAt;
    }

    public Integer getApprovedGameId() {
        return approvedGameId;
    }
}
