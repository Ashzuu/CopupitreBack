package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.dto.organization.OrganizationDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.mapper.OrganizationMapper;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import jakarta.websocket.server.PathParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/organizations")
public class OrganizationController {
    private final OrganizationRepository repo;

    public OrganizationController(OrganizationRepository repo){
        this.repo = repo;
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrganizationDTO>> getOrganizations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if(userDetails == null){
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity
                .status(200)
                .body(OrganizationMapper.toDto(
                    this.repo.getAllByUserId(userDetails.getId())
                ));
    }

    @GetMapping("/{id}/reinforcements")
    public ResponseEntity<List<ReinforcementAnnouncementDTO>> getAnnouncementsOnOrganization(@PathVariable String id){
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/{id}/equipment-requests")
    public ResponseEntity<List<EquipmentRequestDTO>> getEquipmentRequestOnOrganization(@PathVariable String id){
        return ResponseEntity.ok(new ArrayList<>());
    }
}
