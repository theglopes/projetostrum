package com.loja.models;

public class User {
    private int id;
    private String email;
    private String passwordHash;
    private long createdAt;
    private String role;
    private String plan;

    public User(int id, String email, String passwordHash, long createdAt, String role, String plan) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.role = role;
        this.plan = plan;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getRole() {
        return role;
    }

    public String getPlan() {
        return plan;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isModerator() {
        return "MODERATOR".equalsIgnoreCase(role);
    }

    public boolean isDeveloper() {
        return "DEVELOPER".equalsIgnoreCase(role);
    }

    public boolean hasPremium() {
        return !"FREE".equalsIgnoreCase(plan);
    }
}
