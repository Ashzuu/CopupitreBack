package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.dto.equipment.UpdateEquipmentStatusRequest;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les points d'accès (endpoints) pour la gestion du matériel (Epic 04).
 */
@RestController
@RequestMapping("/equipments")
public class EquipmentController {

    private final EquipmentService service;

    public EquipmentController(EquipmentService service) {
        this.service = service;
    }

    /**
     * Récupère l'ensemble des demandes d'équipements publiques (US-MAT-02).
     */
    @GetMapping("/requests/all")
    public ResponseEntity<List<EquipmentRequestDTO>> getAll() {
        return ResponseEntity.ok(this.service.getAllRequests());
    }

    /**
     * Récupère les demandes de matériel des organisations administrées par l'utilisateur connecté (US-MAT-03).
     */
    @GetMapping("/me")
    public ResponseEntity<List<EquipmentRequestDTO>> getMyRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(this.service.getMyEquipmentRequests(userDetails));
    }

    /**
     * Récupère les demandes de matériel pour une organisation spécifique.
     */
    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<EquipmentRequestDTO>> getRequestsByOrganization(@PathVariable Long orgId) {
        return ResponseEntity.ok(this.service.getRequestsByOrganization(orgId));
    }

    /**
     * Crée ou modifie une demande de matériel (US-MAT-01).
     */
    @PostMapping("/save")
    public ResponseEntity<EquipmentRequestDTO> saveRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody EquipmentRequestDTO dto) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(this.service.saveRequest(dto, userDetails));
    }

    /**
     * Met à jour le statut d'une demande de matériel (US-MAT-03 / RG-MAT-04).
     */
    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<EquipmentRequestDTO> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateEquipmentStatusRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(this.service.updateStatus(id, request.getStatus(), userDetails));
    }

    /**
     * Supprime ou clôture une demande de matériel (UC-MAT-05 / US-MAT-03).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        this.service.deleteRequest(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
