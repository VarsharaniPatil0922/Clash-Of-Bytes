package service;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Configuration holder for Supabase Cloud REST & Auth endpoints.
 * Prioritizes:
 * 1. Environment variables (SUPABASE_URL, SUPABASE_ANON_KEY, SUPABASE_PUBLISHABLE_KEY)
 * 2. External configuration file (supabase.properties)
 * 3. Default verified project configuration
 *
 * NOTE: Strictly client-safe. NEVER stores or exposes Supabase secret/service-role keys
 * or PostgreSQL database passwords.
 */
public class SupabaseConfig {

    private static final String DEFAULT_URL = "https://fzhtgfpdvefzjcqceqym.supabase.co";
    private static final String DEFAULT_ANON_KEY = "sb_publishable_bTuqcR6mt62hKhuR9mcHWQ_Fm7cdsWe";

    private static String url = DEFAULT_URL;
    private static String anonKey = DEFAULT_ANON_KEY;

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        // 1. Environment variables and System Properties
        String envUrl = System.getenv("SUPABASE_URL");
        if (envUrl == null || envUrl.trim().isEmpty()) {
            envUrl = System.getProperty("supabase.url");
        }

        String envKey = System.getenv("SUPABASE_ANON_KEY");
        if (envKey == null || envKey.trim().isEmpty()) {
            envKey = System.getenv("SUPABASE_PUBLISHABLE_KEY");
        }
        if (envKey == null || envKey.trim().isEmpty()) {
            envKey = System.getProperty("supabase.key");
        }

        // 2. Search for supabase.properties file
        String[] propPaths = {
                "supabase.properties",
                "Clash_Of_Bytes/supabase.properties",
                "4_Object_Oriented_Programming_Java/supabase.properties",
                "../supabase.properties"
        };
        for (String path : propPaths) {
            File f = new File(path);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    Properties props = new Properties();
                    props.load(fis);
                    String pUrl = props.getProperty("supabase.url");
                    String pKey = props.getProperty("supabase.anon_key", props.getProperty("supabase.publishable_key"));
                    if (pUrl != null && !pUrl.trim().isEmpty()) envUrl = pUrl.trim();
                    if (pKey != null && !pKey.trim().isEmpty()) envKey = pKey.trim();
                    break;
                } catch (Exception ignored) {}
            }
        }

        // 3. Fallback resolution
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            url = envUrl.trim();
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        }
        if (envKey != null && !envKey.trim().isEmpty()) {
            anonKey = envKey.trim();
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getAnonKey() {
        return anonKey;
    }

    public static String getPublishableKey() {
        return anonKey;
    }

    public static URI getAuthUri(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        return URI.create(url + "/auth/v1" + p);
    }

    public static URI getRestUri(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        return URI.create(url + "/rest/v1" + p);
    }
}
