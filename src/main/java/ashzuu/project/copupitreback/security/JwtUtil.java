package ashzuu.project.copupitreback.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * <h1>JwtUtil</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> Classe utilitaire pour la gestion des tokens JWT (génération, validation, extraction d'informations).</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Sécuriser les échanges entre le frontend et le backend en fournissant un mécanisme d'authentification sans état (stateless).</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle est utilisée par JwtAuthenticationFilter pour valider les requêtes et par AuthService pour générer les tokens lors du login/register.</p>
 */
@Component
public class JwtUtil {

    // Ideally, this should be injected from properties
    @Value("${jwt.secret:defaultSecretKeyThatIsVeryLongAndSecureEnoughForHS256AlgorithmToBeValid123!}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 1 day in ms
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
