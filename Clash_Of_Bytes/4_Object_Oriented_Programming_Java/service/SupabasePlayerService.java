package service;

import model.Character;
import model.PlayerSession;
import util.SimpleJson;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service managing player profile records in public.users and fighter rosters in public.characters
 * via the Supabase PostgREST API.
 */
public class SupabasePlayerService {

    private final HttpClient httpClient;

    public SupabasePlayerService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public SupabasePlayerService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Verifies that the player's profile exists in public.users.
     * If not already created by the auth trigger, creates it using the authenticated player's credentials.
     */
    public boolean ensureProfile(PlayerSession session) {
        if (session == null || session.getUserId() == null) return false;

        try {
            // 1. Check existing profile
            HttpRequest checkReq = HttpRequest.newBuilder()
                    .uri(SupabaseConfig.getRestUri("/users?id=eq." + session.getUserId() + "&select=*"))
                    .header("apikey", SupabaseConfig.getAnonKey())
                    .header("Authorization", "Bearer " + session.getAccessToken())
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> checkResp = httpClient.send(checkReq, HttpResponse.BodyHandlers.ofString());
            if (checkResp.statusCode() >= 200 && checkResp.statusCode() < 300) {
                List<Object> list = SimpleJson.parseArray(checkResp.body());
                if (!list.isEmpty()) {
                    return true; // Profile already exists
                }
            }

            // 2. Insert missing profile
            String jsonPayload = String.format(
                    "{\"id\":\"%s\",\"username\":\"%s\"}",
                    SimpleJson.escape(session.getUserId()),
                    SimpleJson.escape(session.getUsername())
            );

            HttpRequest insertReq = HttpRequest.newBuilder()
                    .uri(SupabaseConfig.getRestUri("/users"))
                    .header("apikey", SupabaseConfig.getAnonKey())
                    .header("Authorization", "Bearer " + session.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("Prefer", "resolution=merge-duplicates")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> insertResp = httpClient.send(insertReq, HttpResponse.BodyHandlers.ofString());
            return insertResp.statusCode() >= 200 && insertResp.statusCode() < 300;
        } catch (Exception e) {
            System.err.println("[SupabasePlayerService] Warning: Failed to ensure profile for " + session.getUsername() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches the canonical 6 fighter characters from public.characters.
     */
    public List<Character> getAllCharacters() {
        List<Character> characters = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(SupabaseConfig.getRestUri("/characters?select=*&order=character_id.asc"))
                    .header("apikey", SupabaseConfig.getAnonKey())
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                List<Object> list = SimpleJson.parseArray(response.body());
                for (Object item : list) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) item;
                        int id = SimpleJson.getInt(map, "character_id", 0);
                        String name = SimpleJson.getString(map, "character_name");
                        String desc = SimpleJson.getString(map, "character_description");
                        String arch = SimpleJson.getString(map, "archetype");
                        double hp = SimpleJson.getDouble(map, "base_hp", 100);
                        double atk = SimpleJson.getDouble(map, "base_attack", 15);
                        double def = SimpleJson.getDouble(map, "base_defense", 5);
                        double spd = SimpleJson.getDouble(map, "speed", 5);

                        characters.add(new Character(id, name, desc, arch, hp, atk, def, spd));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SupabasePlayerService] Warning fetching characters: " + e.getMessage());
        }

        // Fallback default roster if offline
        if (characters.isEmpty()) {
            characters.add(new Character(1, "Ironclad Warrior", "Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.", "WarriorFighter", 115.0, 18.0, 6.0, 5.0));
            characters.add(new Character(2, "Blaze Berserker", "Reckless brawler trading armor defense for devastating fury damage multipliers.", "BerserkerWarrior", 130.0, 22.0, 4.0, 6.5));
            characters.add(new Character(3, "Shadow Ninja", "Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.", "NinjaFighter", 95.0, 16.0, 4.0, 9.0));
            characters.add(new Character(4, "Cyber Brawler", "High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.", "CyberBrawler", 110.0, 19.0, 3.0, 7.0));
            characters.add(new Character(5, "Aura Paladin", "Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.", "PaladinFighter", 120.0, 17.0, 7.5, 4.5));
            characters.add(new Character(6, "Titan Gladiator", "Colosseum champion master combining raw berserker fury with elemental magma strikes.", "HybridGladiator", 140.0, 24.0, 8.0, 4.0));
        }

        return characters;
    }
}
