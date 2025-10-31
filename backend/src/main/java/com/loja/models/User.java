package com.loja.models;

public class User {
    private int id;
    private String email;
    private String passwordHash;
    private long createdAt;

    public User(int id, String email, String passwordHash, long createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
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
}
