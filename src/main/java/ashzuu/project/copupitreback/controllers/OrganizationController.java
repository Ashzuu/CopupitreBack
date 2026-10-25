package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.dto.organization.AddAdminRequest;
import ashzuu.project.copupitreback.dto.organization.OrganizationDTO;
import ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {
    private final OrganizationService service;

    public OrganizationController(OrganizationService service){
        this.service = service;
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrganizationDTO>> getAll(){
        return ResponseEntity.ok(this.service.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<OrganizationDTO> getOrganizationById(@PathVariable String id) {
        return ResponseEntity.ok(this.service.getById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrganizationDTO>> getOrganizations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if(userDetails == null){
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity
                .status(200)
                .body(this.service.getOrganizationFromUser(userDetails));
    }

    @GetMapping("/{id}/reinforcements")
    public ResponseEntity<List<ReinforcementAnnouncementDTO>> getAnnouncementsOnOrganization(@PathVariable String id){
        return ResponseEntity.ok(this.service.getAnnouncementsOnOrganization(id));
    }

    @GetMapping("/{id}/equipment-requests")
    public ResponseEntity<List<EquipmentRequestDTO>> getEquipmentRequestOnOrganization(@PathVariable String id){
        return ResponseEntity.ok(this.service.getEquipmentRequestOnOrganization(id));
    }

    @GetMapping("/{id}/annual-needs")
    public ResponseEntity<List<PermanentAnnouncementDTO>> getAnnualNeedsOnOrganization(@PathVariable String id){
        return ResponseEntity.ok(this.service.getAnnualNeedsOnOrganization(id));
    }

    @PostMapping("/save")
    public ResponseEntity<OrganizationDTO> createOrganization(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody OrganizationDTO org){
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(this.service.save(org, userDetails));
    }

    /**
     * Ajoute un nouvel administrateur à l'organisation (US-ORG-08).
     *
     * @param id          l'identifiant de l'organisation
     * @param request     la requête contenant le pseudo de l'utilisateur à ajouter
     * @param userDetails l'utilisateur actuellement connecté
     * @return l'organisation mise à jour
     */
    @PostMapping("/{id}/admins")
    public ResponseEntity<OrganizationDTO> addAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AddAdminRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(this.service.addAdmin(id, request.getUsername(), userDetails));
    }

    /**
     * Supprime un administrateur de l'organisation (US-ORG-08).
     *
     * @param id          l'identifiant de l'organisation
     * @param username    le pseudo de l'administrateur à retirer
     * @param userDetails l'utilisateur actuellement connecté
     * @return l'organisation mise à jour
     */
    @DeleteMapping("/{id}/admins/{username:.+}")
    public ResponseEntity<OrganizationDTO> removeAdmin(
            @PathVariable Long id,
            @PathVariable String username,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(this.service.removeAdmin(id, username, userDetails));
    }
}
