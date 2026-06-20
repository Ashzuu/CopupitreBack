package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    /**
     * Constructeur de la classe UserController.
     * <p>
     * Rôle : Initialise le contrôleur avec le service utilisateur fourni.
     * <p>
     * Liens : Utilise {@link UserService} pour gérer les opérations liées aux utilisateurs.
     * @param service Service gérant les opérations liées aux utilisateurs
     */
    public UserController(UserService service){
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails){
        if(userDetails == null){
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(this.service.getByUsername(userDetails.getUsername()));
    }
}
