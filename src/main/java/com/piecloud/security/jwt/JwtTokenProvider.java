package com.piecloud.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "roles";

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    @Autowired
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    protected void init() {
        String secret = Base64.getEncoder().encodeToString(jwtProperties.getSecretKey().getBytes());
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Claims claims = Jwts.claims().setSubject(username);
        claims.put(AUTHORITIES_KEY,
                authorities.stream().
                        map(GrantedAuthority::getAuthority).
                        collect(Collectors.joining(",")));
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getValidityInMs());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                claims.get(AUTHORITIES_KEY).toString()
        );
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            return validateTokenUtil(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);
        }
        return false;
    }

    private boolean validateTokenUtil(String token) throws JwtException, IllegalArgumentException {
        JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
        Jws<Claims> claims = jwtParser.parseClaimsJws(token);
        Date expiration = claims.getBody().getExpiration();
        Date now = new Date();
        return !expiration.before(now);
    }
}
