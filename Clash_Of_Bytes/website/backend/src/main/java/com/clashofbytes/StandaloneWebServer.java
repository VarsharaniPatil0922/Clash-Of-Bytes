package com.clashofbytes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Clash of Bytes: High-Performance Standalone REST API Web Server
 * Built with standard Java SE 27 HttpServer & JDBC.
 * Provides identical REST API contracts as Spring Boot with zero external container dependencies.
 */
public class StandaloneWebServer {

    private static final int PORT = 8080;
    private static final String SQLITE_URL = "jdbc:sqlite:clash_of_bytes.db";
    private static final String STATIC_DIR = "website/game-client";

    public static void main(String[] args) throws IOException {
        int port = PORT;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // REST API Endpoints
        server.createContext("/api/auth/register", new AuthRegisterHandler());
        server.createContext("/api/auth/login", new AuthLoginHandler());
        server.createContext("/api/auth/me", new AuthMeHandler());
        server.createContext("/api/characters", new CharactersHandler());
        server.createContext("/api/matches", new MatchesHandler());
        server.createContext("/api/players/", new PlayersHandler());
        server.createContext("/api/leaderboard", new LeaderboardHandler());
        server.createContext("/", new StaticFileHandler());

        System.out.println("=======================================================================");
        System.out.println("  CLASH OF BYTES: FULL-STACK REST API & WEB SERVER");
        System.out.println("=======================================================================");
        System.out.println(" [*] Server listening at: http://localhost:" + port);
        System.out.println(" [*] Static Web Root:     " + STATIC_DIR);
        System.out.println(" [*] Database Connection: " + SQLITE_URL);
        System.out.println(" [*] Press Ctrl+C to terminate server.");
        System.out.println("=======================================================================");

        server.start();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(SQLITE_URL);
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        addCorsHeaders(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        String clean = json.trim();
        if (clean.startsWith("{")) clean = clean.substring(1);
        if (clean.endsWith("}")) clean = clean.substring(0, clean.length() - 1);

        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replace("\"", "");
                String v = kv[1].trim().replace("\"", "");
                map.put(k, v);
            }
        }
        return map;
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // --- HANDLERS ---

    static class AuthRegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = parseSimpleJson(body);
            String username = data.get("username");
            String email = data.get("email");
            String password = data.get("password");

