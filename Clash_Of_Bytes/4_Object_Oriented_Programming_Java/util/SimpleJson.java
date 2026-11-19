package util;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON parser and serializer for Supabase REST & Auth payloads.
 * Supports nested JSON objects, arrays, strings, numbers, booleans, and null values.
 */
public class SimpleJson {

    public static Object parse(String json) {
        if (json == null) return null;
        String s = json.trim();
        if (s.isEmpty()) return null;
        int[] pos = new int[]{0};
        skipWhitespace(s, pos);
        if (pos[0] >= s.length()) return null;
        char c = s.charAt(pos[0]);
        if (c == '{') {
            return parseObject(s, pos);
        } else if (c == '[') {
            return parseArray(s, pos);
        } else {
            return parseValue(s, pos);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object res = parse(json);
        if (res instanceof Map) {
            return (Map<String, Object>) res;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object res = parse(json);
        if (res instanceof List) {
            return (List<Object>) res;
        }
        return Collections.emptyList();
    }

    private static Map<String, Object> parseObject(String s, int[] pos) {
        Map<String, Object> map = new LinkedHashMap<>();
        pos[0]++; // skip '{'
        skipWhitespace(s, pos);
        if (pos[0] < s.length() && s.charAt(pos[0]) == '}') {
            pos[0]++;
            return map;
        }

        while (pos[0] < s.length()) {
            skipWhitespace(s, pos);
            if (pos[0] >= s.length() || s.charAt(pos[0]) != '"') break;
            String key = parseString(s, pos);
            skipWhitespace(s, pos);
            if (pos[0] >= s.length() || s.charAt(pos[0]) != ':') break;
            pos[0]++; // skip ':'
            skipWhitespace(s, pos);
            Object value = parseValue(s, pos);
            map.put(key, value);

            skipWhitespace(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') {
                pos[0]++;
                continue;
            } else if (pos[0] < s.length() && s.charAt(pos[0]) == '}') {
                pos[0]++;
                break;
            } else {
                break;
            }
        }
        return map;
    }

    private static List<Object> parseArray(String s, int[] pos) {
        List<Object> list = new ArrayList<>();
        pos[0]++; // skip '['
        skipWhitespace(s, pos);
        if (pos[0] < s.length() && s.charAt(pos[0]) == ']') {
            pos[0]++;
            return list;
        }

        while (pos[0] < s.length()) {
            skipWhitespace(s, pos);
            Object val = parseValue(s, pos);
            list.add(val);

            skipWhitespace(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') {
                pos[0]++;
                continue;
            } else if (pos[0] < s.length() && s.charAt(pos[0]) == ']') {
                pos[0]++;
                break;
            } else {
                break;
            }
        }
        return list;
    }

    private static Object parseValue(String s, int[] pos) {
        skipWhitespace(s, pos);
        if (pos[0] >= s.length()) return null;
        char c = s.charAt(pos[0]);
        if (c == '"') {
            return parseString(s, pos);
        } else if (c == '{') {
            return parseObject(s, pos);
        } else if (c == '[') {
            return parseArray(s, pos);
        } else if (c == 't' || c == 'T') {
            if (s.regionMatches(true, pos[0], "true", 0, 4)) {
                pos[0] += 4;
                return Boolean.TRUE;
            }
        } else if (c == 'f' || c == 'F') {
            if (s.regionMatches(true, pos[0], "false", 0, 5)) {
                pos[0] += 5;
                return Boolean.FALSE;
            }
        } else if (c == 'n' || c == 'N') {
            if (s.regionMatches(true, pos[0], "null", 0, 4)) {
                pos[0] += 4;
                return null;
            }
        } else if (c == '-' || (c >= '0' && c <= '9')) {
            return parseNumber(s, pos);
        }

        // Fallback: read until delimiter
        int start = pos[0];
        while (pos[0] < s.length() && ",}] \t\r\n".indexOf(s.charAt(pos[0])) < 0) {
            pos[0]++;
        }
        return s.substring(start, pos[0]);
    }

    private static String parseString(String s, int[] pos) {
        pos[0]++; // skip opening '"'
        StringBuilder sb = new StringBuilder();
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]++);
            if (c == '"') {
                return sb.toString();
            } else if (c == '\\' && pos[0] < s.length()) {
                char esc = s.charAt(pos[0]++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos[0] + 4 <= s.length()) {
                            String hex = s.substring(pos[0], pos[0] + 4);
                            pos[0] += 4;
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                            } catch (NumberFormatException e) {
                                sb.append('?');
                            }
                        }
                        break;
                    default:
                        sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Object parseNumber(String s, int[] pos) {
        int start = pos[0];
        boolean isFloat = false;
        if (s.charAt(pos[0]) == '-') pos[0]++;
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c >= '0' && c <= '9') {
                pos[0]++;
            } else if (c == '.' || c == 'e' || c == 'E' || c == '+') {
                isFloat = true;
                pos[0]++;
            } else {
                break;
            }
        }
        String numStr = s.substring(start, pos[0]);
        try {
            if (isFloat) {
                return Double.parseDouble(numStr);
            } else {
                long val = Long.parseLong(numStr);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            }
        } catch (NumberFormatException e) {
            return numStr;
        }
    }

    private static void skipWhitespace(String s, int[] pos) {
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                pos[0]++;
            } else {
                break;
            }
        }
    }

    // Helper accessors
    public static String getString(Map<String, Object> map, String key) {
        return getString(map, key, null);
    }

    public static String getString(Map<String, Object> map, String key, String def) {
        if (map == null) return def;
        Object val = map.get(key);
        if (val == null) return def;
        return String.valueOf(val);
    }

    public static long getLong(Map<String, Object> map, String key, long def) {
        if (map == null) return def;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        if (val != null) {
            try { return Long.parseLong(val.toString()); } catch (Exception ignored) {}
        }
        return def;
    }

    public static int getInt(Map<String, Object> map, String key, int def) {
        if (map == null) return def;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val != null) {
            try { return Integer.parseInt(val.toString()); } catch (Exception ignored) {}
        }
        return def;
    }

    public static double getDouble(Map<String, Object> map, String key, double def) {
        if (map == null) return def;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val != null) {
            try { return Double.parseDouble(val.toString()); } catch (Exception ignored) {}
        }
        return def;
    }

    public static boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        if (map == null) return def;
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val != null) {
            return Boolean.parseBoolean(val.toString());
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getObject(Map<String, Object> map, String key) {
        if (map == null) return Collections.emptyMap();
        Object val = map.get(key);
        if (val instanceof Map) return (Map<String, Object>) val;
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getArray(Map<String, Object> map, String key) {
        if (map == null) return Collections.emptyList();
        Object val = map.get(key);
        if (val instanceof List) return (List<Object>) val;
        return Collections.emptyList();
    }

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
