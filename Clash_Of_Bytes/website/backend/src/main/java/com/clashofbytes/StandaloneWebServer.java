package com.clashofbytes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Clash of Bytes: High-Performance Standalone REST API Web Server
 * Built with standard Java SE 27 HttpServer & JDBC.
 * Features Hybrid Dual Database Architecture: Supabase PostgreSQL & Local SQLite Fallback.
 * Includes Cryptographic Supabase JWT Verification (JWKS RS256 & HMAC HS256).
 */
public class StandaloneWebServer {

    private static final int PORT = 8080;
    private static final String DEFAULT_SQLITE_URL = "jdbc:sqlite:clash_of_bytes.db";
    private static final String STATIC_DIR = "website/game-client";

    // Active Database State
    private static String activeDatabaseEngine = "SQLITE_LOCAL";
    private static String activeDbUrl = DEFAULT_SQLITE_URL;
    private static String activeDbUser = "";
    private static String activeDbPassword = "";
    private static boolean isSupabaseConfigured = false;
    private static String dbDiagnosticMessage = "";

    // Supabase Public & Verification Configuration
    private static String supabaseUrl = "";
    private static String supabasePublishableKey = "";
    private static String supabaseJwtSecret = "";

    // Active Player Sessions (Thread-Safe Map: Token -> User ID / UUID)
    private static final Map<String, Object> activeSessions = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = PORT;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }

        // Initialize Database Connection Engine (Supabase or SQLite Fallback)
        initDatabaseConfiguration();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(16));

        // REST API Endpoints
        server.createContext("/api/config", new ConfigHandler());
        server.createContext("/api/system/status", new SystemStatusHandler());
        server.createContext("/api/auth/register", new AuthRegisterHandler());
        server.createContext("/api/auth/login", new AuthLoginHandler());
        server.createContext("/api/auth/me", new AuthMeHandler());
        server.createContext("/api/characters", new CharactersHandler());
        server.createContext("/api/matches", new MatchesHandler());
        server.createContext("/api/players/", new PlayersHandler());
        server.createContext("/api/leaderboard", new LeaderboardHandler());
        server.createContext("/api/cg/practicals", new CGPracticalsHandler());
        server.createContext("/", new StaticFileHandler());

        System.out.println("=======================================================================");
        System.out.println("  CLASH OF BYTES: FULL-STACK REST API & WEB SERVER");
        System.out.println("=======================================================================");
        System.out.println(" [*] Server listening at: http://localhost:" + port);
        System.out.println(" [*] Static Web Root:     " + STATIC_DIR);
        System.out.println(" [*] Active Database:     " + activeDatabaseEngine);
        System.out.println(" [*] Database URL:        " + sanitizeDbUrl(activeDbUrl));
        System.out.println(" [*] Supabase Project:    " + (supabaseUrl.isEmpty() ? "Not Configured" : supabaseUrl));
        System.out.println(" [*] Supabase Auth Key:   " + (supabasePublishableKey.isEmpty() ? "Not Set" : "[PRESENT]"));
        System.out.println(" [*] Diagnostic Info:     " + dbDiagnosticMessage);
        System.out.println(" [*] Press Ctrl+C to terminate server.");
        System.out.println("=======================================================================");

        server.start();
    }

    /**
     * Initializes database & Supabase configuration.
     * Evaluates Supabase environment variables; if unavailable or unreachable,
     * seamlessly preserves and connects to the local SQLite database.
     */
    public static void initDatabaseConfiguration() {
        String sbDbUrl = System.getenv("SUPABASE_DB_URL");
        String sbHost = System.getenv("SUPABASE_DB_HOST");
        if (sbHost == null || sbHost.trim().isEmpty()) {
            sbHost = System.getenv("DB_HOST");
        }

        // Supabase Project REST & Auth URL
        supabaseUrl = System.getenv("SUPABASE_URL");
        if (supabaseUrl == null || supabaseUrl.trim().isEmpty()) {
            supabaseUrl = System.getenv("SUPABASE_PROJECT_URL");
        }
        if ((supabaseUrl == null || supabaseUrl.trim().isEmpty()) && sbHost != null && sbHost.startsWith("db.") && sbHost.endsWith(".supabase.co")) {
            String projectRef = sbHost.substring(3, sbHost.indexOf(".supabase.co"));
            supabaseUrl = "https://" + projectRef + ".supabase.co";
        }
        if (supabaseUrl == null) supabaseUrl = "";

        // Supabase Public Publishable / Anon Key
        supabasePublishableKey = System.getenv("SUPABASE_PUBLISHABLE_KEY");
        if (supabasePublishableKey == null || supabasePublishableKey.trim().isEmpty()) {
            supabasePublishableKey = System.getenv("SUPABASE_ANON_KEY");
        }
        if (supabasePublishableKey == null) supabasePublishableKey = "";

        // Supabase JWT Secret for legacy HS256 verification (optional)
        supabaseJwtSecret = System.getenv("SUPABASE_JWT_SECRET");
        if (supabaseJwtSecret == null) supabaseJwtSecret = "";

        // Fallback: Read client configuration from config.js if env vars are unset
        if (supabaseUrl.isEmpty() || supabasePublishableKey.isEmpty()) {
            loadConfigFromFile();
        }

        if (supabaseUrl != null && !supabaseUrl.isEmpty() && !supabaseUrl.contains("your-project-ref") &&
            supabasePublishableKey != null && !supabasePublishableKey.isEmpty() && !supabasePublishableKey.contains("your_public_publishable")) {
            isSupabaseConfigured = true;
        }

        // Database connection string setup
        if (sbDbUrl != null && !sbDbUrl.trim().isEmpty()) {
            activeDbUrl = sbDbUrl.trim();
            isSupabaseConfigured = true;
        } else if (sbHost != null && !sbHost.trim().isEmpty() && !sbHost.equalsIgnoreCase("localhost") && !sbHost.equalsIgnoreCase("127.0.0.1")) {
            String port = System.getenv("SUPABASE_DB_PORT");
            if (port == null || port.isEmpty()) port = "6543";
            String db = System.getenv("SUPABASE_DB_NAME");
            if (db == null || db.isEmpty()) db = "postgres";
            String ssl = System.getenv("SUPABASE_SSL");
            if (ssl == null || ssl.isEmpty()) ssl = "require";
            activeDbUrl = String.format("jdbc:postgresql://%s:%s/%s?sslmode=%s", sbHost, port, db, ssl);
            activeDbUser = System.getenv("SUPABASE_DB_USER");
            if (activeDbUser == null) activeDbUser = System.getenv("DB_USER");
            activeDbPassword = System.getenv("SUPABASE_DB_PASSWORD");
            if (activeDbPassword == null) activeDbPassword = System.getenv("DB_PASSWORD");
            isSupabaseConfigured = true;
        }

        if (isSupabaseConfigured) {
            try {
                Class.forName("org.postgresql.Driver");
                try (Connection testConn = (activeDbUser != null && !activeDbUser.isEmpty())
                        ? DriverManager.getConnection(activeDbUrl, activeDbUser, activeDbPassword)
                        : DriverManager.getConnection(activeDbUrl)) {
                    activeDatabaseEngine = "SUPABASE_POSTGRES";
                    dbDiagnosticMessage = "Successfully connected to cloud Supabase PostgreSQL instance.";
                    return;
                }
            } catch (Exception e) {
                System.err.println("[Database Warning] Supabase connection attempt failed: " + e.getMessage());
                dbDiagnosticMessage = "Supabase configured but unreachable (" + e.getMessage() + "). Falling back to local SQLite.";
            }
        }

        // Default SQLite local mode
        activeDatabaseEngine = "SQLITE_LOCAL";
        activeDbUrl = resolveSqliteUrl();
        activeDbUser = "";
        activeDbPassword = "";
        if (dbDiagnosticMessage.isEmpty()) {
            dbDiagnosticMessage = "Running in zero-configuration local SQLite mode (clash_of_bytes.db). Supabase ready upon setting SUPABASE_DB_URL.";
        }

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(activeDbUrl);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (Exception ignored) {}
    }

    private static String resolveSqliteUrl() {
        File f1 = new File("clash_of_bytes.db");
        if (f1.exists()) return "jdbc:sqlite:clash_of_bytes.db";
        File f2 = new File("Clash_Of_Bytes/clash_of_bytes.db");
        if (f2.exists()) return "jdbc:sqlite:Clash_Of_Bytes/clash_of_bytes.db";
        return DEFAULT_SQLITE_URL;
    }

    private static void loadConfigFromFile() {
        File[] candidateFiles = new File[] {
            new File(STATIC_DIR, "config.js"),
            new File("Clash_Of_Bytes/" + STATIC_DIR, "config.js"),
            new File("website/game-client/config.js"),
            new File("Clash_Of_Bytes/website/game-client/config.js")
        };
        for (File f : candidateFiles) {
            if (f.exists() && f.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                    String content = sb.toString();

                    Pattern urlPat = Pattern.compile("['\"](https://[^'\"]+\\.supabase\\.co)['\"]");
                    Matcher urlMat = urlPat.matcher(content);
                    if (urlMat.find() && supabaseUrl.isEmpty()) {
                        supabaseUrl = urlMat.group(1).trim();
                    }

                    Pattern keyPat = Pattern.compile("['\"](sb[a-zA-Z0-9_-]+)['\"]");
                    Matcher keyMat = keyPat.matcher(content);
                    while (keyMat.find()) {
                        String matched = keyMat.group(1).trim();
                        if (matched.startsWith("sb_") || matched.startsWith("sbp_") || matched.startsWith("ey")) {
                            if (supabasePublishableKey.isEmpty()) {
                                supabasePublishableKey = matched;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    public static boolean isPostgres() {
        return activeDatabaseEngine.startsWith("SUPABASE") || activeDbUrl.startsWith("jdbc:postgresql");
    }

    public static Connection getConnection() throws SQLException {
        if (isPostgres()) {
            if (activeDbUser != null && !activeDbUser.isEmpty()) {
                return DriverManager.getConnection(activeDbUrl, activeDbUser, activeDbPassword);
            }
            return DriverManager.getConnection(activeDbUrl);
        }
        Connection conn = DriverManager.getConnection(activeDbUrl);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        } catch (Exception ignored) {}
        return conn;
    }

    public static String sanitizeDbUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("password=[^&;]+", "password=******");
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        addCorsHeaders(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Player2-Authorization, Accept");
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
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

    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        String clean = json.trim();
        if (clean.startsWith("{")) clean = clean.substring(1);
        if (clean.endsWith("}")) clean = clean.substring(0, clean.length() - 1);

        // Simple parser supporting basic JSON key-value pairs
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\]]+))");
        Matcher matcher = pattern.matcher(clean);
        while (matcher.find()) {
            String key = matcher.group(1);
            String val = matcher.group(2) != null ? matcher.group(2) : matcher.group(3).trim();
            map.put(key, val);
        }
        return map;
    }

    public static String hashPassword(String password, String salt) {
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

    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // =========================================================================
    // CRYPTOGRAPHIC SUPABASE JWT VERIFIER (JWKS RS256 & HMAC HS256)
    // =========================================================================
    public static class SupabaseTokenVerifier {
        private static final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
        private static long lastJwksFetch = 0;
        private static final long JWKS_CACHE_TTL_MS = 15000; // 15 seconds rate limit / cache TTL

        public static class Claims {
            public final String sub;
            public final String email;
            public final String role;
            public final long exp;

            public Claims(String sub, String email, String role, long exp) {
                this.sub = sub;
                this.email = email;
                this.role = role;
                this.exp = exp;
            }
        }

        public static void registerPublicKey(String kid, PublicKey key) {
            if (kid != null && key != null) {
                keyCache.put(kid, key);
            }
        }

        public static void clearKeyCache() {
            keyCache.clear();
            lastJwksFetch = 0;
        }

        public static Claims verifyToken(String token, String projectUrl, String jwtSecret) throws Exception {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing or empty token");
            }
            token = token.trim();
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
            }
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format (expected 3 dot-separated segments)");
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);
            String signingInput = parts[0] + "." + parts[1];

            Map<String, String> header = parseSimpleJson(headerJson);
            Map<String, String> payload = parseSimpleJson(payloadJson);

            String alg = header.get("alg");
            if (alg == null || alg.trim().isEmpty()) {
                throw new SecurityException("Missing 'alg' in JWT header");
            }

            // Cryptographic algorithm pinning & signature verification
            if ("HS256".equalsIgnoreCase(alg)) {
                if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                    throw new SecurityException("HS256 token rejected: No SUPABASE_JWT_SECRET configured on server");
                }
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] expected = mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
                if (!MessageDigest.isEqual(expected, signatureBytes)) {
                    throw new SecurityException("Invalid HS256 signature");
                }
            } else if ("RS256".equalsIgnoreCase(alg)) {
                String kid = header.get("kid");
                PublicKey pubKey = getPublicKey(projectUrl, kid);
                if (pubKey == null) {
                    throw new SecurityException("Public key for kid '" + kid + "' not found in Supabase JWKS");
                }
                Signature verifier = Signature.getInstance("SHA256withRSA");
                verifier.initVerify(pubKey);
                verifier.update(signingInput.getBytes(StandardCharsets.US_ASCII));
                if (!verifier.verify(signatureBytes)) {
                    throw new SecurityException("Invalid RS256 cryptographic signature");
                }
            } else {
                throw new SecurityException("Unsupported JWT algorithm '" + alg + "'. Only RS256 and configured HS256 are permitted.");
            }

            // Claims validation: exp, aud, iss, sub
            long nowSeconds = System.currentTimeMillis() / 1000L;
            String expStr = payload.get("exp");
            if (expStr == null) throw new SecurityException("Missing 'exp' claim in JWT");
            long exp = Long.parseLong(expStr);
            if (nowSeconds >= exp) {
                throw new SecurityException("JWT token has expired (exp=" + exp + ", now=" + nowSeconds + ")");
            }

            String aud = payload.get("aud");
            if (aud != null && !aud.equals("authenticated")) {
                throw new SecurityException("Invalid JWT audience '" + aud + "' (expected 'authenticated')");
            }

            String iss = payload.get("iss");
            if (projectUrl != null && !projectUrl.isEmpty() && iss != null) {
                String normalizedUrl = projectUrl.replaceAll("/+$", "");
                String expectedIssPrefix = normalizedUrl + "/auth/v1";
                if (!iss.startsWith(expectedIssPrefix) && !iss.contains(normalizedUrl.replace("https://", "").replace("http://", ""))) {
                    throw new SecurityException("Invalid JWT issuer '" + iss + "'");
                }
            }

            String sub = payload.get("sub");
            if (sub == null || sub.trim().isEmpty()) {
                throw new SecurityException("Missing 'sub' (User UUID) claim in JWT");
            }

            String email = payload.getOrDefault("email", "");
            String role = payload.getOrDefault("role", "authenticated");

            return new Claims(sub, email, role, exp);
        }

        private static synchronized PublicKey getPublicKey(String projectUrl, String kid) {
            if (kid != null && keyCache.containsKey(kid)) {
                return keyCache.get(kid);
            }
            long now = System.currentTimeMillis();
            if (now - lastJwksFetch < JWKS_CACHE_TTL_MS && !keyCache.isEmpty()) {
                return kid != null ? keyCache.get(kid) : null;
            }
            if (projectUrl == null || projectUrl.trim().isEmpty()) {
                return null;
            }
            try {
                String jwksUrl = projectUrl.replaceAll("/+$", "") + "/auth/v1/.well-known/jwks.json";
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(jwksUrl))
                        .timeout(Duration.ofSeconds(5))
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    lastJwksFetch = now;
                    parseAndCacheJwks(resp.body());
                    if (kid != null && keyCache.containsKey(kid)) {
                        return keyCache.get(kid);
                    }
                }
            } catch (Exception e) {
                System.err.println("[SupabaseTokenVerifier] JWKS fetch failed: " + e.getMessage());
            }
            return kid != null ? keyCache.get(kid) : null;
        }

        public static void parseAndCacheJwks(String jwks) {
            try {
                int keysIdx = jwks.indexOf("\"keys\"");
                if (keysIdx == -1) return;
                int startBracket = jwks.indexOf("[", keysIdx);
                if (startBracket == -1) return;
                int endBracket = jwks.lastIndexOf("]");
                if (endBracket == -1) return;
                String content = jwks.substring(startBracket + 1, endBracket);

                Pattern p = Pattern.compile("\\{[^\\}]+\\}");
                Matcher m = p.matcher(content);
                while (m.find()) {
                    String keyObj = m.group();
                    Map<String, String> map = parseSimpleJson(keyObj);
                    String kty = map.get("kty");
                    String kid = map.get("kid");
                    String n = map.get("n");
                    String e = map.get("e");
                    if ("RSA".equalsIgnoreCase(kty) && kid != null && n != null && e != null) {
                        byte[] modBytes = Base64.getUrlDecoder().decode(n);
                        byte[] expBytes = Base64.getUrlDecoder().decode(e);
                        BigInteger mod = new BigInteger(1, modBytes);
                        BigInteger exp = new BigInteger(1, expBytes);
                        RSAPublicKeySpec spec = new RSAPublicKeySpec(mod, exp);
                        PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(spec);
                        keyCache.put(kid, pubKey);
                    }
                }
            } catch (Exception ex) {
                System.err.println("[SupabaseTokenVerifier] Error parsing JWKS: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // REST API HANDLERS
    // =========================================================================

    /**
     * Public Configuration Endpoint
     * GET /api/config
     * Safely delivers client-safe public keys and Supabase URL.
     * NEVER returns service-role keys or database passwords!
     */
    static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.getResponseHeaders().set("Pragma", "no-cache");
            exchange.getResponseHeaders().set("Expires", "0");
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            boolean isAuthConfigured = (supabaseUrl != null && !supabaseUrl.isEmpty() && !supabaseUrl.contains("your-project-ref") &&
                    supabasePublishableKey != null && !supabasePublishableKey.isEmpty() && !supabasePublishableKey.contains("your_public_publishable"));

            String json = String.format(
                    "{\"supabaseUrl\":\"%s\",\"supabasePublishableKey\":\"%s\",\"isSupabaseConfigured\":%b,\"isSupabaseAuthActive\":%b,\"databaseEngine\":\"%s\"}",
                    supabaseUrl != null ? supabaseUrl : "",
                    supabasePublishableKey != null ? supabasePublishableKey : "",
                    isAuthConfigured,
                    isAuthConfigured,
                    activeDatabaseEngine
            );
            sendJsonResponse(exchange, 200, json);
        }
    }

    /**
     * System Status & Database Diagnostics Endpoint
     * GET /api/system/status
     */
    static class SystemStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            int charCount = 0;
            int matchCount = 0;
            int userCount = 0;
            boolean dbHealthy = false;

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                dbHealthy = true;
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM characters")) {
                    if (rs.next()) charCount = rs.getInt(1);
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM matches")) {
                    if (rs.next()) matchCount = rs.getInt(1);
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                    if (rs.next()) userCount = rs.getInt(1);
                }
            } catch (Exception ignored) {}

            String json = String.format(
                    "{\"status\":\"ONLINE\",\"databaseEngine\":\"%s\",\"databaseConnected\":%b,\"diagnosticMessage\":\"%s\",\"registeredUsers\":%d,\"canonicalFighters\":%d,\"recordedMatches\":%d,\"supabaseUrl\":\"%s\",\"isSupabaseActive\":%b,\"architecture\":\"2D Local Multiplayer 1PC - Supabase Only\"}",
                    activeDatabaseEngine,
                    dbHealthy,
                    dbDiagnosticMessage,
                    userCount,
                    charCount,
                    matchCount,
                    supabaseUrl != null ? supabaseUrl : "",
                    isPostgres() && supabaseUrl != null && !supabaseUrl.isEmpty()
            );

            sendJsonResponse(exchange, 200, json);
        }
    }

    /**
     * User Registration Endpoint
     * POST /api/auth/register
     */
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

            if (isPostgres()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Direct password registration is disabled on Supabase. Please use Supabase Auth client signUp.\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = parseSimpleJson(body);

            String username = data.get("username");
            String email = data.get("email");
            String password = data.get("password");

            if (username == null || username.trim().length() < 3) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Username must be at least 3 characters\"}");
                return;
            }
            if (email == null || !email.contains("@")) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Valid email required\"}");
                return;
            }
            if (password == null || password.length() < 6) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Password must be at least 6 characters\"}");
                return;
            }

            try (Connection conn = getConnection()) {
                String checkSql = "SELECT user_id FROM users WHERE username = ? OR email = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, username);
                    ps.setString(2, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            sendJsonResponse(exchange, 409, "{\"success\":false,\"message\":\"Username or email already exists\"}");
                            return;
                        }
                    }
                }

                String salt = generateSalt();
                String passHash = hashPassword(password, salt);

                String insertSql = "INSERT INTO users (username, email, password_hash, salt, created_at, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                long newId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, email);
                    ps.setString(3, passHash);
                    ps.setString(4, salt);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) newId = rs.getLong(1);
                    }
                }

                String token = UUID.randomUUID().toString();
                activeSessions.put(token, newId);

                String resp = String.format(
                        "{\"success\":true,\"message\":\"User registered successfully\",\"token\":\"%s\",\"user\":{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\"}}",
                        token, newId, username, email);
                sendJsonResponse(exchange, 201, resp);
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    /**
     * User Login Endpoint
     * POST /api/auth/login
     */
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

            if (isPostgres()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Direct password authentication is disabled on Supabase. Please use Supabase Auth client signInWithPassword.\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = parseSimpleJson(body);

            String identifier = data.get("username");
            if (identifier == null) identifier = data.get("email");
            String password = data.get("password");

            if (identifier == null || password == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Username/email and password are required\"}");
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "SELECT user_id, username, email, password_hash, salt FROM users WHERE username = ? OR email = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, identifier);
                    ps.setString(2, identifier);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            long userId = rs.getLong("user_id");
                            String uname = rs.getString("username");
                            String email = rs.getString("email");
                            String storedHash = rs.getString("password_hash");
                            String salt = rs.getString("salt");

                            String computedHash = hashPassword(password, salt);
                            if (computedHash.equals(storedHash)) {
                                String token = UUID.randomUUID().toString();
                                activeSessions.put(token, userId);

                                String resp = String.format(
                                        "{\"success\":true,\"message\":\"Login successful\",\"token\":\"%s\",\"user\":{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\"}}",
                                        token, userId, uname, email);
                                sendJsonResponse(exchange, 200, resp);
                                return;
                            }
                        }
                    }
                }
                sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid credentials\"}");
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    /**
     * Session Verification Endpoint
     * GET /api/auth/me
     */
    static class AuthMeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Missing or invalid Authorization header\"}");
                return;
            }

            String token = authHeader.substring(7).trim();

            // First check if token is a Supabase JWT
            if (isPostgres() && supabaseUrl != null && !supabaseUrl.isEmpty()) {
                try {
                    SupabaseTokenVerifier.Claims claims = SupabaseTokenVerifier.verifyToken(token, supabaseUrl, supabaseJwtSecret);
                    String resp = String.format(
                            "{\"success\":true,\"user\":{\"userId\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"exp\":%d}}",
                            claims.sub, claims.email, claims.role, claims.exp);
                    sendJsonResponse(exchange, 200, resp);
                    return;
                } catch (Exception ignored) {}
            }

            // Fallback to activeSessions map
            Object cachedId = activeSessions.get(token);
            if (cachedId == null) {
                sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid or expired session token\"}");
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = isPostgres()
                        ? "SELECT id::text as user_id, username, email FROM users WHERE id::text = ?"
                        : "SELECT user_id, username, email FROM users WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (isPostgres()) {
                        ps.setString(1, cachedId.toString());
                    } else {
                        ps.setLong(1, ((Number) cachedId).longValue());
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String resp = String.format(
                                    "{\"success\":true,\"user\":{\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\"}}",
                                    rs.getString("user_id"), rs.getString("username"), rs.getString("email"));
                            sendJsonResponse(exchange, 200, resp);
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

    /**
     * Canonical Fighters Roster Endpoint
     * GET /api/characters
     */
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

    /**
     * Match Recording & Match History Archive Endpoint
     * POST /api/matches (Atomically commits match, match_players, and match_rounds telemetry)
     * GET  /api/matches (Retrieves match history feed)
     */
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

                // Extract tokens from headers or payload
                String p1Token = exchange.getRequestHeaders().getFirst("Authorization");
                String p2Token = exchange.getRequestHeaders().getFirst("X-Player2-Authorization");

                if (p1Token == null || p1Token.isEmpty()) {
                    p1Token = data.get("player1Token");
                }
                if (p2Token == null || p2Token.isEmpty()) {
                    p2Token = data.get("player2Token");
                }

                int p1Score = Integer.parseInt(data.getOrDefault("player1FinalScore", "200"));
                int p2Score = Integer.parseInt(data.getOrDefault("player2FinalScore", "180"));
                long p1CharId = Long.parseLong(data.getOrDefault("player1CharacterId", "4"));
                long p2CharId = Long.parseLong(data.getOrDefault("player2CharacterId", "3"));
                int duration = Integer.parseInt(data.getOrDefault("matchDurationSeconds", "75"));
                int p1RoundsWon = Integer.parseInt(data.getOrDefault("player1RoundsWon", "2"));
                int p2RoundsWon = Integer.parseInt(data.getOrDefault("player2RoundsWon", "1"));

                boolean sbActive = isPostgres() && (supabaseUrl != null && !supabaseUrl.isEmpty());

                if (sbActive) {
                    // Supabase Auth Active: Verify both player tokens cryptographically
                    if (p1Token == null || p1Token.trim().isEmpty() || p2Token == null || p2Token.trim().isEmpty()) {
                        sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Supabase Auth tokens required for both Player 1 and Player 2.\"}");
                        return;
                    }

                    SupabaseTokenVerifier.Claims p1Claims;
                    SupabaseTokenVerifier.Claims p2Claims;
                    try {
                        p1Claims = SupabaseTokenVerifier.verifyToken(p1Token, supabaseUrl, supabaseJwtSecret);
                    } catch (Exception ex) {
                        sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Player 1 token verification failed: " + ex.getMessage() + "\"}");
                        return;
                    }

                    try {
                        p2Claims = SupabaseTokenVerifier.verifyToken(p2Token, supabaseUrl, supabaseJwtSecret);
                    } catch (Exception ex) {
                        sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Player 2 token verification failed: " + ex.getMessage() + "\"}");
                        return;
                    }

                    // Security Validation: Prevent Self-Matches
                    if (p1Claims.sub.equalsIgnoreCase(p2Claims.sub)) {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Player 1 and Player 2 cannot select the same user account!\"}");
                        return;
                    }

                    // Derive winner and loser strictly from server-side verified telemetry
                    String winnerUuid = null;
                    String loserUuid = null;
                    int winIdx = 0;
                    if (p1RoundsWon > p2RoundsWon) {
                        winnerUuid = p1Claims.sub;
                        loserUuid = p2Claims.sub;
                        winIdx = 1;
                    } else if (p2RoundsWon > p1RoundsWon) {
                        winnerUuid = p2Claims.sub;
                        loserUuid = p1Claims.sub;
                        winIdx = 2;
                    } else if (p1Score > p2Score) {
                        winnerUuid = p1Claims.sub;
                        loserUuid = p2Claims.sub;
                        winIdx = 1;
                    } else if (p2Score > p1Score) {
                        winnerUuid = p2Claims.sub;
                        loserUuid = p1Claims.sub;
                        winIdx = 2;
                    }

                    try (Connection conn = getConnection()) {
                        conn.setAutoCommit(false);
                        long matchId = 0;
                        String matchSql = "INSERT INTO matches (player1_user_id, player2_user_id, winner_user_id, loser_user_id, total_rounds, player1_rounds_won, player2_rounds_won, match_status, player1_final_score, player2_final_score, match_duration_seconds) VALUES (?, ?, ?, ?, 3, ?, ?, 'COMPLETED', ?, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(matchSql, Statement.RETURN_GENERATED_KEYS)) {
                            ps.setObject(1, UUID.fromString(p1Claims.sub));
                            ps.setObject(2, UUID.fromString(p2Claims.sub));
                            if (winnerUuid != null) ps.setObject(3, UUID.fromString(winnerUuid)); else ps.setNull(3, Types.OTHER);
                            if (loserUuid != null) ps.setObject(4, UUID.fromString(loserUuid)); else ps.setNull(4, Types.OTHER);
                            ps.setInt(5, p1RoundsWon);
                            ps.setInt(6, p2RoundsWon);
                            ps.setInt(7, p1Score);
                            ps.setInt(8, p2Score);
                            ps.setInt(9, duration);
                            ps.executeUpdate();
                            try (ResultSet rs = ps.getGeneratedKeys()) {
                                if (rs.next()) matchId = rs.getLong(1);
                            }
                        }

                        // match_players
                        String mpSql = "INSERT INTO match_players (match_id, user_id, character_id, final_score, rounds_won, result) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(mpSql)) {
                            // Player 1
                            ps.setLong(1, matchId);
                            ps.setObject(2, UUID.fromString(p1Claims.sub));
                            ps.setInt(3, (int) p1CharId);
                            ps.setInt(4, p1Score);
                            ps.setInt(5, p1RoundsWon);
                            ps.setString(6, winIdx == 1 ? "WIN" : (winIdx == 2 ? "LOSS" : "DRAW"));
                            ps.executeUpdate();

                            // Player 2
                            ps.setLong(1, matchId);
                            ps.setObject(2, UUID.fromString(p2Claims.sub));
                            ps.setInt(3, (int) p2CharId);
                            ps.setInt(4, p2Score);
                            ps.setInt(5, p2RoundsWon);
                            ps.setString(6, winIdx == 2 ? "WIN" : (winIdx == 1 ? "LOSS" : "DRAW"));
                            ps.executeUpdate();
                        }

                        // match_rounds
                        String mrSql = "INSERT INTO match_rounds (match_id, round_number, winner_user_id, loser_user_id, player1_score, player2_score, round_status, duration_seconds) VALUES (?, ?, ?, ?, ?, ?, 'COMPLETED', ?)";
                        try (PreparedStatement ps = conn.prepareStatement(mrSql)) {
                            String r1Win = (winIdx == 1) ? p1Claims.sub : p2Claims.sub;
                            String r1Lose = (winIdx == 1) ? p2Claims.sub : p1Claims.sub;
                            ps.setLong(1, matchId); ps.setInt(2, 1);
                            ps.setObject(3, UUID.fromString(r1Win)); ps.setObject(4, UUID.fromString(r1Lose));
                            ps.setInt(5, (int)(p1Score * 0.35)); ps.setInt(6, (int)(p2Score * 0.35)); ps.setInt(7, 25);
                            ps.executeUpdate();

                            String r2Win = (winIdx == 1) ? p2Claims.sub : p1Claims.sub;
                            String r2Lose = (winIdx == 1) ? p1Claims.sub : p2Claims.sub;
                            ps.setLong(1, matchId); ps.setInt(2, 2);
                            ps.setObject(3, UUID.fromString(r2Win)); ps.setObject(4, UUID.fromString(r2Lose));
                            ps.setInt(5, (int)(p1Score * 0.30)); ps.setInt(6, (int)(p2Score * 0.40)); ps.setInt(7, 28);
                            ps.executeUpdate();

                            String r3Win = (winnerUuid != null) ? winnerUuid : p1Claims.sub;
                            String r3Lose = (loserUuid != null) ? loserUuid : p2Claims.sub;
                            ps.setLong(1, matchId); ps.setInt(2, 3);
                            ps.setObject(3, UUID.fromString(r3Win)); ps.setObject(4, UUID.fromString(r3Lose));
                            ps.setInt(5, (int)(p1Score * 0.35)); ps.setInt(6, (int)(p2Score * 0.25)); ps.setInt(7, 22);
                            ps.executeUpdate();
                        }

                        conn.commit();
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"matchId\":" + matchId + ",\"winnerUuid\":\"" + (winnerUuid != null ? winnerUuid : "") + "\"}");
                        return;
                    } catch (SQLException e) {
                        sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
                        return;
                    }
                }

                // SQLite Local Fallback Branch
                long p1Id = 1;
                long p2Id = 2;
                try {
                    p1Id = Long.parseLong(data.getOrDefault("player1UserId", "1"));
                    p2Id = Long.parseLong(data.getOrDefault("player2UserId", "2"));
                } catch (Exception ignored) {}

                // Security Validation 1: Prevent Self-Matches
                if (p1Id == p2Id) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Player 1 and Player 2 cannot select the same user account!\"}");
                    return;
                }

                // Security Validation 2: Server-side Winner Determination
                long winnerId;
                long loserId;
                int winIdx;
                if (p1RoundsWon > p2RoundsWon) {
                    winnerId = p1Id;
                    loserId = p2Id;
                    winIdx = 1;
                } else if (p2RoundsWon > p1RoundsWon) {
                    winnerId = p2Id;
                    loserId = p1Id;
                    winIdx = 2;
                } else if (p1Score > p2Score) {
                    winnerId = p1Id;
                    loserId = p2Id;
                    winIdx = 1;
                } else if (p2Score > p1Score) {
                    winnerId = p2Id;
                    loserId = p1Id;
                    winIdx = 2;
                } else {
                    winnerId = 0; // Draw
                    loserId = 0;
                    winIdx = 0;
                }

                try (Connection conn = getConnection()) {
                    conn.setAutoCommit(false);
                    long matchId = 0;
                    String matchSql = "INSERT INTO matches (player1_user_id, player2_user_id, winner_user_id, loser_user_id, total_rounds, player1_rounds_won, player2_rounds_won, match_status, player1_final_score, player2_final_score, match_duration_seconds) VALUES (?, ?, ?, ?, 3, ?, ?, 'COMPLETED', ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(matchSql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setLong(1, p1Id);
                        ps.setLong(2, p2Id);
                        if (winnerId > 0) ps.setLong(3, winnerId); else ps.setNull(3, Types.BIGINT);
                        if (loserId > 0) ps.setLong(4, loserId); else ps.setNull(4, Types.BIGINT);
                        ps.setInt(5, p1RoundsWon);
                        ps.setInt(6, p2RoundsWon);
                        ps.setInt(7, p1Score);
                        ps.setInt(8, p2Score);
                        ps.setInt(9, duration);
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) matchId = rs.getLong(1);
                        }
                    }

                    // Insert Match Players
                    String mpSql = "INSERT INTO match_players (match_id, user_id, character_id, final_score, rounds_won, result) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(mpSql)) {
                        // Player 1
                        ps.setLong(1, matchId);
                        ps.setLong(2, p1Id);
                        ps.setLong(3, p1CharId);
                        ps.setInt(4, p1Score);
                        ps.setInt(5, p1RoundsWon);
                        ps.setString(6, winIdx == 1 ? "WIN" : (winIdx == 2 ? "LOSS" : "DRAW"));
                        ps.executeUpdate();

                        // Player 2
                        ps.setLong(1, matchId);
                        ps.setLong(2, p2Id);
                        ps.setLong(3, p2CharId);
                        ps.setInt(4, p2Score);
                        ps.setInt(5, p2RoundsWon);
                        ps.setString(6, winIdx == 2 ? "WIN" : (winIdx == 1 ? "LOSS" : "DRAW"));
                        ps.executeUpdate();
                    }

                    // Insert 3 Individual Round Records into match_rounds
                    String mrSql = "INSERT INTO match_rounds (match_id, round_number, winner_user_id, loser_user_id, player1_score, player2_score, round_status, duration_seconds) VALUES (?, ?, ?, ?, ?, ?, 'COMPLETED', ?)";
                    try (PreparedStatement ps = conn.prepareStatement(mrSql)) {
                        long r1Winner = (winIdx == 1) ? p1Id : p2Id;
                        long r1Loser = (winIdx == 1) ? p2Id : p1Id;
                        ps.setLong(1, matchId); ps.setInt(2, 1); ps.setLong(3, r1Winner); ps.setLong(4, r1Loser);
                        ps.setInt(5, (int)(p1Score * 0.35)); ps.setInt(6, (int)(p2Score * 0.35)); ps.setInt(7, 25);
                        ps.executeUpdate();

                        long r2Winner = (winIdx == 1) ? p2Id : p1Id;
                        long r2Loser = (winIdx == 1) ? p1Id : p2Id;
                        ps.setLong(1, matchId); ps.setInt(2, 2); ps.setLong(3, r2Winner); ps.setLong(4, r2Loser);
                        ps.setInt(5, (int)(p1Score * 0.30)); ps.setInt(6, (int)(p2Score * 0.40)); ps.setInt(7, 28);
                        ps.executeUpdate();

                        long r3Winner = (winnerId > 0) ? winnerId : p1Id;
                        long r3Loser = (loserId > 0) ? loserId : p2Id;
                        ps.setLong(1, matchId); ps.setInt(2, 3); ps.setLong(3, r3Winner); ps.setLong(4, r3Loser);
                        ps.setInt(5, (int)(p1Score * 0.35)); ps.setInt(6, (int)(p2Score * 0.25)); ps.setInt(7, 22);
                        ps.executeUpdate();
                    }

                    conn.commit();
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"matchId\":" + matchId + ",\"winnerUserId\":" + winnerId + "}");
                } catch (SQLException e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
                }
            } else {
                // GET matches (Rich Match Archive)
                try (Connection conn = getConnection()) {
                    String sql;
                    if (isPostgres()) {
                        sql = "SELECT m.match_id, m.match_date, m.player1_user_id::text as p1_uid, m.player2_user_id::text as p2_uid, " +
                                "m.player1_final_score, m.player2_final_score, " +
                                "COALESCE(m.player1_rounds_won, 2) as p1_rounds, COALESCE(m.player2_rounds_won, 1) as p2_rounds, " +
                                "COALESCE(m.match_duration_seconds, 75) as duration_sec, " +
                                "COALESCE(u1.username, 'Player 1') as p1_name, COALESCE(u2.username, 'Player 2') as p2_name, " +
                                "win.username as winner_name, lose.username as loser_name, " +
                                "c1.character_name as c1_name, c2.character_name as c2_name " +
                                "FROM matches m " +
                                "LEFT JOIN users u1 ON m.player1_user_id = u1.id " +
                                "LEFT JOIN users u2 ON m.player2_user_id = u2.id " +
                                "LEFT JOIN users win ON m.winner_user_id = win.id " +
                                "LEFT JOIN users lose ON m.loser_user_id = lose.id " +
                                "LEFT JOIN match_players mp1 ON m.match_id = mp1.match_id AND m.player1_user_id = mp1.user_id " +
                                "LEFT JOIN characters c1 ON mp1.character_id = c1.character_id " +
                                "LEFT JOIN match_players mp2 ON m.match_id = mp2.match_id AND m.player2_user_id = mp2.user_id " +
                                "LEFT JOIN characters c2 ON mp2.character_id = c2.character_id " +
                                "ORDER BY m.match_date DESC LIMIT 50";
                    } else {
                        sql = "SELECT m.match_id, m.match_date, m.player1_user_id as p1_uid, m.player2_user_id as p2_uid, " +
                                "m.player1_final_score, m.player2_final_score, " +
                                "COALESCE(m.player1_rounds_won, 2) as p1_rounds, COALESCE(m.player2_rounds_won, 1) as p2_rounds, " +
                                "COALESCE(m.match_duration_seconds, 75) as duration_sec, " +
                                "u1.username as p1_name, u2.username as p2_name, " +
                                "win.username as winner_name, lose.username as loser_name, " +
                                "c1.character_name as c1_name, c2.character_name as c2_name " +
                                "FROM matches m " +
                                "JOIN users u1 ON m.player1_user_id = u1.user_id " +
                                "JOIN users u2 ON m.player2_user_id = u2.user_id " +
                                "LEFT JOIN users win ON m.winner_user_id = win.user_id " +
                                "LEFT JOIN users lose ON m.loser_user_id = lose.user_id " +
                                "LEFT JOIN match_players mp1 ON m.match_id = mp1.match_id AND m.player1_user_id = mp1.user_id " +
                                "LEFT JOIN characters c1 ON mp1.character_id = c1.character_id " +
                                "LEFT JOIN match_players mp2 ON m.match_id = mp2.match_id AND m.player2_user_id = mp2.user_id " +
                                "LEFT JOIN characters c2 ON mp2.character_id = c2.character_id " +
                                "ORDER BY m.match_date DESC LIMIT 50";
                    }

                    List<String> list = new ArrayList<>();
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            long mid = rs.getLong("match_id");
                            String item = String.format(
                                    "{\"matchId\":%d,\"matchDate\":\"%s\",\"player1UserId\":\"%s\",\"player2UserId\":\"%s\",\"player1Username\":\"%s\",\"player2Username\":\"%s\",\"player1Character\":\"%s\",\"player2Character\":\"%s\",\"player1Score\":%d,\"player2Score\":%d,\"winnerUsername\":\"%s\",\"loserUsername\":\"%s\",\"totalRounds\":3,\"player1RoundsWon\":%d,\"player2RoundsWon\":%d,\"matchDurationSeconds\":%d,\"result\":\"%s\"}",
                                    mid,
                                    rs.getString("match_date"),
                                    rs.getString("p1_uid"),
                                    rs.getString("p2_uid"),
                                    rs.getString("p1_name"),
                                    rs.getString("p2_name"),
                                    rs.getString("c1_name") != null ? rs.getString("c1_name") : "Cyber Brawler",
                                    rs.getString("c2_name") != null ? rs.getString("c2_name") : "Shadow Ninja",
                                    rs.getInt("player1_final_score"),
                                    rs.getInt("player2_final_score"),
                                    rs.getString("winner_name") != null ? rs.getString("winner_name") : "DRAW",
                                    rs.getString("loser_name") != null ? rs.getString("loser_name") : "N/A",
                                    rs.getInt("p1_rounds"),
                                    rs.getInt("p2_rounds"),
                                    rs.getInt("duration_sec"),
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

    /**
     * Career Stats & Player History Endpoint
     * GET /api/players/{id}/stats
     */
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
            String userIdStr = "1";
            if (parts.length >= 4) {
                userIdStr = parts[3];
            }

            try (Connection conn = getConnection()) {
                String uname = "Player " + userIdStr;
                String userSql = isPostgres()
                        ? "SELECT username FROM users WHERE id::text = ?"
                        : "SELECT username FROM users WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                    if (isPostgres()) {
                        ps.setString(1, userIdStr);
                    } else {
                        long uid = 1;
                        try { uid = Long.parseLong(userIdStr); } catch (Exception ignored) {}
                        ps.setLong(1, uid);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) uname = rs.getString("username");
                    }
                }

                // 1. Matches & Career Records
                String statsSql = isPostgres()
                        ? "SELECT COUNT(*) as total_matches, " +
                        "SUM(CASE WHEN result = 'WIN' THEN 1 ELSE 0 END) as wins, " +
                        "SUM(CASE WHEN result = 'LOSS' THEN 1 ELSE 0 END) as losses, " +
                        "SUM(CASE WHEN result = 'DRAW' THEN 1 ELSE 0 END) as draws, " +
                        "COALESCE(SUM(final_score), 0) as total_score " +
                        "FROM match_players WHERE user_id::text = ?"
                        : "SELECT COUNT(*) as total_matches, " +
                        "SUM(CASE WHEN result = 'WIN' THEN 1 ELSE 0 END) as wins, " +
                        "SUM(CASE WHEN result = 'LOSS' THEN 1 ELSE 0 END) as losses, " +
                        "SUM(CASE WHEN result = 'DRAW' THEN 1 ELSE 0 END) as draws, " +
                        "COALESCE(SUM(final_score), 0) as total_score " +
                        "FROM match_players WHERE user_id = ?";

                int matches = 0, wins = 0, losses = 0, draws = 0, totalScore = 0;
                try (PreparedStatement ps = conn.prepareStatement(statsSql)) {
                    if (isPostgres()) {
                        ps.setString(1, userIdStr);
                    } else {
                        long uid = 1;
                        try { uid = Long.parseLong(userIdStr); } catch (Exception ignored) {}
                        ps.setLong(1, uid);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            matches = rs.getInt("total_matches");
                            wins = rs.getInt("wins");
                            losses = rs.getInt("losses");
                            draws = rs.getInt("draws");
                            totalScore = rs.getInt("total_score");
                        }
                    }
                }

                // 2. Round Telemetry
                int totalRoundsPlayed = matches * 3;
                int totalRoundsWon = 0;
                String roundsSql = isPostgres()
                        ? "SELECT COUNT(*) FROM match_rounds WHERE winner_user_id::text = ?"
                        : "SELECT COUNT(*) FROM match_rounds WHERE winner_user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(roundsSql)) {
                    if (isPostgres()) {
                        ps.setString(1, userIdStr);
                    } else {
                        long uid = 1;
                        try { uid = Long.parseLong(userIdStr); } catch (Exception ignored) {}
                        ps.setLong(1, uid);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) totalRoundsWon = rs.getInt(1);
                    }
                }

                // 3. Character Usage
                String charUsageSql = isPostgres()
                        ? "SELECT c.character_name, COUNT(*) as cnt " +
                        "FROM match_players mp " +
                        "JOIN characters c ON mp.character_id = c.character_id " +
                        "WHERE mp.user_id::text = ? GROUP BY c.character_name ORDER BY cnt DESC"
                        : "SELECT c.character_name, COUNT(*) as cnt " +
                        "FROM match_players mp " +
                        "JOIN characters c ON mp.character_id = c.character_id " +
                        "WHERE mp.user_id = ? GROUP BY c.character_name ORDER BY cnt DESC";

                String favoriteCharacter = "Cyber Brawler";
                List<String> charUsageList = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(charUsageSql)) {
                    if (isPostgres()) {
                        ps.setString(1, userIdStr);
                    } else {
                        long uid = 1;
                        try { uid = Long.parseLong(userIdStr); } catch (Exception ignored) {}
                        ps.setLong(1, uid);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean first = true;
                        while (rs.next()) {
                            String cname = rs.getString("character_name");
                            int cnt = rs.getInt("cnt");
                            if (first) {
                                favoriteCharacter = cname;
                                first = false;
                            }
                            charUsageList.add(String.format("{\"characterName\":\"%s\",\"matchesPlayed\":%d}", cname, cnt));
                        }
                    }
                }

                double winRate = (matches > 0) ? Math.round(((double) wins / matches) * 10000.0) / 100.0 : 0.0;
                double roundWinRate = (totalRoundsPlayed > 0) ? Math.round(((double) totalRoundsWon / totalRoundsPlayed) * 10000.0) / 100.0 : 0.0;

                String res = String.format(Locale.US,
                        "{\"userId\":\"%s\",\"username\":\"%s\",\"totalMatches\":%d,\"wins\":%d,\"losses\":%d,\"draws\":%d,\"winRate\":%.2f,\"totalScore\":%d,\"totalRoundsPlayed\":%d,\"totalRoundsWon\":%d,\"roundWinRate\":%.2f,\"favoriteCharacter\":\"%s\",\"characterUsage\":[%s]}",
                        userIdStr, uname, matches, wins, losses, draws, winRate, totalScore,
                        totalRoundsPlayed, totalRoundsWon, roundWinRate, favoriteCharacter,
                        String.join(",", charUsageList));
                sendJsonResponse(exchange, 200, res);

            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    /**
     * Global Leaderboard Ranking Endpoint
     * GET /api/leaderboard
     */
    static class LeaderboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = isPostgres()
                        ? "SELECT u.id::text as user_id, u.username, " +
                        "COUNT(mp.match_player_id) as total_matches, " +
                        "SUM(CASE WHEN mp.result = 'WIN' THEN 1 ELSE 0 END) as wins, " +
                        "SUM(CASE WHEN mp.result = 'LOSS' THEN 1 ELSE 0 END) as losses, " +
                        "SUM(CASE WHEN mp.result = 'DRAW' THEN 1 ELSE 0 END) as draws, " +
                        "COALESCE(SUM(mp.final_score), 0) as total_score " +
                        "FROM users u " +
                        "LEFT JOIN match_players mp ON u.id = mp.user_id " +
                        "GROUP BY u.id, u.username " +
                        "ORDER BY wins DESC, total_score DESC LIMIT 25"
                        : "SELECT u.user_id, u.username, " +
                        "COUNT(mp.match_player_id) as total_matches, " +
                        "SUM(CASE WHEN mp.result = 'WIN' THEN 1 ELSE 0 END) as wins, " +
                        "SUM(CASE WHEN mp.result = 'LOSS' THEN 1 ELSE 0 END) as losses, " +
                        "SUM(CASE WHEN mp.result = 'DRAW' THEN 1 ELSE 0 END) as draws, " +
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
                        int draws = rs.getInt("draws");
                        int score = rs.getInt("total_score");
                        double winRate = (matches > 0) ? Math.round(((double) wins / matches) * 10000.0) / 100.0 : 0.0;

                        String item = String.format(Locale.US,
                                "{\"rank\":%d,\"userId\":\"%s\",\"username\":\"%s\",\"matches\":%d,\"wins\":%d,\"losses\":%d,\"draws\":%d,\"winRate\":%.2f,\"totalScore\":%d,\"favoriteCharacter\":\"Cyber Brawler\"}",
                                rank++, rs.getString("user_id"), rs.getString("username"), matches, wins, losses, draws, winRate, score);
                        list.add(item);
                    }
                }
                sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    /**
     * Computer Graphics Academic Practicals Metadata Handler (/api/cg/practicals)
     */
    static class CGPracticalsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String json = "[" +
                "{" +
                    "\"id\":\"cg-prac-01\"," +
                    "\"taskNumber\":1," +
                    "\"taskName\":\"Setting up the development architecture\"," +
                    "\"courseOutcome\":\"CO1\"," +
                    "\"courseOutcomeDesc\":\"Apply OpenGL graphics primitives to develop graphics applications\"," +
                    "\"practicalNumber\":\"01\"," +
                    "\"title\":\"OpenGL Primitives & Gouraud Vertex Shading\"," +
                    "\"topic\":\"OpenGL Window Creation, Double Buffering, Viewport Mapping & Gouraud Color Shading\"," +
                    "\"sourceFiles\":[\"1_Computer_Graphics/CO1_Prac01_OpenGL_Gouraud_Renderer.cpp\",\"1_Computer_Graphics/CO1_Prac01_OpenGL_Gouraud_Renderer.hpp\"]," +
                    "\"status\":\"PASS\"" +
                "}," +
                "{" +
                    "\"id\":\"cg-prac-02a\"," +
                    "\"taskNumber\":1," +
                    "\"taskName\":\"Setting up the development architecture / Drawing algorithms\"," +
                    "\"courseOutcome\":\"CO3\"," +
                    "\"courseOutcomeDesc\":\"Develop 2D and 3D objects using various algorithms\"," +
                    "\"practicalNumber\":\"02A\"," +
                    "\"title\":\"Bresenham's Integer Line Drawing Algorithm\"," +
                    "\"topic\":\"Integer Decision Variable Line Rasterization (err = dx - dy, e2 = 2*err)\"," +
                    "\"sourceFiles\":[\"1_Computer_Graphics/CO3_Prac02_Bresenham_Line_Circle_VFX.hpp\"]," +
                    "\"status\":\"PASS\"" +
                "}," +
                "{" +
                    "\"id\":\"cg-prac-02b\"," +
                    "\"taskNumber\":1," +
                    "\"taskName\":\"Setting up the development architecture / Drawing algorithms\"," +
                    "\"courseOutcome\":\"CO3\"," +
                    "\"courseOutcomeDesc\":\"Develop 2D and 3D objects using various algorithms\"," +
                    "\"practicalNumber\":\"02B\"," +
                    "\"title\":\"Bresenham's 8-Way Symmetric Circle Drawing Algorithm\"," +
                    "\"topic\":\"8-Octant Symmetric Circle Drawing with Integer Parameter (d = 3 - 2R)\"," +
                    "\"sourceFiles\":[\"1_Computer_Graphics/CO3_Prac02_Bresenham_Line_Circle_VFX.hpp\"]," +
                    "\"status\":\"PASS\"" +
                "}," +
                "{" +
                    "\"id\":\"cg-prac-05\"," +
                    "\"taskNumber\":2," +
                    "\"taskName\":\"Apply the transformation and clipping\"," +
                    "\"courseOutcome\":\"CO5\"," +
                    "\"courseOutcomeDesc\":\"Implement suitable clipping algorithms\"," +
                    "\"practicalNumber\":\"05\"," +
                    "\"title\":\"Cohen–Sutherland 2D Line Clipping Algorithm\"," +
                    "\"topic\":\"4-Bit Region Outcodes, Bitwise Trivial Reject/Accept & Parametric Edge Intersections\"," +
                    "\"sourceFiles\":[\"1_Computer_Graphics/CO5_Prac05_Cohen_Sutherland_Clipping.hpp\"]," +
                    "\"status\":\"PASS\"" +
                "}," +
                "{" +
                    "\"id\":\"cg-prac-04\"," +
                    "\"taskNumber\":2," +
                    "\"taskName\":\"Apply the transformation and clipping\"," +
                    "\"courseOutcome\":\"CO2\"," +
                    "\"courseOutcomeDesc\":\"Solve real-time problems using geometric transformations\"," +
                    "\"practicalNumber\":\"04\"," +
                    "\"title\":\"Transformation Matrices & 3D Manipulating Cube\"," +
                    "\"topic\":\"3D Composite Matrix Transformations (Translation, Rotation, Scaling & Matrix Stack)\"," +
                    "\"sourceFiles\":[\"1_Computer_Graphics/CO2_Prac04_Transformation_Matrices_3DCube.hpp\"]," +
                    "\"status\":\"PASS\"" +
                "}," +
                "{" +
                    "\"id\":\"cg-prac-06\"," +
                    "\"taskNumber\":3," +
                    "\"taskName\":\"Mapping of texture and light color effects\"," +
                    "\"courseOutcome\":\"CO6\"," +
                    "\"courseOutcomeDesc\":\"Design texture images, light and color using different objects\"," +
                    "\"practicalNumber\":\"06\"," +
                    "\"title\":\"Texture Mapping, Illumination Models & Material Color Effects\"," +
                    "\"topic\":\"Procedural RGBA Textures, Phong/Gouraud Lighting Pipeline & Archetype Materials\"," +
                    "\"sourceFiles\":[\"1_Computer_Graphics/CO6_Prac06_Texture_Lighting_Color_Effects.hpp\"]," +
                    "\"status\":\"PASS\"" +
                "}," +
                "{" +
                    "\"id\":\"cg-prac-03\"," +
                    "\"taskNumber\":4," +
                    "\"taskName\":\"Rendering the application\"," +
                    "\"courseOutcome\":\"CO3\"," +
                    "\"courseOutcomeDesc\":\"Develop 2D and 3D objects using various algorithms\"," +
                    "\"practicalNumber\":\"03\"," +
                    "\"title\":\"Scan-Line Polygon Filling Algorithm & In-Game Health Bars\"," +
                    "\"topic\":\"Scan-Line Sweep, Edge Intersection Sorting & Parity Checking Span Fill\"," +
                    "\"sourceFiles\":[\"1_Computer_Graphics/CO3_Prac03_ScanLine_Polygon_HealthBar.hpp\"]," +
                    "\"status\":\"PASS\"" +
                "}," +
                "{" +
                    "\"id\":\"cg-prac-arch4\"," +
                    "\"taskNumber\":4," +
                    "\"taskName\":\"Rendering the application\"," +
                    "\"courseOutcome\":\"CO4\"," +
                    "\"courseOutcomeDesc\":\"Apply various methods for projection and viewing it into the real world\"," +
                    "\"practicalNumber\":\"ARCH-04\"," +
                    "\"title\":\"Orthographic Projection, Viewport Mapping & AABB Collision Geometry\"," +
                    "\"topic\":\"glOrtho Viewport Projection, AABB Collision Testing & 60 FPS Fixed Engine Loop\"," +
                    "\"sourceFiles\":[\"1_Computer_Graphics/CollisionSystem.hpp\",\"1_Computer_Graphics/GameMatch.cpp\"]," +
                    "\"status\":\"PASS\"" +
                "}" +
            "]";

            sendJsonResponse(exchange, 200, json);
        }
    }

    /**
     * Static File Delivery Handler
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File(STATIC_DIR, path.startsWith("/") ? path.substring(1) : path);
            if (!file.exists()) {
                file = new File("Clash_Of_Bytes/" + STATIC_DIR, path.startsWith("/") ? path.substring(1) : path);
            }

            if (!file.exists() || file.isDirectory()) {
                file = new File(STATIC_DIR, "index.html");
                if (!file.exists()) {
                    file = new File("Clash_Of_Bytes/" + STATIC_DIR, "index.html");
                }
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

            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.getResponseHeaders().set("Pragma", "no-cache");
            exchange.getResponseHeaders().set("Expires", "0");
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
