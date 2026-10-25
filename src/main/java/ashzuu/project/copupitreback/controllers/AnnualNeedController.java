package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ApplyRequest;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.dto.reinforcement.UpdateApplicationStatusRequest;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.AnnualNeedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les points d'accès pour les annonces de musiciens permanents (besoins annuels).
 */
@RestController
@RequestMapping("/annual-needs")
public class AnnualNeedController {

    private final AnnualNeedService service;

    public AnnualNeedController(AnnualNeedService service) {
        this.service = service;
    }

    /**
     * Point d'accès pour récupérer l'ensemble des annonces de recrutement permanent.
     */
    @GetMapping("/all")
    public ResponseEntity<List<PermanentAnnouncementDTO>> getAllAnnouncements() {
        return ResponseEntity.ok(this.service.getAllPermanentAnnouncements());
    }

    /**
     * Point d'accès pour récupérer les besoins annuels des organisations administrées par l'utilisateur connecté.
     */
    @GetMapping("/me")
    public ResponseEntity<List<PermanentAnnouncementDTO>> getMyAnnouncements(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(this.service.getMyPermanentAnnouncements(userDetails));
    }

    /**
     * Point d'accès pour récupérer les candidatures faites par l'utilisateur connecté aux postes permanents.
     */
    @GetMapping("/my-applications")
    public ResponseEntity<List<ReinforcementResponseDTO>> getMyApplications(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(this.service.getMyApplications(userDetails));
    }

    /**
     * Point d'accès pour postuler à un besoin permanent (annuel).
     */
    @PostMapping("/{id}/apply")
    public ResponseEntity<ReinforcementResponseDTO> applyToAnnualNeed(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody(required = false) ApplyRequest applyRequest) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String message = (applyRequest != null) ? applyRequest.getMessage() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.service.applyToAnnualNeed(id, userDetails, message));
    }

    /**
     * Point d'accès pour valider ou refuser une candidature permanente.
     */
    @RequestMapping(value = "/{id}/applications/{applicationId}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ReinforcementResponseDTO> updateApplicationStatus(
            @PathVariable Long id,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(this.service.updateApplicationStatus(id, applicationId, request.getStatus(), userDetails));
    }
}
