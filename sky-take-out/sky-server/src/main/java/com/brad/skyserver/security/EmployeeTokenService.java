package com.brad.skyserver.security;

import constant.JwtClaimsConstant;
import entity.Employee;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class EmployeeTokenService {
    private final JwtEncoder encoder;
    private final long ttl;
    public EmployeeTokenService(JwtEncoder encoder, @Value("${sky.jwt.admin-ttl:7200000}") long ttl) {
        if (ttl <= 0) throw new IllegalArgumentException("JWT 有效期必须大于 0");
        this.encoder = encoder;
        this.ttl = ttl;
    }
    public String createToken(Employee employee) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("sky-admin")
                .subject(employee.getId().toString()).issuedAt(now).expiresAt(now.plusMillis(ttl))
                .claim(JwtClaimsConstant.EMP_ID, employee.getId()).claim("scope", "employee").build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
