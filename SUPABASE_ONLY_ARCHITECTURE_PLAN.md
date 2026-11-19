# Clash of Bytes: Permanent Supabase-Only Database Architecture Plan

**Document Title:** Final Architecture Specification & Technical Implementation Plan for Unified Supabase Integration  
**Project:** Clash of Bytes – 2D Local Multiplayer Fighting Game  
**Institution:** MIT Academy of Engineering (MIT AOE), Alandi (D.), Pune  
**Department:** Department of Computer Engineering (Software Engineering)  
**Date:** 2026-11-17T13:35:00+05:30  
**Status:** **FINAL APPROVED ARCHITECTURAL SPECIFICATION — 100% DECISIONS CLOSED (ZERO UNRESOLVED QUESTIONS)**  
**Current Phase Safeguards:**
- Local SQLite database (`clash_of_bytes.db`) is **NOT** deleted (preserved strictly as temporary backup).
- Live Supabase cloud connection is **NOT** initiated yet.
- Application source code is **NOT** modified yet (documentation and architectural baseline turn).
- Zero deployments, remotes, or GitHub pushes executed.
- Academic practical modules (CG, DSA, COA, Java CO1–CO4) remain **100% intact**.

---

## 1. Executive Architectural Mandate

By final architectural decision, **Supabase** is established as the **sole, permanent database and authentication provider** for the entire Clash of Bytes platform across both the Web Arena and the Java Swing desktop application.

### Core Architecture & Security Principles:
1. **Single Authentication Provider:** **Supabase Auth (`auth.users`)** is the sole identity and authentication provider for both the Web Arena platform and the Java Swing desktop application.
2. **Elimination of Redundant Password Systems:** No separate SHA-256 or BCrypt password hashing tables exist in the application database. `public.users` stores application profile metadata linked to `auth.users` via UUID foreign key.
3. **Dual Independent Player Authentication:** For multiplayer matches, both Player 1 and Player 2 authenticate independently using their own Supabase Auth credentials. Each player receives an independent Supabase-issued access token (JWT).
4. **Anti-Impersonation & Server-Derived Participants:** Match participant IDs are **derived directly by the backend from verified JWT subject claims (`sub`)**, not blindly accepted from client-submitted JSON body fields. Player 1 cannot impersonate Player 2 or commit fraudulent matches.
5. **Reputable Java JWT Library Verification:** Cryptographic token verification in `StandaloneWebServer.java` uses an industry-standard, battle-tested Java JWT library (e.g. `com.auth0:java-jwt` with `com.auth0:jwks-rsa`). Custom cryptographic parsing or verification from scratch is strictly prohibited.
6. **JWKS-Based Asymmetric Verification as Standard:** `StandaloneWebServer.java` verifies asymmetric signatures (RS256/ES256) using Supabase's published JSON Web Key Set (JWKS).
7. **Strict Algorithm Pinning (No Arbitrary Fallback):** Arbitrary JWT algorithm fallback is strictly forbidden to prevent algorithm confusion attacks (CVE-2015-9235). Symmetric HS256 is supported only under explicit, isolated configuration. `alg: none` is rejected unconditionally.
8. **Rigorous Token Claims Validation:** Every protected request requires cryptographic validation of signature, issuer (`iss`), audience (`aud`), expiration (`exp`), and subject user ID (`sub`).
9. **In-Memory Token Lifecycle (Zero Disk Persistence):** The Java Swing desktop application stores access and refresh tokens exclusively in memory (`UserSession` singleton). Tokens are never written to disk, JSON, text, or configuration files. Users log in fresh upon launching the application.
10. **Modern API Key Terminology:** Strict separation between the public **Supabase Publishable Key** (`SUPABASE_PUBLISHABLE_KEY` / anon key) and the private **Supabase Secret Key** (`SUPABASE_SECRET_KEY` / service-role key).
11. **Zero Credential Exposure in Desktop App:** The Java Swing desktop client **NEVER** contains PostgreSQL database passwords, connection strings, or Supabase secret keys.
12. **RLS vs. Direct JDBC Disconnect Recognized:** Manually validating a JWT in `StandaloneWebServer.java` does **NOT** automatically enforce PostgreSQL Row Level Security (RLS) when executing direct JDBC queries via the transaction pooler. The backend application server acts as the authoritative policy enforcement point (PEP) using parameterized queries.
13. **Supabase PostgreSQL as Sole Database:** Supabase PostgreSQL (accessed via Supavisor Transaction Pooler on Port 6543 with `prepareThreshold=0`) is the only permanent database.
14. **No SQLite Production Fallback:** SQLite will not serve as a production fallback. If Supabase is unreachable in production, the application fails fast with clear diagnostics.
15. **Isolated Offline Demo Functionality:** Offline demonstration runs operate in an ephemeral sandbox and do not commit records to permanent leaderboard or match history tables.
16. **Honest Sanity Checks:** Score and duration validation are documented and enforced as **basic payload sanity checks**, not as complete server-authoritative anti-cheat.

