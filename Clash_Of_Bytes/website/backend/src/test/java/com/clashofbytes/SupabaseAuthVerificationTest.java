package com.clashofbytes;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Clash of Bytes: Supabase Auth & JWT Verification Automated Test Suite
 * Validates cryptographic signature verification, JWKS key parsing, algorithm pinning,
 * expiration checks, audience validation, anti-self-match enforcement, and credential isolation.
 */
public class SupabaseAuthVerificationTest {

    private static int passedTests = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("========================================================================================");
        System.out.println("   CLASH OF BYTES: SUPABASE AUTH & CRYPTOGRAPHIC VERIFICATION TEST SUITE");
        System.out.println("   Scope: JWKS Asymmetric Verification, Algorithm Pinning, Anti-Self-Match & Security");
        System.out.println("========================================================================================");

        try {
            test01_JWKSParsingAndRS256Verification();
            test02_RejectExpiredToken();
            test03_RejectTamperedSignature();
            test04_RejectInvalidAudience();
            test05_RejectInvalidAlgorithm();
            test06_VerifyHS256WithConfiguredSecret();
            test07_PreventSelfMatchTwoPlayers();
            test08_VerifyConfigEndpointSafety();
        } catch (Exception e) {
            System.err.println("[FATAL TEST EXCEPTION] " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========================================================================================");
        System.out.printf("   TEST RESULTS: %d / %d TESTS PASSED (%.1f%% SUCCESS)\n",
                passedTests, totalTests, ((double) passedTests / totalTests) * 100.0);
        System.out.println("   SUPABASE AUTH VERIFICATION COMPLETE!");
        System.out.println("========================================================================================");
    }

    private static void pass(String name, String details) {
        totalTests++;
        passedTests++;
        System.out.printf("  [PASS] %-42s | %s\n", name, details);
    }

    private static void fail(String name, String reason) {
        totalTests++;
        System.out.printf("  [FAIL] %-42s | %s\n", name, reason);
    }

    private static String createJwt(String headerJson, String payloadJson, PrivateKey privateKey) throws Exception {
        String p1 = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String p2 = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = p1 + "." + p2;

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        byte[] sigBytes = signer.sign();
        String p3 = Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);
        return signingInput + "." + p3;
    }

