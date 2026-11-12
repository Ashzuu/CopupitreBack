package ashzuu.project.copupitreback.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * <h1>SecurityConfig</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> Configuration principale de Spring Security pour l'application.</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Définir les règles de sécurité, désactiver CSRF (pour JWT), configurer CORS pour Angular, et spécifier quelles routes sont publiques ou protégées.</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle utilise JwtAuthenticationFilter et configure globalement l'application.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String allowedOriginsProperty;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200,http://localhost:4000,http://localhost}") String allowedOriginsProperty) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOriginsProperty = allowedOriginsProperty;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Endpoints d'authentification et logs
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/logs/**", "/logs").permitAll()

                // Endpoints spécifiques authentifiés sous /organizations
                .requestMatchers(HttpMethod.GET, "/organizations/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/organizations/*/is-subscribed").authenticated()
                .requestMatchers(HttpMethod.GET, "/organizations/*/absences/**").authenticated()
                // Consultation publique des fiches organisations (liste et détail par ID ou slug, musiciens, absences)
                .requestMatchers(HttpMethod.GET, "/organizations/**").permitAll()

                // Endpoints spécifiques authentifiés sous /reinforcements
                .requestMatchers(HttpMethod.GET, "/reinforcements/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/reinforcements/my-applications").authenticated()
                .requestMatchers(HttpMethod.GET, "/reinforcements/*/responses").authenticated()
                // Consultation publique des annonces de renfort
                .requestMatchers(HttpMethod.GET, "/reinforcements/all").permitAll()

                // Endpoints spécifiques authentifiés sous /annual-needs
                .requestMatchers(HttpMethod.GET, "/annual-needs/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/annual-needs/my-applications").authenticated()
                // Consultation publique des besoins annuels
                .requestMatchers(HttpMethod.GET, "/annual-needs/**").permitAll()

                // Endpoints spécifiques authentifiés sous /equipments
                .requestMatchers(HttpMethod.GET, "/equipments/me").authenticated()
                // Consultation publique du matériel
                .requestMatchers(HttpMethod.GET, "/equipments/**").permitAll()

                // Toutes les autres routes (création, modification, suppression, messagerie, etc.) nécessitent l'authentification
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED))
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
