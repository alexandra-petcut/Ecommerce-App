package com.example.ecommerce_app.service;

import com.example.ecommerce_app.exception.UnauthorizedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public String generateToken(Long userId, String email, String role) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = Instant.now().plusMillis(jwtExpirationMs).getEpochSecond();

            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", email);
            payload.put("userId", userId);
            payload.put("role", role);
            payload.put("iat", now);
            payload.put("exp", exp);

            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String signature = sign(encodedHeader + "." + encodedPayload);

            return encodedHeader + "." + encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public Map<String, Object> validateTokenAndGetClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new UnauthorizedException("Invalid token format");
            }

            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String expectedSignature = sign(header + "." + payload);
            if (!expectedSignature.equals(signature)) {
                throw new UnauthorizedException("Invalid token signature");
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(payload);
            Map<String, Object> claims = objectMapper.readValue(decodedPayload, new TypeReference<>() {});

            Number exp = (Number) claims.get("exp");
            long now = Instant.now().getEpochSecond();

            if (exp == null || exp.longValue() < now) {
                throw new UnauthorizedException("Token has expired");
            }

            return claims;
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(signatureBytes);
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