---

## 2. Final Architecture Decision Table

| # | Architecture Domain | Final Approved Decision | Security & Operational Rationale |
| :---: | :--- | :--- | :--- |
| **1** | **Authentication Provider** | **Supabase Auth (`auth.users`) Only** | Centralizes identity management. Managed hashing (BCrypt/Argon2), brute-force throttling, and token lifecycle managed by Supabase. |
| **2** | **Application User Storage** | **`public.users` Profile Table (UUID FK)** | `public.users` holds `id UUID REFERENCES auth.users(id)`, `username`, and metadata. Zero passwords or password hashes stored in application tables. |
| **3** | **Client Access Tokens** | **Supabase-Issued Access Tokens (JWT)** | Universal authentication format across both web browser and desktop Swing clients. |
| **4** | **JWT Verification Library** | **Reputable Java JWT Library (`com.auth0`)** | Industry-standard cryptographic library (`java-jwt` + `jwks-rsa`). Prohibits rolling custom crypto from scratch. Eliminates parsing and signature verification vulnerabilities. |
| **5** | **Token Signing Standard** | **Asymmetric JWKS (RS256 / ES256) Preferred** | Aligns with modern Supabase security architecture. Public keys retrieved from Supabase JWKS endpoint; private keys never leave Supabase Auth. |
| **6** | **Legacy Symmetric Signing** | **Strictly Guarded HS256 Mode** | Supported only if explicitly configured for older projects (`JWT_VERIFICATION_MODE=legacy_hs256`). Completely isolated from asymmetric verification to prevent algorithm confusion. |
| **7** | **Algorithm Confusion Guard** | **Strict Algorithm Pinning (No Dynamic Fallback)** | Rejects tokens with `alg: "none"`, mismatched algorithms, or public-key-as-HMAC-secret attacks (mitigating CVE-2015-9235). |
| **8** | **Claims Validation Checklist** | **Full 5-Point Claims Validation** | Cryptographically validates: 1) Signature, 2) `iss` (project URL), 3) `aud` ("authenticated"), 4) `exp` (> current epoch), 5) `sub` (valid UUID). |
| **9** | **Two-Player Authorization** | **Dual Independent Authentication Required** | Both Player 1 and Player 2 must log in with their own Supabase Auth accounts. The client presents both tokens (`Authorization` and `X-Player2-Authorization`). |
| **10** | **Anti-Impersonation** | **Server-Derived Match Participants** | The server derives `player1_user_id = jwt1.sub` and `player2_user_id = jwt2.sub`. Verifies `jwt1.sub != jwt2.sub`. Client cannot forge or spoof participant IDs. |
| **11** | **Key Nomenclature** | **Publishable Key vs. Secret Key** | Standardized on modern terminology: `SUPABASE_PUBLISHABLE_KEY` (client-safe) vs. `SUPABASE_SECRET_KEY` (server-only). |
| **12** | **Desktop Network Client** | **`java.net.http.HttpClient` (Java 11+)** | Built into modern JDK (Java 17/27). Zero external dependencies, async HTTP/2, native JSON payload streaming. |
| **13** | **Desktop Database Access** | **Restricted to REST API Only** | Java Swing client holds **zero database credentials**. Decompiling desktop `.class` bytecode reveals no passwords or secret keys. |
| **14** | **Desktop Token Lifecycle** | **In-Memory Storage Only (Zero Disk Files)** | Tokens exist strictly in heap memory during runtime (`UserSession`). No refresh tokens written to `.json`, `.txt`, or properties. Re-login required upon app launch. |
| **15** | **RLS vs. JDBC Enforcement** | **Application-Layer Authorization Perimeter** | Direct JDBC via port 6543 runs as `postgres` role (bypassing RLS or evaluating `auth.uid()` as NULL). Backend gateway MUST enforce tenant isolation in SQL. |
| **16** | **Permanent Database** | **Supabase PostgreSQL (Port 6543)** | Single source of truth shared by Website and Java Swing. Accessed via Supavisor Transaction Pooler with `prepareThreshold=0` and `sslmode=require`. |
| **17** | **SQLite Role** | **Temporary Development Backup Only** | Completely excluded from production runtime. Preserved locally during transition; never used as a production failover. |
| **18** | **Offline Game Matches** | **Ephemeral Local Demo Mode** | Disconnected matches are flagged as `DEMO_OFFLINE` and do not write to or distort ranked leaderboards. |
| **19** | **Anti-Cheat Scope** | **Basic Payload Sanity Checks** | Mathematically bounds scores ($\le 720$) and durations ($5\text{s} \le t \le 900\text{s}$). Honest demarcation: not full authoritative server anti-cheat. |

