package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.auth.AuthResponse;
import ashzuu.project.copupitreback.dto.auth.LoginRequest;
import ashzuu.project.copupitreback.dto.auth.RegisterRequest;
import ashzuu.project.copupitreback.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>AuthController</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> Contrôleur REST exposant les endpoints pour l'inscription et la connexion.</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Permettre aux clients (comme l'application Angular) d'envoyer des requêtes HTTP POST pour s'authentifier ou créer un compte.</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle fait appel à AuthService pour traiter les requêtes et renvoie les réponses sous format JSON.</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder().token(null).username(e.getMessage()).build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(AuthResponse.builder().token(null).username("Identifiants incorrects").build());
        }
    }
}
