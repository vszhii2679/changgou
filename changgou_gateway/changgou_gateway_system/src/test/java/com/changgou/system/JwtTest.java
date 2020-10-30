package com.changgou.system;

import com.changgou.system.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JwtTest {
    String jwtToken;

    @Before
    public void testJwtToken() throws Exception {
        jwtToken = JwtUtil.createJWT(UUID.randomUUID().toString(), "test", JwtUtil.JWT_TTL);
        System.out.println("jwtToken = " + jwtToken);
    }

    @Test
    public void testParse() throws Exception {
        Claims claims = JwtUtil.parseJWT(jwtToken);
        System.out.println("claims = " + claims);
    }
}