---

## 3. System Architecture & Dual-Token Trust Boundaries

```
+───────────────────────────────────────────────────────────────────────────────────────────────────+
|                                  UNTRUSTED CLIENT PERIMETER                                       |
|                                                                                                   |
|    +─────────────────────────────────────────────+   +─────────────────────────────────────────+  |
|    |          HTML5 / Canvas Web Client          |   |        Java Swing Desktop Client        |  |
|    |      (Browser: localhost:8080 / CDN)        |   |       (TwoPlayerLoginFrame / Lobby)     |  |
|    |   Holds: SUPABASE_PUBLISHABLE_KEY only      |   |   Holds: SUPABASE_PUBLISHABLE_KEY only  |  |
|    |   Dual In-Memory Sessions (P1 + P2)         |   |   Dual In-Memory Sessions (P1 + P2)     |  |
|    +──────────────────────┬──────────────────────+   +────────────────────┬────────────────────+  |
|                           │                                               │                       |
|         Player 1 Login    │ HTTPS (Supabase Auth API)                     │ HTTPS (GoTrue API)    |
|         Player 2 Login    │ POST /auth/v1/token?grant_type=password       │ Independent logins    |
|                           ▼                                               ▼                       |
|    +───────────────────────────────────────────────────────────────────────────────────────────+  |
|    |               SUPABASE AUTH SERVICE (GoTrue Managed Identity Provider)                    |  |
|    |  - Authenticates Player 1 -> Issues JWT_P1 (claims: sub=UUID_P1, aud=authenticated)       |  |
|    |  - Authenticates Player 2 -> Issues JWT_P2 (claims: sub=UUID_P2, aud=authenticated)       |  |
|    |  - Signs JWTs using Supabase project asymmetric private key (RS256 / ES256)               |  |
|    +──────────────────────┬───────────────────────────────────────────────┬────────────────────+  |
|                           │ Tokens held IN-MEMORY ONLY                    │ Tokens IN-MEMORY   |
|                           ▼                                               ▼                       |
+───────────────────────────┼───────────────────────────────────────────────┼───────────────────────+
                            │                                               │
============================│===============================================│========================
TRUST BOUNDARY: REQUESTS    │ Headers:                                      │
ENTER SECURED BACKEND       │   Authorization: Bearer <JWT_P1>              │
                            │   X-Player2-Authorization: Bearer <JWT_P2>    │
                            ▼                                               ▼
=====================================================================================================
+───────────────────────────────────────────────────────────────────────────────────────────────────+
|                       SECURE APPLICATION GATEWAY: StandaloneWebServer.java                        |
|                                                                                                   |
|    SECRETS HELD STRICTLY SERVER-SIDE:                                                             |
|      - SUPABASE_SECRET_KEY (Service-role administrative access; never sent to clients)             |
|      - DATABASE_URL / SUPABASE_DB_PASSWORD (Port 6543 pooler credentials)                         |
|      - JWT_SECRET (Only if legacy HS256 mode is explicitly enabled)                               |
|                                                                                                   |
|    1. REPUTABLE JAVA JWT VERIFICATION ENGINE (com.auth0:java-jwt + jwks-rsa):                     |
|       - Fetches & caches JWKS from: https://<REF>.supabase.co/auth/v1/.well-known/jwks.json       |
|       - Cryptographically verifies JWT_P1 using verified public key (RS256/ES256)                 |
|       - Cryptographically verifies JWT_P2 using verified public key (RS256/ES256)                 |
|       - Algorithm Pinning: Enforces expected algorithm; unconditionally blocks "alg: none"        |
|       - 5-Point Claims Validation for BOTH tokens: Signature, iss, aud, exp, sub                  |
|                                                                                                   |
|    2. DUAL-PLAYER PARTICIPANT DERIVATION & ANTI-IMPERSONATION:                                    |
|       - Server extracts: player1_user_id = JWT_P1.getSubject()                                    |
|       - Server extracts: player2_user_id = JWT_P2.getSubject()                                    |
|       - Anti-Self-Play Check: player1_user_id != player2_user_id (rejects if identical)           |
|       - Participant IDs written to SQL are 100% DERIVED FROM TOKENS (ignoring client spoofing)     |
|       - Neither player can commit a match result without the active co-authentication of opponent |
|                                                                                                   |
|    3. BASIC PAYLOAD SANITY CHECKS:                                                                |
|       - Canonical roster guard: character_id IN (1, 2, 3, 4, 5, 6)                                |
|       - Mathematical score bounds: 0 <= score <= 720 (max 240/round * 3 rounds)                   |
|       - Duration bounds: 5s <= duration <= 900s                                                   |
|       - Round count & winner alignment (best of 3 requires 2-3 rounds; winner has >= 2 wins)      |
+───────────────────────────────────────────────────────────────────────────────────────────────────+
                                                  │
                                                  │ PostgreSQL JDBC (org.postgresql.Driver)
                                                  │ Port 6543 (Supavisor Transaction Pooler)
                                                  │ Connection params: sslmode=require & prepareThreshold=0
                                                  │ Role: postgres (Full DB Privileges)
                                                  ▼
+───────────────────────────────────────────────────────────────────────────────────────────────────+
|                                  SUPABASE MANAGED CLOUD POSTGRESQL                                |
|                                                                                                   |
|    SHARED DATA STORE (Used by both Web Arena and Java Swing desktop application):                 |
|      ├── auth.users (Managed by Supabase Auth — emails, encrypted passwords, token lifecycle)     |
|      ├── public.users (id UUID PRIMARY KEY REFERENCES auth.users, username, created_at)          |
|      ├── public.characters (Canonical 6 archetypes: Warrior, Berserker, Ninja, etc.)              |
|      ├── public.matches (match_id, match_date, player1_user_id, player2_user_id, winner_id...)    |
|      ├── public.match_players (bridge table linking users, character archetype, final scores)     |
|      └── public.match_rounds (granular round telemetry, scores, durations)                        |
|                                                                                                   |
|    POSTGRESQL ROW LEVEL SECURITY (RLS):                                                           |
|      - Enabled on all tables.                                                                     |
|      - Directly protects browser PostgREST requests when using client-side SDK.                   |
|      - Inactive / bypassed for direct JDBC gateway queries (Application-Layer PEP above enforces) |
+───────────────────────────────────────────────────────────────────────────────────────────────────+
```

