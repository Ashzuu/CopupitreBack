package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.reinforcement.ApplyRequest;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.dto.reinforcement.UpdateApplicationStatusRequest;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.ReinforcementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les points d'accès (endpoints) pour la gestion des renforts et candidatures.
 */
@RestController
@RequestMapping("/reinforcements")
public class ReinforcementController {

    private final ReinforcementService service;

    public ReinforcementController(ReinforcementService service){
        this.service = service;
    }

    /**
     * Point d'accès pour récupérer toutes les annonces de renfort.
     */
    @GetMapping("/all")
    public ResponseEntity<List<ReinforcementAnnouncementDTO>> getAllAnnouncements(){
        return ResponseEntity.ok(this.service.getAllAnnouncements());
    }

    /**
     * Point d'accès pour récupérer les annonces des organisations administrées par l'utilisateur connecté.
     */
    @GetMapping("/me")
    public ResponseEntity<List<ReinforcementAnnouncementDTO>> getMyAnnouncements(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(this.service.getMyReinforcements(userDetails));
    }

    /**
     * Point d'accès pour récupérer toutes les réponses à une annonce spécifique (US-DASH-04).
     * Réservé aux administrateurs de l'organisation propriétaire.
     */
    @GetMapping("/{id}/responses")
    public ResponseEntity<List<ReinforcementResponseDTO>> getAllResponses(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(this.service.getAllResponses(id, userDetails));
    }

    /**
     * Point d'accès pour récupérer les candidatures faites par l'utilisateur connecté (US-RENF-03).
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
     * Point d'accès pour créer ou modifier une annonce de renfort.
     */
    @PostMapping("/save")
    public ResponseEntity<ReinforcementAnnouncementDTO> saveAnnouncement(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ReinforcementAnnouncementDTO dto) {
        return ResponseEntity.ok(this.service.saveAnnouncement(dto, userDetails));
    }

    /**
     * Point d'accès pour postuler à une annonce de renfort (US-RENF-03).
     */
    @PostMapping("/{id}/apply")
    public ResponseEntity<ReinforcementResponseDTO> applyToAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody(required = false) ApplyRequest applyRequest) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String message = (applyRequest != null) ? applyRequest.getMessage() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.service.applyToAnnouncement(id, userDetails, message));
    }

    /**
     * Point d'accès pour valider ou refuser une candidature (US-RENF-04).
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

    /**
     * Point d'accès pour supprimer une annonce de renfort (US-RENF-05).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        this.service.deleteAnnouncement(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
