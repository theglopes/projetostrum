package com.loja.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loja.db.Database;
import com.loja.models.Game;
import com.loja.models.GameSubmission;
import com.loja.models.DeveloperRequest;
import com.loja.models.User;
import com.loja.services.AuthService;
import com.loja.services.GameService;
import com.loja.services.GameSubmissionService;
import com.loja.services.DeveloperRequestService;
import com.loja.services.OrderService;
import com.loja.services.OrderService.CheckoutItem;
import com.loja.services.OrderService.CheckoutResult;
import com.loja.services.UserService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {
    private static final Gson GSON = new Gson();
    private static final GameService GAME_SERVICE = new GameService();
    private static final AuthService AUTH_SERVICE = new AuthService();
    private static final OrderService ORDER_SERVICE = new OrderService();
    private static final UserService USER_SERVICE = new UserService();
    private static final DeveloperRequestService DEVELOPER_REQUEST_SERVICE = new DeveloperRequestService();
    private static final GameSubmissionService GAME_SUBMISSION_SERVICE = new GameSubmissionService();

    public static void main(String[] args) throws IOException {
        Database.ensureSeededGames();
        AUTH_SERVICE.ensureDefaultAdmin();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/games", withCors(Server::handleGames));
        server.createContext("/auth/register", withCors(Server::handleRegister));
        server.createContext("/auth/login", withCors(Server::handleLogin));
        server.createContext("/auth/me", withCors(Server::handleMe));
        server.createContext("/auth/logout", withCors(Server::handleLogout));
        server.createContext("/users/library", withCors(Server::handleLibrary));
        server.createContext("/cart/checkout", withCors(Server::handleCheckout));
        server.createContext("/billing/subscribe", withCors(Server::handleSubscribe));
        server.createContext("/billing/cancel", withCors(Server::handleCancelSubscription));
        server.createContext("/admin/users", withCors(Server::handleAdminUsers));
        server.createContext("/admin/users/role", withCors(Server::handleAdminUpdateRole));
        server.createContext("/admin/users/plan", withCors(Server::handleAdminUpdatePlan));
        server.createContext("/admin/games", withCors(Server::handleAdminGames));
        server.createContext("/admin/overview", withCors(Server::handleAdminOverview));
        server.createContext("/developer/overview", withCors(Server::handleDeveloperOverview));
        server.createContext("/developer/requests", withCors(Server::handleDeveloperRequests));
        server.createContext("/developer/games", withCors(Server::handleDeveloperGames));
        server.createContext("/admin/developer/requests", withCors(Server::handleAdminDeveloperRequests));
        server.createContext("/admin/developer/requests/status", withCors(Server::handleAdminDeveloperRequestStatus));
        server.createContext("/admin/game-submissions", withCors(Server::handleAdminGameSubmissions));
        server.createContext("/admin/game-submissions/status", withCors(Server::handleAdminGameSubmissionStatus));
        server.createContext("/moderator/overview", withCors(Server::handleModeratorOverview));
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
        headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void handleGames(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        List<Game> games = GAME_SERVICE.listarGames();
        sendJson(exchange, 200, games);
    }

    private static void handleRegister(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null) {
            sendJson(exchange, 400, Map.of("error", "Corpo invalido"));
            return;
        }
        String email = body.has("email") ? body.get("email").getAsString() : null;
        String password = body.has("password") ? body.get("password").getAsString() : null;

        Optional<User> created = AUTH_SERVICE.register(email, password);
        if (created.isEmpty()) {
            sendJson(exchange, 400, Map.of("error", "Nao foi possivel registrar. Verifique credenciais."));
            return;
        }
        sendJson(exchange, 201, Map.of("message", "Cadastro realizado com sucesso"));
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null) {
            sendJson(exchange, 400, Map.of("error", "Corpo invalido"));
            return;
        }
        String email = body.has("email") ? body.get("email").getAsString() : null;
        String password = body.has("password") ? body.get("password").getAsString() : null;

        Optional<User> user = AUTH_SERVICE.authenticate(email, password);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Credenciais invalidas"));
            return;
        }

        String token = AUTH_SERVICE.createSession(user.get().getId());
        sendJson(exchange, 200, Map.of(
            "token", token,
            "email", user.get().getEmail(),
            "role", user.get().getRole(),
            "plan", user.get().getPlan()
        ));
    }

    private static void handleMe(HttpExchange exchange) throws IOException {
        Optional<User> user = authenticateRequest(exchange);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        sendJson(exchange, 200, Map.of(
            "email", user.get().getEmail(),
            "createdAt", user.get().getCreatedAt(),
            "role", user.get().getRole(),
            "plan", user.get().getPlan(),
            "premium", user.get().hasPremium()
        ));
    }

    private static void handleLogout(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<String> token = extractToken(exchange);
        token.ifPresent(AUTH_SERVICE::invalidateToken);
        sendJson(exchange, 200, Map.of("message", "Logout efetuado"));
    }

    private static void handleLibrary(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> user = authenticateRequest(exchange);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        List<Game> library = ORDER_SERVICE.listPurchasedGames(user.get().getId());
        sendJson(exchange, 200, Map.of("games", library));
    }

    private static void handleCheckout(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> user = authenticateRequest(exchange);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }

        CheckoutPayload payload = parseBody(exchange, CheckoutPayload.class);
        if (payload == null) {
            sendJson(exchange, 400, Map.of("error", "Corpo invalido"));
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
        List<Game> library = ORDER_SERVICE.listPurchasedGames(user.get().getId());

        sendJson(exchange, 200, Map.of(
            "message", "Pedido registrado com sucesso",
            "orderId", result.orderId(),
            "total", result.total(),
            "games", result.games(),
            "library", library
        ));
    }

    private static void handleSubscribe(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> user = authenticateRequest(exchange);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }

        JsonObject body = parseBody(exchange, JsonObject.class);
        String desiredPlan = body != null && body.has("plan") ? body.get("plan").getAsString() : "PREMIUM";

        Optional<User> updated = USER_SERVICE.updatePlan(user.get().getId(), desiredPlan);
        if (updated.isEmpty()) {
            sendJson(exchange, 400, Map.of("error", "Plano informado invalido"));
            return;
        }

        sendJson(exchange, 200, Map.of(
            "message", "Assinatura atualizada",
            "user", userPayload(updated.get())
        ));
    }

    private static void handleCancelSubscription(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> user = authenticateRequest(exchange);
        if (user.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }

        Optional<User> updated = USER_SERVICE.updatePlan(user.get().getId(), "FREE");
        if (updated.isEmpty()) {
            sendJson(exchange, 500, Map.of("error", "Falha ao cancelar plano"));
            return;
        }

        sendJson(exchange, 200, Map.of(
            "message", "Assinatura cancelada",
            "user", userPayload(updated.get())
        ));
    }

    private static void handleAdminUsers(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }

        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!requester.get().isAdmin()) {
            sendJson(exchange, 403, Map.of("error", "Acesso exclusivo para administradores"));
            return;
        }

        List<Map<String, Object>> users = USER_SERVICE.listUsers().stream()
            .map(Server::userPayload)
            .toList();
        sendJson(exchange, 200, Map.of("users", users));
    }

    private static void handleAdminUpdateRole(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!requester.get().isAdmin()) {
            sendJson(exchange, 403, Map.of("error", "Acesso exclusivo para administradores"));
            return;
        }

        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null || !body.has("userId") || !body.has("role")) {
            sendJson(exchange, 400, Map.of("error", "Parametros obrigatorios ausentes"));
            return;
        }

        int userId = body.get("userId").getAsInt();
        String role = body.get("role").getAsString();

        Optional<User> target = USER_SERVICE.findById(userId);
        if (target.isEmpty()) {
            sendJson(exchange, 404, Map.of("error", "Usuario nao encontrado"));
            return;
        }

        Optional<String> normalizedRole = USER_SERVICE.normalizeRoleValue(role);
        if (normalizedRole.isEmpty()) {
            sendJson(exchange, 400, Map.of("error", "Papel informado invalido"));
            return;
        }

        try {
            Optional<User> updated = USER_SERVICE.updateRole(userId, normalizedRole.get());
            if (updated.isEmpty()) {
                sendJson(exchange, 400, Map.of("error", "Falha ao atualizar papel"));
                return;
            }

            sendJson(exchange, 200, Map.of(
                "message", "Papel atualizado",
                "user", userPayload(updated.get())
            ));
        } catch (IllegalArgumentException ex) {
            sendJson(exchange, 400, Map.of("error", ex.getMessage()));
        }
    }

    private static void handleAdminUpdatePlan(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!requester.get().isAdmin()) {
            sendJson(exchange, 403, Map.of("error", "Acesso exclusivo para administradores"));
            return;
        }

        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null || !body.has("userId") || !body.has("plan")) {
            sendJson(exchange, 400, Map.of("error", "Parametros obrigatorios ausentes"));
            return;
        }

        int userId = body.get("userId").getAsInt();
        String plan = body.get("plan").getAsString();

        Optional<User> updated = USER_SERVICE.updatePlan(userId, plan);
        if (updated.isEmpty()) {
            sendJson(exchange, 400, Map.of("error", "Falha ao atualizar plano"));
            return;
        }

        sendJson(exchange, 200, Map.of(
            "message", "Plano atualizado",
            "user", userPayload(updated.get())
        ));
    }

    private static void handleAdminGames(HttpExchange exchange) throws IOException {
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        if ("GET".equals(method)) {
            if (!hasAnyRole(requester.get(), "ADMIN", "MODERATOR", "DEVELOPER")) {
                sendJson(exchange, 403, Map.of("error", "Acesso restrito"));
                return;
            }
            List<Game> games = GAME_SERVICE.listarGames();
            sendJson(exchange, 200, Map.of("games", games));
            return;
        }

        if ("DELETE".equals(method)) {
            if (!requester.get().isAdmin()) {
                sendJson(exchange, 403, Map.of("error", "Apenas administradores podem remover jogos"));
                return;
            }
            Map<String, String> params = queryParams(exchange);
            int gameId = parseInt(params.get("id"));
            if (gameId <= 0) {
                JsonObject body = parseBody(exchange, JsonObject.class);
                if (body != null && body.has("id")) {
                    gameId = parseInt(body.get("id").getAsString());
                }
            }
            if (gameId <= 0) {
                sendJson(exchange, 400, Map.of("error", "Identificador do jogo obrigatorio"));
                return;
            }
            boolean removed = GAME_SERVICE.removerGame(gameId);
            if (!removed) {
                sendJson(exchange, 404, Map.of("error", "Jogo nao encontrado"));
                return;
            }
            sendJson(exchange, 200, Map.of("message", "Jogo removido com sucesso", "gameId", gameId));
            return;
        }

        if (!"POST".equals(method)) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }

        if (!hasAnyRole(requester.get(), "ADMIN", "DEVELOPER")) {
            sendJson(exchange, 403, Map.of("error", "Apenas administradores ou desenvolvedores podem adicionar jogos"));
            return;
        }

        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null) {
            sendJson(exchange, 400, Map.of("error", "Corpo invalido"));
            return;
        }

        String name = body.has("name") ? body.get("name").getAsString().trim() : "";
        double price = body.has("price") ? body.get("price").getAsDouble() : -1;
        String image = body.has("image") ? body.get("image").getAsString().trim() : null;
        boolean promo = body.has("promo") && body.get("promo").getAsBoolean();

        if (name.isEmpty() || price < 0) {
            sendJson(exchange, 400, Map.of("error", "Dados do jogo invalidos"));
            return;
        }

        Game created = new Game(0, name, price, "PC", promo, image);
        int gameId = GAME_SERVICE.adicionarGame(created);
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Jogo criado com sucesso");
        payload.put("gameId", gameId);
        payload.put("game", created);
        sendJson(exchange, 201, payload);
    }

    private static void handleAdminOverview(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }

        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!requester.get().isAdmin()) {
            sendJson(exchange, 403, Map.of("error", "Acesso exclusivo para administradores"));
            return;
        }

        List<User> users = USER_SERVICE.listUsers();
        Map<String, Long> roles = users.stream()
            .collect(Collectors.groupingBy(u -> u.getRole().toUpperCase(), Collectors.counting()));
        long premiumUsers = users.stream().filter(User::hasPremium).count();
        int games = GAME_SERVICE.listarGames().size();
        double estimatedRevenue = premiumUsers * 29.90;

        sendJson(exchange, 200, Map.of(
            "totalUsers", users.size(),
            "premiumUsers", premiumUsers,
            "roles", roles,
            "totalGames", games,
            "estimatedRevenue", estimatedRevenue
        ));
    }

    private static void handleDeveloperOverview(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!hasAnyRole(requester.get(), "DEVELOPER")) {
            sendJson(exchange, 403, Map.of("error", "Area exclusiva para desenvolvedores"));
            return;
        }

        List<Game> games = GAME_SERVICE.listarGames();
        List<String> highlights = games.stream()
            .limit(5)
            .map(Game::getNome)
            .toList();
        long premiumUsers = USER_SERVICE.listUsers().stream().filter(User::hasPremium).count();

        sendJson(exchange, 200, Map.of(
            "totalGames", games.size(),
            "premiumPlayers", premiumUsers,
            "highlights", highlights
        ));
    }

    private static void handleDeveloperRequests(HttpExchange exchange) throws IOException {
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        String method = exchange.getRequestMethod().toUpperCase();
        if ("GET".equals(method)) {
            Optional<DeveloperRequest> request = DEVELOPER_REQUEST_SERVICE.findByUserId(requester.get().getId());
            Map<String, Object> payload = new HashMap<>();
            payload.put("status", request.map(DeveloperRequest::getStatus).orElse("NONE"));
            request.ifPresent(r -> payload.put("request", r));
            sendJson(exchange, 200, payload);
            return;
        }
        if ("POST".equals(method)) {
            if (hasAnyRole(requester.get(), "DEVELOPER", "ADMIN")) {
                sendJson(exchange, 400, Map.of("error", "Voce ja possui acesso de desenvolvedor."));
                return;
            }
            JsonObject body = parseBody(exchange, JsonObject.class);
            if (body == null) {
                sendJson(exchange, 400, Map.of("error", "Corpo invalido"));
                return;
            }
            String studioName = body.has("studioName") ? body.get("studioName").getAsString() : null;
            String cnpj = body.has("cnpj") ? body.get("cnpj").getAsString() : null;
            DeveloperRequest request = DEVELOPER_REQUEST_SERVICE.createOrReset(requester.get().getId(), studioName, cnpj);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Solicitacao registrada.");
            response.put("request", request);
            sendJson(exchange, 201, response);
            return;
        }
        sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
    }

    private static void handleDeveloperGames(HttpExchange exchange) throws IOException {
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        if ("GET".equals(method)) {
            if (!hasAnyRole(requester.get(), "DEVELOPER")) {
                sendJson(exchange, 403, Map.of("error", "Area exclusiva para desenvolvedores"));
                return;
            }
            List<GameSubmission> submissions = GAME_SUBMISSION_SERVICE.listByUser(requester.get().getId());
            sendJson(exchange, 200, Map.of("submissions", submissions));
            return;
        }

        if ("POST".equals(method)) {
            if (!hasAnyRole(requester.get(), "DEVELOPER")) {
                sendJson(exchange, 403, Map.of("error", "Area exclusiva para desenvolvedores"));
                return;
            }
            JsonObject body = parseBody(exchange, JsonObject.class);
            if (body == null || !body.has("name") || !body.has("price")) {
                sendJson(exchange, 400, Map.of("error", "Dados do jogo sao obrigatorios"));
                return;
            }
            String name = body.get("name").getAsString();
            double price = body.get("price").getAsDouble();
            String image = body.has("image") ? body.get("image").getAsString() : null;
            boolean promo = body.has("promo") && body.get("promo").getAsBoolean();
            try {
                GameSubmission submission = GAME_SUBMISSION_SERVICE.submit(requester.get().getId(), name, price, image, promo);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Jogo enviado para revisao.");
                response.put("submission", submission);
                sendJson(exchange, 201, response);
            } catch (IllegalArgumentException ex) {
                sendJson(exchange, 400, Map.of("error", ex.getMessage()));
            }
            return;
        }

        sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
    }

    private static void handleAdminDeveloperRequests(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!hasAnyRole(requester.get(), "ADMIN", "MODERATOR")) {
            sendJson(exchange, 403, Map.of("error", "Acesso restrito"));
            return;
        }
        List<DeveloperRequest> requests = DEVELOPER_REQUEST_SERVICE.listAll();
        sendJson(exchange, 200, Map.of("requests", requests));
    }

    private static void handleAdminDeveloperRequestStatus(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!hasAnyRole(requester.get(), "ADMIN", "MODERATOR")) {
            sendJson(exchange, 403, Map.of("error", "Acesso restrito"));
            return;
        }
        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null || !body.has("requestId") || !body.has("status")) {
            sendJson(exchange, 400, Map.of("error", "Parametros obrigatorios ausentes"));
            return;
        }
        int requestId = body.get("requestId").getAsInt();
        String status = body.get("status").getAsString();
        Optional<DeveloperRequest> updated = DEVELOPER_REQUEST_SERVICE.updateStatus(requestId, status, requester.get().getId());
        if (updated.isEmpty()) {
            sendJson(exchange, 404, Map.of("error", "Solicitacao nao encontrada"));
            return;
        }
        DeveloperRequest request = updated.get();
        if ("APPROVED".equalsIgnoreCase(request.getStatus())) {
            USER_SERVICE.updateRole(request.getUserId(), "DEVELOPER");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Solicitacao atualizada.");
        response.put("request", request);
        sendJson(exchange, 200, response);
    }

    private static void handleAdminGameSubmissions(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!hasAnyRole(requester.get(), "ADMIN", "MODERATOR")) {
            sendJson(exchange, 403, Map.of("error", "Acesso restrito"));
            return;
        }
        List<GameSubmission> submissions = GAME_SUBMISSION_SERVICE.listAll();
        sendJson(exchange, 200, Map.of("submissions", submissions));
    }

    private static void handleAdminGameSubmissionStatus(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!hasAnyRole(requester.get(), "ADMIN", "MODERATOR")) {
            sendJson(exchange, 403, Map.of("error", "Acesso restrito"));
            return;
        }
        JsonObject body = parseBody(exchange, JsonObject.class);
        if (body == null || !body.has("submissionId") || !body.has("status")) {
            sendJson(exchange, 400, Map.of("error", "Parametros obrigatorios ausentes"));
            return;
        }
        int submissionId = body.get("submissionId").getAsInt();
        String status = body.get("status").getAsString();
        try {
            Optional<GameSubmission> updated = GAME_SUBMISSION_SERVICE.updateStatus(submissionId, status, requester.get().getId());
            if (updated.isEmpty()) {
                sendJson(exchange, 404, Map.of("error", "Submissao nao encontrada"));
                return;
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Submissao atualizada.");
            response.put("submission", updated.get());
            sendJson(exchange, 200, response);
        } catch (IllegalArgumentException ex) {
            sendJson(exchange, 400, Map.of("error", ex.getMessage()));
        }
    }

    private static void handleModeratorOverview(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Metodo nao permitido"));
            return;
        }
        Optional<User> requester = authenticateRequest(exchange);
        if (requester.isEmpty()) {
            sendJson(exchange, 401, Map.of("error", "Nao autorizado"));
            return;
        }
        if (!hasAnyRole(requester.get(), "MODERATOR")) {
            sendJson(exchange, 403, Map.of("error", "Area exclusiva para moderadores"));
            return;
        }

        long moderators = USER_SERVICE.listUsers().stream()
            .filter(u -> "MODERATOR".equalsIgnoreCase(u.getRole()))
            .count();

        List<Map<String, Object>> tasks = List.of(
            Map.of("id", 1, "titulo", "Revisar denuncias da comunidade", "status", "PENDENTE"),
            Map.of("id", 2, "titulo", "Validar novos comentarios", "status", "EM_ANDAMENTO"),
            Map.of("id", 3, "titulo", "Preparar destaque da semana", "status", "CONCLUIDO")
        );

        sendJson(exchange, 200, Map.of(
            "pendingReports", Math.max(2, moderators * 2),
            "activeModerators", moderators,
            "tasks", tasks
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

    private static Map<String, String> queryParams(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        if (rawQuery == null || rawQuery.isBlank()) {
            return Collections.emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        for (String pair : rawQuery.split("&")) {
            if (pair == null || pair.isBlank()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = urlDecode(parts[0]);
            String value = parts.length > 1 ? urlDecode(parts[1]) : "";
            params.put(key, value);
        }
        return params;
    }

    private static String urlDecode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    private static int parseInt(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
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

    private static boolean hasAnyRole(User user, String... roles) {
        if (user == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        if (roles == null) {
            return false;
        }
        String current = user.getRole() == null ? "" : user.getRole().toUpperCase();
        for (String role : roles) {
            if (role != null && current.equals(role.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Object> userPayload(User user) {
        return Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "role", user.getRole(),
            "plan", user.getPlan(),
            "createdAt", user.getCreatedAt(),
            "premium", user.hasPremium()
        );
    }

    private record CheckoutPayload(List<CheckoutItemPayload> items) {}

    private record CheckoutItemPayload(int gameId, int quantity) {}
}
