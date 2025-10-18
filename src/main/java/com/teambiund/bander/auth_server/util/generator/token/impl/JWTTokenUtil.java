package com.teambiund.bander.auth_server.util.generator.token.impl;

import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.util.generator.token.TokenUtil;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTTokenUtil implements TokenUtil {


    @Value("${security.jwt.access-token-expire-time}")
    private Long accessTokenTTL;
    @Value("${security.jwt.refresh-token-expire-time}")
    private Long refreshTokenTTL;


    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    // Minimal JSON builder to avoid adding dependencies
    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(entry.getKey())).append('"').append(':');
            Object v = entry.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v.toString());
            } else {
                sb.append('"').append(escape(v.toString())).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    // Minimal JSON parser for flat objects with string/number/boolean/null values
    private static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> map = new HashMap<>();
        if (json == null) return map;
        String s = json.trim();
        if (s.length() < 2 || s.charAt(0) != '{' || s.charAt(s.length() - 1) != '}') return map;
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) return map;
        int i = 0;
        while (i < s.length()) {
            // parse key
            if (s.charAt(i) != '"') break;
            int keyStart = ++i;
            StringBuilder keySb = new StringBuilder();
            boolean escaped = false;
            for (; i < s.length(); i++) {
                char c = s.charAt(i);
                if (escaped) {
                    keySb.append(c);
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    i++;
                    break;
                } else {
                    keySb.append(c);
                }
            }
            // skip colon
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            if (i >= s.length() || s.charAt(i) != ':') break;
            i++;
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            // parse value
            Object value;
            if (i < s.length() && s.charAt(i) == '"') {
                // string
                i++;
                StringBuilder valSb = new StringBuilder();
                boolean esc = false;
                for (; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (esc) {
                        valSb.append(c);
                        esc = false;
                    } else if (c == '\\') {
                        esc = true;
                    } else if (c == '"') {
                        i++;
                        break;
                    } else {
                        valSb.append(c);
                    }
                }
                value = valSb.toString();
            } else {
                // literal (number, boolean, null)
                int start = i;
                while (i < s.length() && ",}".indexOf(s.charAt(i)) == -1) i++;
                String literal = s.substring(start, i).trim();
                if (literal.equals("null")) {
                    value = null;
                } else if (literal.equals("true") || literal.equals("false")) {
                    value = Boolean.valueOf(literal);
                } else {
                    try {
                        if (literal.contains(".") || literal.contains("e") || literal.contains("E")) {
                            value = Double.valueOf(literal);
                        } else {
                            value = Long.valueOf(literal);
                        }
                    } catch (NumberFormatException ex) {
                        value = literal;
                    }
                }
            }
            map.put(keySb.toString(), value);
            // skip spaces and comma
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            if (i < s.length() && s.charAt(i) == ',') {
                i++;
                while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            }
        }
        return map;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String generateAccessToken(String userId, Role role, String deviceId) {
        return buildToken(userId, role, Duration.ofMinutes(accessTokenTTL), deviceId);
    }

    @Override
    public String generateRefreshToken(String userId, Role role, String deviceId) {
        return buildToken(userId, role, Duration.ofMinutes(refreshTokenTTL), deviceId);
    }

    // ---------------------- Decode & Validate ----------------------
    @Override
    public boolean isValid(String token) {
        try {
            String[] parts = splitToken(token);
            String unsigned = parts[0] + "." + parts[1];
            String expectedSig = sign(unsigned, jwtSecret);
            if (!constantTimeEquals(expectedSig, parts[2])) return false;
            Map<String, Object> payload = parsePayload(parts[1]);
            Object expObj = payload.get("exp");
            if (expObj instanceof Number) {
                long exp = ((Number) expObj).longValue() + 30;
                long now = Instant.now().getEpochSecond();
                return now < exp;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String extractUserId(String token) {
        Map<String, Object> payload = safePayload(token);
        Object sub = payload.get("sub");
        return sub != null ? sub.toString() : null;
    }

    @Override
    public Role extractRole(String token) {
        Map<String, Object> payload = safePayload(token);
        Object role = payload.get("role");
        if (role == null) return null;
        try {
            return Role.valueOf(role.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public String extractDeviceId(String token) {
        Map<String, Object> payload = safePayload(token);
        Object deviceId = payload.get("deviceId");
        return deviceId != null ? deviceId.toString() : null;
    }

    @Override
    public long extractExpiration(String token) {
        Map<String, Object> payload = safePayload(token);
        Object expObj = payload.get("exp");
        if (expObj instanceof Number) {
            long exp = ((Number) expObj).longValue();
            long now = Instant.now().getEpochSecond();
            return (exp - now);
        }
        return 0;
    }

    private Map<String, Object> safePayload(String token) {
        try {
            String[] parts = splitToken(token);
            return parsePayload(parts[1]);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String[] splitToken(String token) {
        if (token == null) throw new IllegalArgumentException("token is null");
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("invalid JWT format");
        return parts;
    }

    private Map<String, Object> parsePayload(String payloadB64) {
        byte[] jsonBytes = base64UrlDecode(payloadB64);
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        return parseJsonObject(json);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    // ---------------------- Build (Encode) ----------------------
    private String buildToken(String userId, Role role, Duration ttl, String deviceId) {
        long iat = Instant.now().getEpochSecond();
        long exp = Instant.now().plus(ttl).getEpochSecond();

        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userId);
        payload.put("role", role != null ? role.name() : null);
        payload.put("deviceId", deviceId);
        payload.put("iat", iat);
        payload.put("exp", exp);

        String headerJson = toJson(header);
        String payloadJson = toJson(payload);

        String headerEncoded = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadEncoded = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

        String unsignedToken = headerEncoded + "." + payloadEncoded;
        String signature = sign(unsignedToken, jwtSecret);

        return unsignedToken + "." + signature;
    }

    private String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(sig);
        } catch (Exception e) {
            // Fallback to unsigned token if signing fails (should not happen)
            return "";
        }
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString().substring(0, 4);
    }

}

