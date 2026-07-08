package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.jpa.repositories.ReinforcementRepository;
import ashzuu.project.copupitreback.mapper.ReinforcementMapper;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.ReinforcementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les points d'accès (endpoints) pour la gestion des renforts.
 * <p>
 * Ce contrôleur gère les requêtes liées aux annonces de renfort pour des musiciens ou orchestres.
 * </p>
 */
@RestController
@RequestMapping("/reinforcements")
public class ReinforcementController {

    /**
     * Service gérant la logique métier liée aux renforts.
     */
    private final ReinforcementService service;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param service le service des renforts
     */
    public ReinforcementController(ReinforcementService service){
        this.service = service;
    }

    /**
     * Point d'accès pour récupérer toutes les annonces de renfort.
     * <p>
     * Cette méthode répond aux requêtes HTTP pour lister toutes les annonces de renfort actives.
     * </p>
     *
     * @return une {@link ResponseEntity} contenant la liste de toutes les annonces de renfort sous forme de DTO avec un code HTTP 200 (OK)
     */
    @RequestMapping("/all")
    public ResponseEntity<List<ReinforcementAnnouncementDTO>> getAllAnnouncements(){
        return ResponseEntity
                .ok(this.service.getAllAnnouncements());
    }

    @RequestMapping("/{id}/responses")
    public ResponseEntity<List<ReinforcementResponseDTO>> getAllResponses(@PathVariable String id){
        return ResponseEntity.ok(this.service.getAllResponses(id));
    }

    @PostMapping("/save")
    public ResponseEntity<ReinforcementAnnouncementDTO> saveAnnouncement(
            @AuthenticationPrincipal ashzuu.project.copupitreback.security.UserDetailsImpl userDetails,
            @RequestBody ReinforcementAnnouncementDTO dto) {
        return ResponseEntity.ok(this.service.saveAnnouncement(dto, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        this.service.deleteAnnouncement(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