            if (username == null || email == null || password == null || password.length() < 6) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid registration data\"}");
                return;
            }

            try (Connection conn = getConnection()) {
                String checkSql = "SELECT user_id FROM users WHERE username = ? OR email = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, username);
                    ps.setString(2, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Username or email already exists\"}");
                            return;
                        }
                    }
                }

                String salt = generateSalt();
                String hash = hashPassword(password, salt);
                String insertSql = "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)";
                long newId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, email);
                    ps.setString(3, hash);
                    ps.setString(4, salt);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) newId = rs.getLong(1);
                    }
                }

                String token = "cob_token_" + newId + "_" + UUID.randomUUID().toString().replace("-", "");
                String res = String.format("{\"success\":true,\"token\":\"%s\",\"user\":{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\"}}",
                        token, newId, username, email);
                sendJsonResponse(exchange, 200, res);

            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class AuthLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = parseSimpleJson(body);
            String username = data.get("username");
            String password = data.get("password");

            try (Connection conn = getConnection()) {
                String sql = "SELECT user_id, username, email, password_hash, salt FROM users WHERE username = ? OR email = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, username);
                    ps.setString(2, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            long userId = rs.getLong("user_id");
                            String uname = rs.getString("username");
                            String email = rs.getString("email");
                            String storedHash = rs.getString("password_hash");
                            String salt = rs.getString("salt");

                            String computedHash = hashPassword(password, salt);
                            if (computedHash.equalsIgnoreCase(storedHash)) {
                                String token = "cob_token_" + userId + "_" + UUID.randomUUID().toString().replace("-", "");
                                String res = String.format("{\"success\":true,\"token\":\"%s\",\"user\":{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\"}}",
                                        token, userId, uname, email);
                                sendJsonResponse(exchange, 200, res);
                                return;
                            }
                        }
                    }
                }

                sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid username or password\"}");
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class AuthMeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            long userId = 1;
            if (query != null && query.contains("userId=")) {
                try {
                    userId = Long.parseLong(query.split("userId=")[1].split("&")[0]);
                } catch (Exception ignored) {}
            }

            try (Connection conn = getConnection()) {
                String sql = "SELECT user_id, username, email FROM users WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String res = String.format("{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\"}",
                                    rs.getLong("user_id"), rs.getString("username"), rs.getString("email"));
                            sendJsonResponse(exchange, 200, res);
                            return;
                        }
                    }
                }
                sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"User not found\"}");
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class CharactersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "SELECT character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed FROM characters ORDER BY character_id ASC";
                List<String> list = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        String item = String.format(Locale.US,
                                "{\"characterId\":%d,\"characterName\":\"%s\",\"characterDescription\":\"%s\",\"archetype\":\"%s\",\"baseHp\":%.1f,\"baseAttack\":%.1f,\"baseDefense\":%.1f,\"speed\":%.1f}",
                                rs.getLong("character_id"),
                                rs.getString("character_name"),
                                rs.getString("character_description").replace("\"", "\\\""),
                                rs.getString("archetype"),
                                rs.getDouble("base_hp"),
                                rs.getDouble("base_attack"),
                                rs.getDouble("base_defense"),
                                rs.getDouble("speed"));
                        list.add(item);
                    }
                }
                sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class MatchesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseSimpleJson(body);

                long p1Id = Long.parseLong(data.getOrDefault("player1UserId", "1"));
                long p2Id = Long.parseLong(data.getOrDefault("player2UserId", "2"));
                int p1Score = Integer.parseInt(data.getOrDefault("player1FinalScore", "200"));
                int p2Score = Integer.parseInt(data.getOrDefault("player2FinalScore", "180"));
                int winIdx = Integer.parseInt(data.getOrDefault("winnerPlayerIndex", "1"));
                long winnerId = (winIdx == 1) ? p1Id : p2Id;
                long p1CharId = Long.parseLong(data.getOrDefault("player1CharacterId", "4"));
                long p2CharId = Long.parseLong(data.getOrDefault("player2CharacterId", "3"));

                try (Connection conn = getConnection()) {
                    conn.setAutoCommit(false);
                    long matchId = 0;
                    String matchSql = "INSERT INTO matches (player1_user_id, player2_user_id, winner_user_id, total_rounds, match_status, player1_final_score, player2_final_score) VALUES (?, ?, ?, 3, 'COMPLETED', ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(matchSql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setLong(1, p1Id);
                        ps.setLong(2, p2Id);
                        ps.setLong(3, winnerId);
                        ps.setInt(4, p1Score);
                        ps.setInt(5, p2Score);
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) matchId = rs.getLong(1);
                        }
                    }

                    String mpSql = "INSERT INTO match_players (match_id, user_id, character_id, final_score, result) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(mpSql)) {
                        ps.setLong(1, matchId);
                        ps.setLong(2, p1Id);
                        ps.setLong(3, p1CharId);
                        ps.setInt(4, p1Score);
                        ps.setString(5, winIdx == 1 ? "WIN" : "LOSS");
                        ps.executeUpdate();

                        ps.setLong(1, matchId);
                        ps.setLong(2, p2Id);
                        ps.setLong(3, p2CharId);
                        ps.setInt(4, p2Score);
                        ps.setString(5, winIdx == 2 ? "WIN" : "LOSS");
                        ps.executeUpdate();
                    }

                    conn.commit();
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"matchId\":" + matchId + "}");
                } catch (SQLException e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
                }
            } else {
                // GET matches
                try (Connection conn = getConnection()) {
                    String sql = "SELECT m.match_id, m.match_date, m.player1_final_score, m.player2_final_score, " +
                                 "u1.username as p1_name, u2.username as p2_name, win.username as winner_name, " +
                                 "c1.character_name as c1_name, c2.character_name as c2_name " +
                                 "FROM matches m " +
                                 "JOIN users u1 ON m.player1_user_id = u1.user_id " +
                                 "JOIN users u2 ON m.player2_user_id = u2.user_id " +
                                 "LEFT JOIN users win ON m.winner_user_id = win.user_id " +
                                 "LEFT JOIN match_players mp1 ON m.match_id = mp1.match_id AND m.player1_user_id = mp1.user_id " +
                                 "LEFT JOIN characters c1 ON mp1.character_id = c1.character_id " +
                                 "LEFT JOIN match_players mp2 ON m.match_id = mp2.match_id AND m.player2_user_id = mp2.user_id " +
                                 "LEFT JOIN characters c2 ON mp2.character_id = c2.character_id " +
                                 "ORDER BY m.match_date DESC LIMIT 50";

                    List<String> list = new ArrayList<>();
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            String item = String.format(
                                    "{\"matchId\":%d,\"matchDate\":\"%s\",\"player1Username\":\"%s\",\"player2Username\":\"%s\",\"player1Character\":\"%s\",\"player2Character\":\"%s\",\"player1Score\":%d,\"player2Score\":%d,\"winnerUsername\":\"%s\",\"result\":\"%s\",\"totalRounds\":3}",
                                    rs.getLong("match_id"),
                                    rs.getString("match_date"),
                                    rs.getString("p1_name"),
                                    rs.getString("p2_name"),
                                    rs.getString("c1_name") != null ? rs.getString("c1_name") : "Cyber Brawler",
                                    rs.getString("c2_name") != null ? rs.getString("c2_name") : "Shadow Ninja",
                                    rs.getInt("player1_final_score"),
                                    rs.getInt("player2_final_score"),
                                    rs.getString("winner_name") != null ? rs.getString("winner_name") : "DRAW",
                                    "WIN");
                            list.add(item);
                        }
                    }
                    sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
                } catch (SQLException e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
                }
            }
        }
    }

    static class PlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            // Expected /api/players/{id}/stats
            String[] parts = path.split("/");
            long userId = 1;
            if (parts.length >= 4) {
                try { userId = Long.parseLong(parts[3]); } catch (Exception ignored) {}
            }

            try (Connection conn = getConnection()) {
                String uname = "Player " + userId;
                String userSql = "SELECT username FROM users WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                    ps.setLong(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) uname = rs.getString("username");
                    }
                }

                String statsSql = "SELECT COUNT(*) as total_matches, " +
                        "SUM(CASE WHEN result = 'WIN' THEN 1 ELSE 0 END) as wins, " +
                        "SUM(CASE WHEN result = 'LOSS' THEN 1 ELSE 0 END) as losses, " +
                        "SUM(final_score) as total_score " +
                        "FROM match_players WHERE user_id = ?";

                int matches = 0, wins = 0, losses = 0, totalScore = 0;
                try (PreparedStatement ps = conn.prepareStatement(statsSql)) {
                    ps.setLong(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            matches = rs.getInt("total_matches");
                            wins = rs.getInt("wins");
                            losses = rs.getInt("losses");
                            totalScore = rs.getInt("total_score");
                        }
                    }
                }

                double winRate = (matches > 0) ? Math.round(((double) wins / matches) * 10000.0) / 100.0 : 0.0;
                String res = String.format(Locale.US,
                        "{\"userId\":%d,\"username\":\"%s\",\"totalMatches\":%d,\"wins\":%d,\"losses\":%d,\"winRate\":%.2f,\"totalScore\":%d,\"favoriteCharacter\":\"Cyber Brawler\"}",
                        userId, uname, matches, wins, losses, winRate, totalScore);
                sendJsonResponse(exchange, 200, res);

            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class LeaderboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "SELECT u.user_id, u.username, " +
                        "COUNT(mp.match_player_id) as total_matches, " +
                        "SUM(CASE WHEN mp.result = 'WIN' THEN 1 ELSE 0 END) as wins, " +
                        "SUM(CASE WHEN mp.result = 'LOSS' THEN 1 ELSE 0 END) as losses, " +
                        "COALESCE(SUM(mp.final_score), 0) as total_score " +
                        "FROM users u " +
                        "LEFT JOIN match_players mp ON u.user_id = mp.user_id " +
                        "GROUP BY u.user_id, u.username " +
                        "ORDER BY wins DESC, total_score DESC LIMIT 25";

                List<String> list = new ArrayList<>();
                int rank = 1;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        int matches = rs.getInt("total_matches");
                        int wins = rs.getInt("wins");
                        int losses = rs.getInt("losses");
                        int score = rs.getInt("total_score");
                        double winRate = (matches > 0) ? Math.round(((double) wins / matches) * 10000.0) / 100.0 : 0.0;

                        String item = String.format(Locale.US,
                                "{\"rank\":%d,\"userId\":%d,\"username\":\"%s\",\"matches\":%d,\"wins\":%d,\"losses\":%d,\"winRate\":%.2f,\"totalScore\":%d,\"favoriteCharacter\":\"Cyber Brawler\"}",
                                rank++, rs.getLong("user_id"), rs.getString("username"), matches, wins, losses, winRate, score);
                        list.add(item);
                    }
                }
                sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File(STATIC_DIR, path.startsWith("/") ? path.substring(1) : path);
            if (!file.exists() || file.isDirectory()) {
                file = new File(STATIC_DIR, "index.html");
            }

            if (!file.exists()) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) { os.write(notFound.getBytes()); }
                return;
            }

            String mime = "text/plain";
            if (file.getName().endsWith(".html")) mime = "text/html; charset=UTF-8";
            else if (file.getName().endsWith(".js")) mime = "application/javascript; charset=UTF-8";
            else if (file.getName().endsWith(".css")) mime = "text/css; charset=UTF-8";
            else if (file.getName().endsWith(".png")) mime = "image/png";
            else if (file.getName().endsWith(".json")) mime = "application/json; charset=UTF-8";

            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.sendResponseHeaders(200, file.length());
            try (InputStream is = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) {
                    os.write(buf, 0, n);
                }
            }
        }
    }
}