---

## 4. Supabase API Key Management & Secret Isolation

To comply with current Supabase security standards and prevent credential leakage, API keys and credentials are categorized into two strict tiers:

### 4.1 Publishable Key (Client-Safe)
* **Variable Name:** `SUPABASE_PUBLISHABLE_KEY` (legacy name: `SUPABASE_ANON_KEY`).
* **Format:** Modern Supabase publishable keys begin with `sbp_...` or represent the standard anonymous token.
* **Distribution:** Embedded in frontend JavaScript client configuration and Java Swing desktop application configuration (`client.properties` or environment).
* **Allowed Scope:**
  - Authenticating both Player 1 and Player 2 against Supabase Auth endpoints (`/auth/v1/signup`, `/auth/v1/token`).
  - Read-only queries to public views (e.g. public leaderboard, character roster) via PostgREST when RLS permits.
* **Security Guard:** Never grants administrative privileges. Protected by PostgreSQL Row Level Security (RLS) on client-facing PostgREST endpoints.

### 4.2 Secret Key & Database Credentials (Server-Side Only)
* **Variable Names:**
  - `SUPABASE_SECRET_KEY` (legacy name: `SUPABASE_SERVICE_ROLE_KEY`, often formatted as `sb_sk_...`).
  - `SUPABASE_DB_PASSWORD` / `DATABASE_URL` (PostgreSQL direct connection string).
  - `JWT_SECRET` (HMAC secret, only if legacy symmetric verification is active).
* **Distribution:** **STRICTLY SERVER-SIDE.** Stored exclusively in server environment variables (e.g. backend `.env` or deployment secret store).
* **Absolute Prohibition:**
  - **NEVER** package `SUPABASE_SECRET_KEY` or `SUPABASE_DB_PASSWORD` into client-side web JavaScript files.
  - **NEVER** compile `SUPABASE_SECRET_KEY` or database passwords into Java Swing `.class` or `.jar` binaries (where simple decompilation via `javap` or CFR would expose them).
  - **NEVER** commit live keys to Git repositories.

---

## 5. Cryptographic JWT Verification Specification (`StandaloneWebServer.java`)

`StandaloneWebServer.java` serves as the secure gateway. It must cryptographically verify every Supabase Auth JWT before authorizing match writes or private data reads.

