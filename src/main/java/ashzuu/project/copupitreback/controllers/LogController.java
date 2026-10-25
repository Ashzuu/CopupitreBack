package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.log.ClientLogRequest;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.LogService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Contrôleur REST pour centraliser les logs et erreurs applicatives du frontend
 * dans les fichiers de logs côté serveur, évitant ainsi l'exposition par console.error/console.log.
 * Expose également un point de contrôle santé (Healthcheck HTTP GET) pour Docker et orchestrateurs.
 */
@RestController
@RequestMapping("/logs")
public class LogController {
    private final LogService logService;

    LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Endpoint de vérification de santé HTTP (utilisé notamment par Docker HEALTHCHECK).
     *
     * @return un statut UP indiquant que le service backend est opérationnel
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @PostMapping
    public ResponseEntity<Void> receiveClientLog(
            @Valid @RequestBody ClientLogRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        this.logService.receiveLogClient(request, userDetails);
        return ResponseEntity.accepted().build();
    }
}
