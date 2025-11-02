package com.loja.models;

public class DeveloperRequest {
    private final int id;
    private final int userId;
    private final String userEmail;
    private final String studioName;
    private final String cnpj;
    private final String status;
    private final long createdAt;
    private final long updatedAt;
    private final Integer resolvedBy;
    private final String resolvedByEmail;
    private final Long resolvedAt;

    public DeveloperRequest(
        int id,
        int userId,
        String userEmail,
        String studioName,
        String cnpj,
        String status,
        long createdAt,
        long updatedAt,
        Integer resolvedBy,
        String resolvedByEmail,
        Long resolvedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.studioName = studioName;
        this.cnpj = cnpj;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedBy = resolvedBy;
        this.resolvedByEmail = resolvedByEmail;
        this.resolvedAt = resolvedAt;
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

    public String getStudioName() {
        return studioName;
    }

    public String getCnpj() {
        return cnpj;
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
}