### 5.1 Reputable Java JWT Library Integration
In accordance with approved architectural decisions, custom cryptographic parsing or manual signature verification from scratch is **strictly prohibited**. The server utilizes an established, industry-standard library:
* **Selected Libraries:**
  - `com.auth0:java-jwt` (JWT decoding, algorithm enforcement, claims verification).
  - `com.auth0:jwks-rsa` (RFC 7517 compliant JWKS fetching, caching, and public key provisioning).
* **Security Benefits:**
  - Battle-tested parsing immune to malformed Base64, header injection, or encoding exploits.
  - Automatic RSA/ECDSA signature validation using Java Cryptography Architecture (JCA).
  - Standardized handling of JWKS Key ID (`kid`) rotation.

### 5.2 Asymmetric JWKS Verification Workflow (Primary Standard)
Supabase projects sign JWTs using asymmetric cryptography (**RS256** or **ES256**). The public keys are published at the project's well-known JWKS endpoint:
```text
https://<PROJECT_REF>.supabase.co/auth/v1/.well-known/jwks.json
```

**Implementation Workflow in `StandaloneWebServer.java`:**
```java
// 1. Configure thread-safe JWKS provider with caching and rate limiting
JwkProvider jwkProvider = new JwkProviderBuilder("https://<PROJECT_REF>.supabase.co/auth/v1")
    .cached(10, 24, TimeUnit.HOURS)       // Cache up to 10 keys for 24 hours
    .rateLimited(10, 1, TimeUnit.MINUTES)  // Rate limit: max 10 requests per minute to prevent DoS
    .build();

// 2. Extract Key ID and algorithm from token header
DecodedJWT unverifiedJwt = JWT.decode(tokenString);
String kid = unverifiedJwt.getKeyId();
String alg = unverifiedJwt.getAlgorithm();

// 3. Strict Algorithm Pinning: Reject unexpected algorithms unconditionally
if (!"RS256".equals(alg) && !"ES256".equals(alg)) {
    throw new SecurityException("Invalid algorithm: " + alg + ". Only RS256/ES256 permitted.");
}

// 4. Retrieve public key from provider and configure pinned verifier
Jwk jwk = jwkProvider.get(kid);
Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

// 5. Build strict verifier validating issuer, audience, and expiration
JWTVerifier verifier = JWT.require(algorithm)
    .withIssuer("https://<PROJECT_REF>.supabase.co/auth/v1")
    .withAudience("authenticated")
    .acceptLeeway(30) // 30-second clock skew tolerance
    .build();

// 6. Cryptographically verify signature and claims
DecodedJWT verifiedJwt = verifier.verify(tokenString);
UUID userId = UUID.fromString(verifiedJwt.getSubject());
```

### 5.3 Legacy Symmetric Verification (Guarded HS256 Mode)
For legacy Supabase projects utilizing symmetric HMAC-SHA256 signing:
* **Activation Rule:** Active **ONLY** when the gateway environment explicitly sets:
  ```properties
  JWT_VERIFICATION_MODE=legacy_hs256
  JWT_SECRET=<SUPABASE_PROJECT_JWT_SECRET>
  ```
* **Strict Algorithm Pinning:** The gateway configures `Algorithm.HMAC256(jwtSecret)` and checks that the token header explicitly specifies `alg == "HS256"`. Any token specifying `RS256`, `ES256`, or `none` is immediately rejected.

### 5.4 Prevention of Algorithm Confusion Attacks (CVE-2015-9235)
To prevent algorithm confusion attacks where an attacker signs a token using an asymmetric public key as an HMAC secret:
1. **No Dynamic Switching:** The server must **never** determine whether to verify via HMAC or RSA based on the token's incoming `alg` header.
2. **Strict Mode Pinning:** The verification algorithm is fixed by server configuration (`jwks_asymmetric` by default; `legacy_hs256` only if explicitly opted in).
3. **Unconditional `alg: none` Rejection:** Tokens with `alg: "none"`, empty `alg`, or missing headers are unconditionally rejected by the verifier.

### 5.5 Complete 5-Point Claims Validation Checklist
Once the cryptographic signature is verified, the gateway validates the following claims in the payload:

