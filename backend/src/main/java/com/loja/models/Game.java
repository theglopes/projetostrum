package com.loja.models;

public class Game {
    private int id;
    private String nome;
    private double preco;
    private String plataforma;
    private boolean promocao;

    public Game(int id, String nome, double preco, String plataforma, boolean promocao) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.plataforma = plataforma;
        this.promocao = promocao;
    }

    // Construtor mínimo (uso no Server se quiser apenas nome+promo)
    public Game(String nome, boolean promocao) {
        this(0, nome, 0.0, "PC", promocao);
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public String getPlataforma() { return plataforma; }
    public boolean isPromocao() { return promocao; }

    @Override
    public String toString() {
        return String.format("[%d] %s — %s — R$ %.2f %s",
                id, nome, plataforma, preco, promocao ? "(PROMO)" : "");
    }
}