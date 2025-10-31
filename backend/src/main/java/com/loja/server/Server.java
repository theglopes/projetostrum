package com.loja.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loja.db.Database;
import com.loja.models.Game;
import com.loja.models.User;
import com.loja.services.AuthService;
import com.loja.services.GameService;
import com.loja.services.OrderService;
import com.loja.services.OrderService.CheckoutItem;
import com.loja.services.OrderService.CheckoutResult;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

public class Server {
    private static final Gson GSON = new Gson();
    private static final GameService GAME_SERVICE = new GameService();
    private static final AuthService AUTH_SERVICE = new AuthService();
    private static final OrderService ORDER_SERVICE = new OrderService();

    public static void main(String[] args) throws IOException {
        Database.ensureSeededGames();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/games", withCors(Server::handleGames));
        server.createContext("/auth/register", withCors(Server::handleRegister));
        server.createContext("/auth/login", withCors(Server::handleLogin));
        server.createContext("/auth/me", withCors(Server::handleMe));
        server.createContext("/auth/logout", withCors(Server::handleLogout));
        server.createContext("/cart/checkout", withCors(Server::handleCheckout));
        server.createContext("/health", withCors(Server::handleHealth));

        server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        server.start();
        System.out.println("Servidor rodando na porta 8000...");
    }

    private static HttpHandler withCors(HttpHandler handler) {
        return exchange -> {
            addCors(exchange.getResponseHeaders());
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                handler.handle(exchange);
            } catch (IllegalArgumentException ex) {
                sendJson(exchange, 400, Map.of("error", ex.getMessage()));
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, Map.of("error", "Erro interno"));
            }
        };
    }

    private static void addCors(Headers headers) {
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void handleGames(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Método não permitido"));
            return;
        }
        List<Game> games = GAME_SERVICE.listarGames();
        sendJson(exchange, 200, games);
    }

    private static void handleRegister(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Método não permitido"));
            return;
        }
        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null) {
            sendJson(exchange, 400, Map.of("error", "Corpo inválido"));
            return;
        }
        String email = body.has("email") ? body.get("email").getAsString() : null;
        String password = body.has("password") ? body.get("password").getAsString() : null;

        Optional<User> created = AUTH_SERVICE.register(email, password);
        if (created.isEmpty()) {
            sendJson(exchange, 400, Map.of("error", "Não foi possível registrar. Verifique se o e-mail já está em uso ou se a senha é válida."));
            return;
        }

        sendJson(exchange, 201, Map.of("message", "Cadastro realizado com sucesso"));
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Método não permitido"));
            return;
        }
        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null) {
            sendJson(exchange, 400, Map.of("error", "Corpo inválido"));
            return;
        }
        String email = body.has("email") ? body.get("email").getAsString() : null;
        String password = body.has("password") ? body.get("password").getAsString() : null;

        Optional<User> user = AUTH_SERVICE.authenticate(email, password);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Credenciais inválidas"));
            return;
        }
        String token = AUTH_SERVICE.createSession(user.get().getId());
        sendJson(exchange, 200, Map.of(
            "token", token,
            "email", user.get().getEmail()
        ));
    }

    private static void handleMe(HttpExchange exchange) throws IOException {
        Optional<User> user = authenticateRequest(exchange);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Não autorizado"));
            return;
        }
        sendJson(exchange, 200, Map.of(
            "email", user.get().getEmail()
        ));
    }

    private static void handleLogout(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Método não permitido"));
            return;
        }
        Optional<String> token = extractToken(exchange);
        token.ifPresent(AUTH_SERVICE::invalidateToken);
        sendJson(exchange, 200, Map.of("message", "Logout efetuado"));
    }

    private static void handleCheckout(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Método não permitido"));
            return;
        }
        Optional<User> user = authenticateRequest(exchange);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Não autorizado"));
            return;
        }

        CheckoutPayload payload = parseBody(exchange, CheckoutPayload.class);
        if (payload == null) {
            sendJson(exchange, 400, Map.of("error", "Corpo inválido"));
            return;
        }
        List<CheckoutItemPayload> itemsPayload = payload.items();
        if (itemsPayload == null || itemsPayload.isEmpty()) {
            sendJson(exchange, 400, Map.of("error", "Carrinho vazio"));
            return;
        }

        List<CheckoutItem> items = itemsPayload.stream()
            .map(i -> new CheckoutItem(i.gameId(), Math.max(1, i.quantity())))
            .toList();

        CheckoutResult result = ORDER_SERVICE.checkout(user.get().getId(), items);

        sendJson(exchange, 200, Map.of(
            "message", "Pedido registrado com sucesso",
            "orderId", result.orderId(),
            "total", result.total()
        ));
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, Map.of("status", "ok"));
    }

    private static Optional<User> authenticateRequest(HttpExchange exchange) {
        return extractToken(exchange).flatMap(AUTH_SERVICE::findUserByToken);
    }

    private static Optional<String> extractToken(HttpExchange exchange) {
        Headers headers = exchange.getRequestHeaders();
        String auth = headers.getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return Optional.of(auth.substring("Bearer ".length()).trim());
        }
        return Optional.empty();
    }

    private static <T> T parseBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            try {
                return GSON.fromJson(reader, clazz);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private record CheckoutPayload(List<CheckoutItemPayload> items) {}

    private record CheckoutItemPayload(int gameId, int quantity) {}
}
