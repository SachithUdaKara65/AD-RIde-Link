package com.ridelink.farepayment.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    @Test
    void validatesTokenSignedWithAccountServiceBase64Secret() {
        SecretKey signingKey = Jwts.SIG.HS256.key().build();
        String sharedSecret = Encoders.BASE64.encode(signingKey.getEncoded());
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", sharedSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86_400_000L);

        String token = Jwts.builder()
                .subject("account-user-123")
                .claim("role", "DRIVER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("account-user-123", jwtUtil.extractUsername(token));
        assertEquals("DRIVER", jwtUtil.extractRole(token));
    }
}