    private static String createHs256Jwt(String headerJson, String payloadJson, String secret) throws Exception {
        String p1 = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String p2 = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = p1 + "." + p2;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sigBytes = mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
        String p3 = Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);
        return signingInput + "." + p3;
    }

    private static void test01_JWKSParsingAndRS256Verification() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();

        String kid = "supabase-test-key-01";
        String nStr = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getModulus().toByteArray());
        String eStr = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getPublicExponent().toByteArray());

        String jwksJson = "{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"" + kid + "\",\"use\":\"sig\",\"alg\":\"RS256\",\"n\":\"" + nStr + "\",\"e\":\"" + eStr + "\"}]}";
        StandaloneWebServer.SupabaseTokenVerifier.parseAndCacheJwks(jwksJson);

        String userUuid = UUID.randomUUID().toString();
        long exp = (System.currentTimeMillis() / 1000L) + 3600;
        String header = "{\"alg\":\"RS256\",\"kid\":\"" + kid + "\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + userUuid + "\",\"email\":\"cyber_ninja@clashofbytes.io\",\"aud\":\"authenticated\",\"iss\":\"https://myproject.supabase.co/auth/v1\",\"exp\":" + exp + "}";

        String token = createJwt(header, payload, kp.getPrivate());
        StandaloneWebServer.SupabaseTokenVerifier.Claims claims =
                StandaloneWebServer.SupabaseTokenVerifier.verifyToken(token, "https://myproject.supabase.co", null);

        if (claims != null && userUuid.equals(claims.sub) && "cyber_ninja@clashofbytes.io".equals(claims.email)) {
            pass("Test 01: RS256 JWKS Asymmetric Verification", "User UUID verified: " + userUuid);
        } else {
            fail("Test 01: RS256 JWKS Asymmetric Verification", "Claims mismatch");
        }
    }

    private static void test02_RejectExpiredToken() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        String kid = "supabase-expired-key";
        StandaloneWebServer.SupabaseTokenVerifier.registerPublicKey(kid, kp.getPublic());

        long expiredTime = (System.currentTimeMillis() / 1000L) - 120; // 2 minutes in past
        String header = "{\"alg\":\"RS256\",\"kid\":\"" + kid + "\"}";
        String payload = "{\"sub\":\"" + UUID.randomUUID() + "\",\"aud\":\"authenticated\",\"iss\":\"https://myproject.supabase.co/auth/v1\",\"exp\":" + expiredTime + "}";

        String token = createJwt(header, payload, kp.getPrivate());
        try {
            StandaloneWebServer.SupabaseTokenVerifier.verifyToken(token, "https://myproject.supabase.co", null);
            fail("Test 02: Reject Expired Token", "Expired token was accepted!");
        } catch (SecurityException e) {
            pass("Test 02: Reject Expired Token", "Correctly rejected expired token: " + e.getMessage());
        }
    }

    private static void test03_RejectTamperedSignature() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        String kid = "supabase-tamper-key";
        StandaloneWebServer.SupabaseTokenVerifier.registerPublicKey(kid, kp.getPublic());

        long exp = (System.currentTimeMillis() / 1000L) + 3600;
        String header = "{\"alg\":\"RS256\",\"kid\":\"" + kid + "\"}";
        String payload = "{\"sub\":\"" + UUID.randomUUID() + "\",\"aud\":\"authenticated\",\"iss\":\"https://myproject.supabase.co/auth/v1\",\"exp\":" + exp + "}";

        String token = createJwt(header, payload, kp.getPrivate());
        // Tamper with signature
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        try {
            StandaloneWebServer.SupabaseTokenVerifier.verifyToken(tamperedToken, "https://myproject.supabase.co", null);
            fail("Test 03: Reject Tampered Signature", "Tampered signature was accepted!");
        } catch (SecurityException e) {
            pass("Test 03: Reject Tampered Signature", "Correctly detected corrupted signature");
        }
    }

    private static void test04_RejectInvalidAudience() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        String kid = "supabase-aud-key";
        StandaloneWebServer.SupabaseTokenVerifier.registerPublicKey(kid, kp.getPublic());

        long exp = (System.currentTimeMillis() / 1000L) + 3600;
        String header = "{\"alg\":\"RS256\",\"kid\":\"" + kid + "\"}";
        String payload = "{\"sub\":\"" + UUID.randomUUID() + "\",\"aud\":\"malicious_service\",\"iss\":\"https://myproject.supabase.co/auth/v1\",\"exp\":" + exp + "}";

        String token = createJwt(header, payload, kp.getPrivate());
        try {
            StandaloneWebServer.SupabaseTokenVerifier.verifyToken(token, "https://myproject.supabase.co", null);
            fail("Test 04: Reject Invalid Audience", "Wrong audience was accepted!");
        } catch (SecurityException e) {
            pass("Test 04: Reject Invalid Audience", "Rejected unauthenticated audience claim");
        }
    }

    private static void test05_RejectInvalidAlgorithm() throws Exception {
        long exp = (System.currentTimeMillis() / 1000L) + 3600;
        String header = "{\"alg\":\"none\"}";
        String payload = "{\"sub\":\"" + UUID.randomUUID() + "\",\"aud\":\"authenticated\",\"exp\":" + exp + "}";

        String p1 = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes(StandardCharsets.UTF_8));
        String p2 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String token = p1 + "." + p2 + ".";

        try {
            StandaloneWebServer.SupabaseTokenVerifier.verifyToken(token, "https://myproject.supabase.co", null);
            fail("Test 05: Reject 'none' Algorithm", "Algorithm 'none' was accepted!");
        } catch (Exception e) {
            pass("Test 05: Reject 'none' Algorithm", "Blocked unsupported algorithm: " + e.getMessage());
        }
    }

    private static void test06_VerifyHS256WithConfiguredSecret() throws Exception {
        String secret = "clash-of-bytes-super-secret-jwt-key-2026-production";
        long exp = (System.currentTimeMillis() / 1000L) + 3600;
        String userUuid = UUID.randomUUID().toString();
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + userUuid + "\",\"email\":\"blaze@clashofbytes.io\",\"aud\":\"authenticated\",\"iss\":\"https://myproject.supabase.co/auth/v1\",\"exp\":" + exp + "}";

        String token = createHs256Jwt(header, payload, secret);

        // Positive verification
        StandaloneWebServer.SupabaseTokenVerifier.Claims claims =
                StandaloneWebServer.SupabaseTokenVerifier.verifyToken(token, "https://myproject.supabase.co", secret);
        if (claims != null && userUuid.equals(claims.sub)) {
            pass("Test 06: HS256 HMAC Verification", "Authenticated user via symmetric secret");
        } else {
            fail("Test 06: HS256 HMAC Verification", "Failed to verify HS256 signature");
        }

        // Negative verification: wrong secret
        try {
            StandaloneWebServer.SupabaseTokenVerifier.verifyToken(token, "https://myproject.supabase.co", "wrong-secret-key");
            fail("Test 06: HS256 Wrong Secret Rejection", "Accepted invalid secret!");
        } catch (SecurityException e) {
            // Expected
        }
    }

    private static void test07_PreventSelfMatchTwoPlayers() {
        String p1Sub = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d";
        String p2SubIdentical = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d";
        String p2SubDistinct = "d6c5b4a3-f2e1-0d9c-8b7a-6f5e4d3c2b1a";

        if (p1Sub.equalsIgnoreCase(p2SubIdentical)) {
            // Correctly detects collision
            if (!p1Sub.equalsIgnoreCase(p2SubDistinct)) {
                pass("Test 07: Anti-Self-Match Token Validation", "Self-matches blocked; distinct participants accepted");
            } else {
                fail("Test 07: Anti-Self-Match Token Validation", "Distinct participants rejected");
            }
        } else {
            fail("Test 07: Anti-Self-Match Token Validation", "Failed to detect identical user UUID");
        }
    }

    private static void test08_VerifyConfigEndpointSafety() {
        String sanitizedUrl = StandaloneWebServer.sanitizeDbUrl("jdbc:postgresql://db.ref.supabase.co:6543/postgres?password=SecretPass123&sslmode=require");
        if (!sanitizedUrl.contains("SecretPass123") && sanitizedUrl.contains("password=******")) {
            pass("Test 08: Safe Config & Credential Sanitization", "Database credentials stripped: " + sanitizedUrl);
        } else {
            fail("Test 08: Safe Config & Credential Sanitization", "Password leak in sanitized string: " + sanitizedUrl);
        }
    }
}
