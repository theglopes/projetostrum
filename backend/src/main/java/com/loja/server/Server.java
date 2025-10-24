package com.loja.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.loja.models.Game;
import com.loja.services.GameService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Server {

    public static void main(String[] args) throws Exception {
        GameService service = new GameService();

        // Adicionando os jogos
        service.adicionarGame(new Game("Grand Theft Auto 5", false));
        service.adicionarGame(new Game("Metal Gear Solid V: The Phantom Pain", true));
        service.adicionarGame(new Game("Alan Wake 2", false));
        service.adicionarGame(new Game("Assassin's Creed Mirage", true));
        service.adicionarGame(new Game("Dark Souls Remastered", false));
        service.adicionarGame(new Game("Cyberpunk 2077", true));
        service.adicionarGame(new Game("Dying Light 2 Stay Human", false));
        service.adicionarGame(new Game("Detroit Become Human", false));
        service.adicionarGame(new Game("God of War", true));
        service.adicionarGame(new Game("Elden Ring", true));
        service.adicionarGame(new Game("Hogwarts Legacy", false));
        service.adicionarGame(new Game("Hades", false));
        service.adicionarGame(new Game("Grand Theft Auto San Andreas", true));
        service.adicionarGame(new Game("Hollow Knight", false));
        service.adicionarGame(new Game("Resident Evil 4", true));
        service.adicionarGame(new Game("Red Dead Redemption 2", false));
        service.adicionarGame(new Game("The Last of Us Part 1", true));
        service.adicionarGame(new Game("The Witcher 3", true));

        // Criando servidor HTTP na porta 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Endpoint "/games" retorna a lista de jogos em JSON
        server.createContext("/games", (HttpExchange exchange) -> {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    Gson gson = new Gson();
                    String json = gson.toJson(service.listarGames());
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, json.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(json.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            } catch (IOException e) {
            }
        });

        server.setExecutor(null); // cria um executor padrão
        server.start();

        System.out.println("Servidor rodando na porta 8000...");
        System.out.println("API disponível em: http://localhost:8000/games");
    }
}