package com.loja.models;

public class Game {
    private int id;
    private String nome;
    private double preco;
    private String plataforma;
    private boolean promocao;
    private String imagem;

    public Game(int id, String nome, double preco, String plataforma, boolean promocao, String imagem) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.plataforma = plataforma;
        this.promocao = promocao;
        this.imagem = imagem;
    }

    public Game(String nome, boolean promocao, String imagem) {
        this(0, nome, 0.0, "PC", promocao, imagem);
    }

    public Game(String nome, boolean promocao) {
        this(0, nome, 0.0, "PC", promocao, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public boolean isPromocao() {
        return promocao;
    }

    public void setPromocao(boolean promocao) {
        this.promocao = promocao;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s - R$ %.2f %s",
            id, nome, plataforma, preco, promocao ? "(PROMO)" : "");
    }
}
