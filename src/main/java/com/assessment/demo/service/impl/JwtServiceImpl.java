package com.assessment.demo.service.impl;

import com.assessment.demo.entity.Token;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {
    @Value("${spring.jwt.secretKey}")
    private String secretKey;
    @Value("${spring.jwt.tokenLifespan}")
    private int tokenLifespan;
    @Value("${spring.jwt.refreshTokenLifespan}")
    private int refreshTokenLifespan;

    @Autowired
    private TokenRepository tokenRepository;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> Claims = new HashMap<>();
        return createToken(Claims, userDetails, false);
    }

    @Override
    public String createToken(Map<String, Object> Claims, UserDetails userDetails, boolean isRefresh) {
        int lifespan = tokenLifespan;
        if (isRefresh)
            lifespan = refreshTokenLifespan;

        // Build a JWT with custom claims and subject set to the username
        return Jwts.builder()
                .setClaims(Claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date()) // Token creation time
                // Token expiration time: 7 days from the current time
                // Date(new Date()) will return amount of ms that represent for that day
                .setExpiration(new Date(System.currentTimeMillis() + lifespan))
                .signWith(getSignKey(), SignatureAlgorithm.HS256) // Sign the token with a secret key
                .compact(); // Compact the JWT into its final form
    }

    // Retrieve the secret key for signing the JWT
    private Key getSignKey() {
        // Note: In a production scenario, consider storing the key securely, e.g., in application properties or environment variables
        byte[] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }

    // Extract a specific claim from the JWT using a provided resolver function
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        // Extract claims from the JWT
        final Claims claims = extractAllClaims(token);
        // Apply the resolver function to retrieve the desired claim
        return claimsResolvers.apply(claims);
    }

    // Extract all claims from the JWT
    private Claims extractAllClaims(String token) {
        // Parse and verify the JWT, then retrieve its body (claims)
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract the username claim from the JWT, it is placed at the subject of the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Check if the token is valid by comparing the username and verifying expiration
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Check if the extracted username matches the username from UserDetails and the token is not expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Check if the token is expired by comparing the expiration time with the current time
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public void saveCompressedToken(String tokenData, String refreshTokenData) {
        try {
            // Save the compressed Token into the database
            Token tokenEntity = new Token(compressData(tokenData), compressData(refreshTokenData));
            // Convert Instant to LocalDateTime for better readability (optional)

            Date currentDate = new Date(tokenLifespan);
            Date expirationDate = new Date(currentDate.getTime() + refreshTokenLifespan);

            tokenEntity.setTokenExpireAt(currentDate);
            tokenEntity.setRefreshTokenExpireAt(expirationDate);
            tokenRepository.save(tokenEntity);

        } catch (IOException e) {
            // Handle decompression exception
            log.error("An error occurred:", e);
        }
    }

    public String getDecompressedToken(UUID tokenId) {
        // Retrieve the token entity from the database
        Token tokenEntity = tokenRepository.findById(tokenId).orElse(null);
        if (tokenEntity != null) {
            // Decompress the token and refresh token data using GZIP
            String tokenData = decompressData(tokenEntity.getCompressedTokenData());
            String refreshTokenData = decompressData(tokenEntity.getCompressedRefreshTokenData());
            return tokenData + " " + refreshTokenData;
        }
        return null;
    }

    private byte[] compressData(String data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data.getBytes());
        }
        return byteArrayOutputStream.toByteArray();
    }

    private String decompressData(byte[] compressedData) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            return byteArrayOutputStream.toString();
        } catch (IOException e) {
            // Handle decompression exception
            log.error("An error occurred:", e);
        }
        return null;
    }
}
