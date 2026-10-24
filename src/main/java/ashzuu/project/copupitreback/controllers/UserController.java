package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.user.UpdateProfileRequest;
import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST exposant les points d'accès (endpoints) pour la gestion des utilisateurs.
 * <p>
 * Ce contrôleur permet à un utilisateur connecté de récupérer ou mettre à jour ses propres informations de profil.
 * </p>
 */
@RestController
@RequestMapping("/users")
public class UserController {

    /**
     * Service gérant la logique métier liée aux utilisateurs.
     */
    private final UserService service;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param service le service des utilisateurs
     */
    public UserController(UserService service){
        this.service = service;
    }

    /**
     * Point d'accès pour récupérer les informations de profil de l'utilisateur actuellement connecté.
     * <p>
     * Cette méthode utilise l'annotation {@link AuthenticationPrincipal} pour extraire les détails
     * de l'utilisateur authentifié depuis le contexte de sécurité de Spring.
     * </p>
     *
     * @param userDetails les détails de l'utilisateur actuellement authentifié, injectés par Spring Security
     * @return une {@link ResponseEntity} contenant le DTO {@link UserDTO} de l'utilisateur avec un code HTTP 200 (OK),
     *         ou une réponse 401 (Unauthorized) si aucun utilisateur n'est authentifié dans la session
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails){
        if(userDetails == null){
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(this.service.getByUsername(userDetails.getUsername()));
    }

    /**
     * Point d'accès pour mettre à jour les informations de profil de l'utilisateur actuellement connecté.
     *
     * @param userDetails les détails de l'utilisateur actuellement authentifié
     * @param request     les nouvelles données de profil validées
     * @return une {@link ResponseEntity} contenant le {@link UserDTO} mis à jour avec un code HTTP 200 (OK),
     *         ou une réponse 401 (Unauthorized) si aucun utilisateur n'est authentifié dans la session
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateProfileRequest request){
        if(userDetails == null){
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(this.service.updateProfile(userDetails.getUsername(), request));
    }
}