| Claim | Full Name | Required Value / Check | Failure Response |
| :---: | :--- | :--- | :--- |
| **`sig`** | Cryptographic Signature | Validated against JWKS public key or pinned HMAC secret | `HTTP 401 Unauthorized (Invalid Signature)` |
| **`iss`** | Issuer | Must exactly equal `https://<PROJECT_REF>.supabase.co/auth/v1` | `HTTP 401 Unauthorized (Invalid Issuer)` |
| **`aud`** | Audience | Must equal `"authenticated"` | `HTTP 401 Unauthorized (Invalid Audience)` |
| **`exp`** | Expiration Time | Current Unix epoch must be `< exp` (allow max 30s clock skew) | `HTTP 401 Unauthorized (Token Expired)` |
| **`sub`** | Subject (User ID) | Must parse as a valid RFC 4122 UUID; extracted as authenticated user | `HTTP 401 Unauthorized (Malformed Subject UUID)` |

---

## 6. Two-Player Authorization, Anti-Impersonation & RLS Disconnect

A local 2D fighting game played on a single screen presents unique security challenges when committing ranked multiplayer matches. The architecture enforces strict dual-player authorization and anti-impersonation controls:

### 6.1 Two-Player Independent Authentication Workflow
1. **Pre-Match Lobby (Web & Desktop):**
   - Player 1 enters their Supabase email and password -> Client calls Supabase Auth `POST /auth/v1/token` -> Receives `JWT_P1`.
   - Player 2 enters their Supabase email and password -> Client calls Supabase Auth `POST /auth/v1/token` -> Receives `JWT_P2`.
   - Both tokens are held in-memory during the session.
2. **Match Execution:** Combat simulation executes locally across Player 1 (WASD) and Player 2 (Arrow keys).
3. **Match Submission Request:** When the match concludes, the client submits the result payload to `StandaloneWebServer.java` (:8080) with dual authentication headers:
   ```http
   POST /api/matches HTTP/1.1
   Host: localhost:8080
   Authorization: Bearer <JWT_P1>
   X-Player2-Authorization: Bearer <JWT_P2>
   Content-Type: application/json

   {
     "player1_character_id": 1,
     "player2_character_id": 3,
     "player1_score": 210,
     "player2_score": 180,
     "winner_player": 1,
     "duration_seconds": 45,
     "rounds": [...]
   }
   ```

### 6.2 Anti-Impersonation & Participant Derivation
To prevent Player 1 from impersonating Player 2, forging opponent records, or farming arbitrary accounts:
1. **Dual Verification:** `StandaloneWebServer.java` independently verifies both `JWT_P1` and `JWT_P2` through the 5-point claims checklist.
2. **Server-Derived Participant IDs:**
   ```java
   UUID player1Id = UUID.fromString(verifiedJwtP1.getSubject());
   UUID player2Id = UUID.fromString(verifiedJwtP2.getSubject());
   ```
   The client-submitted JSON payload **does not dictate participant user IDs**. The server populates `player1_user_id` and `player2_user_id` in the database transaction strictly using `player1Id` and `player2Id`.
3. **Distinct Player Validation (Anti-Self-Play):**
   ```java
   if (player1Id.equals(player2Id)) {
       throw new SecurityException("Player 1 and Player 2 must be distinct authenticated accounts.");
   }
   ```
4. **Co-Authorization Guarantee:** Because committing a match requires valid signatures from both accounts, neither player can write match history or ranked statistics for an opponent without that opponent's active authentication.

### 6.3 The PostgreSQL RLS vs. Direct JDBC Disconnect
1. **How Direct JDBC Operates:** When `StandaloneWebServer.java` connects to Supabase PostgreSQL over Port 6543 (Supavisor Transaction Pooler) using `org.postgresql.Driver`, it connects under a single PostgreSQL database user (`postgres`).
2. **The Pooler Reality:** The Supavisor transaction pooler operates at the raw TCP/PostgreSQL protocol layer. It has **no awareness of HTTP headers or client JWTs**.
3. **The Security Impact:** In direct JDBC connections:
   - `auth.uid()` evaluates to `NULL`.
   - The `postgres` role typically possesses the `BYPASSRLS` attribute or full table ownership, meaning **PostgreSQL table-level RLS policies are NOT automatically enforced on direct JDBC queries**.
4. **Application-Layer Enforcement:** Because direct JDBC bypasses RLS, **`StandaloneWebServer.java` is the authoritative policy enforcement point (PEP)**:
   - Private profile reads (`GET /api/player/profile`) bind SQL strictly to the caller: `SELECT ... FROM public.users WHERE id = ?` with parameter = `jwt.getSubject()`.
   - Ranked match commits bind SQL strictly to the two verified tokens (`player1Id` and `player2Id`).

---

## 7. Desktop Session Persistence & Token Lifecycle

To protect user accounts and prevent token theft in shared desktop environments (e.g. university computer labs):

