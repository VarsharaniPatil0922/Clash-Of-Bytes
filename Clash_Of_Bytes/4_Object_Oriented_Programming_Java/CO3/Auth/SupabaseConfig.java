package CO3.Auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration manager for Supabase Cloud Database & Authentication.
 * 
 * Supports:
 * 1. External configuration via 'supabase.properties' file (auto-generated if missing)
 * 2. System Environment variables (SUPABASE_URL, SUPABASE_KEY)
 * 3. Default fallback placeholders
 */
public class SupabaseConfig {

    private static final String CONFIG_FILE = "supabase.properties";

    // Default placeholders (Replace these with your Supabase Project details or edit supabase.properties)
    public static final String DEFAULT_URL = "https://vhxbbfoykbdbypylsapr.supabase.co";
    public static final String DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZoeGJiZm95a2JkYnlweWxzYXByIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM1MTA4NDEsImV4cCI6MjEwOTA4Njg0MX0.3WWXgCwY7HpCdkOfwuHhsQowa-keknVXZ68ii8zGFRk";

    private static String supabaseUrl = DEFAULT_URL;
    private static String supabaseAnonKey = DEFAULT_ANON_KEY;

    static {
        loadConfiguration();
    }

    /**
     * Loads Supabase credentials from supabase.properties, environment, or defaults.
     */
    public static synchronized void loadConfiguration() {
        // 1. Try loading from local properties file
        File propFile = new File(CONFIG_FILE);
        if (!propFile.exists()) {
            propFile = new File("..", CONFIG_FILE);
        }
        if (propFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(propFile)) {
                props.load(in);
                String loadedUrl = props.getProperty("SUPABASE_URL");
                if (loadedUrl == null) loadedUrl = props.getProperty("supabase.url");
                if (loadedUrl != null && !loadedUrl.isBlank()) supabaseUrl = loadedUrl.trim();

                String loadedKey = props.getProperty("SUPABASE_ANON_KEY");
                if (loadedKey == null) loadedKey = props.getProperty("supabase.anon_key");
                if (loadedKey == null) loadedKey = props.getProperty("supabase.publishable_key");
                if (loadedKey != null && !loadedKey.isBlank()) supabaseAnonKey = loadedKey.trim();
                return;
            } catch (IOException ignored) {}
        }

        // 2. Try environment variables
        String envUrl = System.getenv("SUPABASE_URL");
        String envKey = System.getenv("SUPABASE_ANON_KEY");
        if (envKey == null) {
            envKey = System.getenv("SUPABASE_KEY");
        }

        if (envUrl != null && !envUrl.isBlank()) {
            supabaseUrl = envUrl.trim();
        }
        if (envKey != null && !envKey.isBlank()) {
            supabaseAnonKey = envKey.trim();
        }

        // 3. Create template properties file if it does not exist
        if (!propFile.exists()) {
            saveConfiguration(supabaseUrl, supabaseAnonKey);
        }
    }

    /**
     * Saves Supabase credentials to 'supabase.properties'.
     */
    public static synchronized boolean saveConfiguration(String url, String anonKey) {
        supabaseUrl = url != null ? url.trim() : "";
        supabaseAnonKey = anonKey != null ? anonKey.trim() : "";

        Properties props = new Properties();
        props.setProperty("SUPABASE_URL", supabaseUrl);
        props.setProperty("SUPABASE_ANON_KEY", supabaseAnonKey);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Clash of Bytes - Supabase Authentication & Database Configuration\n"
                    + "Get your URL & Anon Key from: https://supabase.com/dashboard/project/_/settings/api");
            return true;
        } catch (IOException e) {
            System.err.println("[SupabaseConfig] Failed to save config: " + e.getMessage());
            return false;
        }
    }

    public static String getSupabaseUrl() {
        return supabaseUrl;
    }

    public static String getSupabaseAnonKey() {
        return supabaseAnonKey;
    }

    /**
     * Checks if real credentials have been configured (i.e. not default dummy values).
     */
    public static boolean isConfigured() {
        return supabaseUrl != null && !supabaseUrl.isBlank() && !supabaseUrl.contains("your-project-id")
                && supabaseAnonKey != null && !supabaseAnonKey.isBlank() && !supabaseAnonKey.contains("your-anon-public-api-key");
    }
}
