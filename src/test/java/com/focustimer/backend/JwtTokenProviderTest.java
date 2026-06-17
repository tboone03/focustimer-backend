package com.focustimer.backend;

import com.focustimer.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    void generateToken_creates_valid_token() {
        String token = jwtTokenProvider.generateToken("alice");
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getUsernameFromToken_roundtrips() {
        String token = jwtTokenProvider.generateToken("bob");
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("bob");
    }

    @Test
    void validateToken_rejects_garbage() {
        assertThat(jwtTokenProvider.validateToken("not.a.token")).isFalse();
    }

    @Test
    void validateToken_rejects_empty() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_rejects_tampered_token() {
        String token = jwtTokenProvider.generateToken("charlie");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }
}