### 7.1 In-Memory Only Token Storage
* **Rule:** Access tokens (`access_token`) and refresh tokens (`refresh_token`) received from Supabase Auth are held **exclusively in volatile Java heap memory** inside the `UserSession` singleton.
* **Prohibition:** Tokens are **NEVER** persisted to disk, plaintext files, JSON documents (e.g. `session.json`), Java `.properties` files, or the Windows Registry.
* **Justification:** Desktop computers in student labs or shared personal devices are vulnerable to local file inspection. Storing long-lived refresh tokens in unencrypted files allows unauthorized parties to compromise user accounts.

### 7.2 Session Lifecycle & Re-Login
1. **Application Launch:** When the Java Swing desktop application is opened, no stored session exists. Both Player 1 and Player 2 must enter their credentials to play ranked matches.
2. **Session Expiry:** Supabase access tokens are valid for **1 hour** (3600 seconds). If a desktop session remains open beyond 1 hour, the in-memory refresh token is used via `POST /auth/v1/token?grant_type=refresh_token` to obtain a fresh access token without user interruption.
3. **Application Exit:** Upon closing the Java Swing application, all token references are discarded from memory and garbage collected.

---

## 8. Supavisor Transaction Pooler (Port 6543) Technical Specifications

When connecting from Java via PostgreSQL JDBC, the connection string and pooling characteristics must strictly satisfy Supavisor's transactional requirements:

### 8.1 Connection String Specification
```text
jdbc:postgresql://<PROJECT_REF>.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0
```

### 8.2 The Prepared Statement Rule: `prepareThreshold=0`
* **Root Cause:** In transaction pooling mode, the pooler assigns an arbitrary backend PostgreSQL connection for each transaction. Named prepared statements (`PREPARE S_1 ...`) are stored in server session memory. When the next transaction runs on a different backend connection, reusing `S_1` causes `ERROR: prepared statement "S_1" does not exist`.
* **The Fix:** Setting `prepareThreshold=0` instructs the PostgreSQL JDBC driver to never generate named server-side prepared statements. Instead, queries are executed using the direct protocol or unnamed statements, ensuring 100% execution stability across pooled connections.

### 8.3 Disallowed Session Operations
* **No `LISTEN / NOTIFY`:** PubSub connections require persistent state; they must not be executed over port 6543.
* **No Session-Level Temporary Tables:** `CREATE TEMP TABLE` persists only for the lifetime of a single transaction in transaction pooling.
* **No Persistent Connection-Level Settings:** Any `SET timezone` or session variable must use `SET LOCAL` within a transaction block.
* *Application Audit:* Clash of Bytes relies solely on discrete atomic `SELECT`, `INSERT`, and `UPDATE` statements within standard transactions. Zero disallowed operations exist.

---

## 9. Basic Payload Sanity Checks vs. Authoritative Anti-Cheat

To maintain academic rigor and honesty, the validation rules enforced by `StandaloneWebServer.java` are classified as **Basic Payload Sanity Checks**, not full authoritative anti-cheat:

### 9.1 Enforced Sanity Checks
1. **Self-Play Prevention:** `player1Id != player2Id` enforced from verified tokens.
2. **Canonical Roster Guard:** `character_id IN (1, 2, 3, 4, 5, 6)`. Rejects unknown or hacked character IDs.
3. **Mathematical Score Upper Bounds:**
   * Maximum score per round: $\text{Base Health} (140) + \text{Perfect Time Bonus} (100) = 240\text{ pts}$.
   * Maximum match score (best of 3 rounds): $240 \times 3 = 720\text{ pts}$.
   * **Rule:** Any submitted score $> 750$ (allowing 30 pts grace for edge ticks) or $< 0$ is rejected with `HTTP 400 Bad Request`.
4. **Round & Winner Alignment:**
   * Best-of-3 format requires exactly 2 or 3 completed rounds.
   * `winner_player` (1 or 2) must match the player who won $\ge 2$ rounds.
5. **Duration Verification:** $5\text{ seconds} \le \text{duration} \le 900\text{ seconds}$ (15 minutes). Matches shorter than 5 seconds are rejected as impossible.

### 9.2 Boundary of Protection (Academic Honesty)
Because the combat engine runs on the client (browser Canvas or local desktop window), these sanity checks guard against malformed, corrupt, or blatantly forged payloads. They do **not** prevent a player from modifying client physics locally (e.g. infinite health or auto-dodging) while submitting plausible-looking final scores. True anti-cheat requires deterministic server-authoritative simulation, which is intentionally outside the scope of a 2D local multiplayer academic game.

---

## 10. Offline Demonstration Safeguards

