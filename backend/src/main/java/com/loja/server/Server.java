package com.loja.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.loja.models.Game;
import com.loja.services.GameService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Server {

    public static void main(String[] args) throws Exception {
        GameService service = new GameService();

        // Adicionando os jogos (com caminho de imagem)
        service.adicionarGame(new Game("Grand Theft Auto 6", true, "css/Imagens/grand-theft-auto-6.png"));

        // Observação: não há arquivo para GTA 5; usando temporariamente a imagem do GTA 6
        // Removido item duplicado de GTA 6 (antes era GTA 5)

        service.adicionarGame(new Game("Metal Gear Solid V: The Phantom Pain", true, "css/Imagens/metal-gear-solid-v-the-phantom-pain.jpg"));
        service.adicionarGame(new Game("Alan Wake 2", false, "css/Imagens/alan-wake-2.jpeg"));
        service.adicionarGame(new Game("Assassin's Creed Mirage", true, "css/Imagens/assassins-creed-mirage.png"));
        service.adicionarGame(new Game("Dark Souls Remastered", false, "css/Imagens/dark-souls-remastered.png"));
        service.adicionarGame(new Game("Cyberpunk 2077", true, "css/Imagens/cyberpunk-2077.jpg"));
        service.adicionarGame(new Game("Dying Light 2 Stay Human", false, "css/Imagens/dying-light-2-stay-human.jpeg"));
        service.adicionarGame(new Game("Detroit Become Human", false, "css/Imagens/detroit-become-human.jpg"));
        service.adicionarGame(new Game("God of War", true, "css/Imagens/god-of-war.jpeg"));
        service.adicionarGame(new Game("Elden Ring", true, "css/Imagens/elden-ring.png"));
        service.adicionarGame(new Game("Hogwarts Legacy", false, "css/Imagens/hogwarts-legacy.png"));
        service.adicionarGame(new Game("Hades", false, "css/Imagens/hades.jpg"));
        service.adicionarGame(new Game("Grand Theft Auto San Andreas", true, "css/Imagens/grand-theft-auto-san-andreas.jpg"));
        service.adicionarGame(new Game("Hollow Knight", false, "css/Imagens/hollow-knight.jpg"));
        service.adicionarGame(new Game("Resident Evil 4", true, "css/Imagens/resident-evil-4.png"));
        service.adicionarGame(new Game("Red Dead Redemption 2", false, "css/Imagens/red-dead-redemption-2.jpg"));
        service.adicionarGame(new Game("The Last of Us Part 1", true, "css/Imagens/the-last-of-us-part-1.png"));
        service.adicionarGame(new Game("The Witcher 3", true, "css/Imagens/the-witcher-3.jpg"));

        // Criando servidor HTTP na porta 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Endpoint "/games" retorna a lista de jogos em JSON
        server.createContext("/games", (HttpExchange exchange) -> {
            try {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equals(exchange.getRequestMethod())) {
                    Gson gson = new Gson();
                    String json = gson.toJson(service.listarGames());
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1);
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
