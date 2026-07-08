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
 * Contrôleur REST exposant les points d'accès (endpoints) pour l'inscription et la connexion.
 * <p>
 * Ce contrôleur permet aux clients (comme une application frontend) d'envoyer des requêtes HTTP POST
 * pour s'authentifier ou créer un compte au sein de la plateforme.
 * </p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Service gérant la logique métier d'authentification.
     */
    private final AuthService authService;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param authService le service d'authentification
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Point d'accès pour l'inscription d'un nouvel utilisateur.
     * <p>
     * Reçoit une requête POST contenant les détails du nouvel utilisateur, valide ces données,
     * puis appelle le service d'authentification pour créer le compte.
     * </p>
     *
     * @param request l'objet de requête contenant les données d'inscription validées
     * @return une {@link ResponseEntity} contenant la réponse d'authentification (avec jeton JWT) en cas de succès,
     *         ou une réponse d'erreur 400 (Bad Request) si l'inscription échoue
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder().token(null).username(e.getMessage()).build());
        }
    }

    /**
     * Point d'accès pour la connexion d'un utilisateur existant.
     * <p>
     * Reçoit une requête POST contenant les identifiants de l'utilisateur, valide ces données,
     * puis appelle le service d'authentification pour valider les accès.
     * </p>
     *
     * @param request l'objet de requête contenant les identifiants de connexion validés
     * @return une {@link ResponseEntity} contenant la réponse d'authentification (avec jeton JWT) en cas de succès,
     *         ou une réponse d'erreur 401 (Unauthorized) en cas d'identifiants invalides ou d'échec
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(AuthResponse.builder().token(null).username("Identifiants incorrects").build());
        }
    }
}
