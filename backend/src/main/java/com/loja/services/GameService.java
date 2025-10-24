package com.loja.services;

import com.loja.models.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameService {
    private final List<Game> jogos = new ArrayList<>();

    public void adicionarGame(Game g) {
        jogos.add(g);
    }

    public List<Game> listarGames() {
        return new ArrayList<>(jogos);
    }

    public Optional<Game> buscarPorId(int id) {
        return jogos.stream().filter(x -> x.getId() == id).findFirst();
    }

    public boolean removerGame(int id) {
        return jogos.removeIf(g -> g.getId() == id);
    }

    public List<Game> buscarPorPlataforma(String plataforma) {
        List<Game> res = new ArrayList<>();
        for (Game g : jogos) if (g.getPlataforma().equalsIgnoreCase(plataforma)) res.add(g);
        return res;
    }

    public List<Game> emPromocao() {
        List<Game> res = new ArrayList<>();
        for (Game g : jogos) if (g.isPromocao()) res.add(g);
        return res;
    }
}