To allow grading, academic presentations, and testing in offline or air-gapped lab environments:
1. **Automatic Offline Detection:** When `StandaloneWebServer.java` or Java Swing cannot reach Supabase Auth or PostgreSQL during initialization, it enters **Offline Demo Mode**.
2. **Ephemeral Local State:** Match simulation, combat, character selection, and local sound effects remain fully functional.
3. **Leaderboard Protection:** Demo matches are flagged in-memory as `DEMO_OFFLINE`. The application **does not commit demo records to the permanent Supabase match history or global leaderboard**.
4. **UI Banner:** A prominent status indicator ("OFFLINE DEMO MODE — SCORES NOT RECORDED") is displayed on both the web interface and Java Swing UI.

---

## 11. Final Pre-Implementation Security & Implementation Checklist

Before executing the implementation phase, verify every item against this security checklist:

- [ ] **[C-1] Secret Isolation:** `SUPABASE_SECRET_KEY` and `SUPABASE_DB_PASSWORD` exist **only** in backend environment configuration; neither appears in frontend JS, Java Swing bytecode, or Git history.
- [ ] **[C-2] Key Nomenclature:** Frontend web and desktop Swing clients use **only** the `SUPABASE_PUBLISHABLE_KEY`.
- [ ] **[C-3] Reputable JWT Library:** `StandaloneWebServer.java` uses `com.auth0:java-jwt` and `com.auth0:jwks-rsa` for cryptographic verification; no custom crypto implemented from scratch.
- [ ] **[C-4] Asymmetric Default & Algorithm Pinning:** Verifier defaults to JWKS RS256/ES256, enforces expected algorithm, and unconditionally rejects `alg: "none"` and mismatched algorithms (mitigating CVE-2015-9235).
- [ ] **[C-5] Full Claims Checklist:** The gateway validates signature, `iss`, `aud`, `exp`, and `sub` before authorizing any protected route.
- [ ] **[C-6] JWKS DoS Throttling:** JWKS provider is configured with rate-limiting to prevent cache-busting denial of service.
- [ ] **[C-7] Dual-Player Independent Authentication:** Match submissions require two independent valid Supabase Auth JWTs (`Authorization` and `X-Player2-Authorization`).
- [ ] **[C-8] Anti-Impersonation & Participant Derivation:** The server derives `player1_user_id` and `player2_user_id` exclusively from verified token `sub` claims, asserting `player1Id != player2Id`.
- [ ] **[C-9] In-Memory Desktop Token Lifecycle:** Java Swing stores tokens exclusively in memory; zero refresh tokens persisted to disk files.
- [ ] **[C-10] Single Shared Database:** Both Web Arena and Java Swing share the exact same Supabase project, authentication system, and PostgreSQL database.
- [ ] **[C-11] Pooler Configuration:** The PostgreSQL JDBC URL specifies `sslmode=require` and `prepareThreshold=0` on port 6543.
- [ ] **[C-12] Honest Sanity Checks:** Match score ($\le 720$) and duration ($5\text{s} \le t \le 900\text{s}$) bounds are enforced as sanity checks without claiming authoritative anti-cheat.
- [ ] **[C-13] Offline Containment:** Offline demo mode executes ephemerally without polluting ranked cloud tables.

---

## 12. Final Decision Resolution Status: Zero Unresolved Questions

All implementation-level decisions have been formally finalized and approved:

| # | Technical Decision Domain | Approved Final Decision | Resolution Status |
| :---: | :--- | :--- | :---: |
| **1** | **JWT Verification Mechanism** | Use reputable Java JWT library (`com.auth0:java-jwt` + `com.auth0:jwks-rsa`). Do not implement custom crypto from scratch. Pin permitted algorithms. Support legacy HS256 only when explicitly configured. | **CLOSED & APPROVED** |
| **2** | **Two-Player Match Authorization** | Both Player 1 and Player 2 authenticate independently via Supabase Auth. Backend validates both tokens. Participant IDs are derived strictly from token `sub` claims (anti-impersonation). Rejects self-play. | **CLOSED & APPROVED** |
| **3** | **Desktop Token Lifecycle** | In-memory token storage only in `UserSession`. Zero disk persistence. Both players must log in again upon launching the desktop application. | **CLOSED & APPROVED** |
| **4** | **Platform Unification** | Both HTML/JS Website and Java Swing desktop application use the exact same Supabase project, Auth provider, and PostgreSQL database. | **CLOSED & APPROVED** |

**Architectural Questions Remaining:** **NONE (0).** The architecture specification is 100% frozen, complete, and fully ready for implementation.
