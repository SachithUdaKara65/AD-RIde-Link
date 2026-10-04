package com.ridelink.drivervehicle.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void acceptsTokenSignedWithAccountServiceBase64Secret() {
        SecretKey signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String sharedSecret = Encoders.BASE64.encode(signingKey.getEncoded());
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", sharedSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86_400_000L);

        String token = Jwts.builder()
                .setSubject("account-user-123")
                .claim("email", "driver@example.com")
                .claim("role", "DRIVER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("account-user-123", jwtUtil.extractUsername(token));
        assertEquals("DRIVER", jwtUtil.extractRole(token));
    }
}
