package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.services.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant les points d'accès (endpoints) pour la gestion des équipements.
 * <p>
 * Ce contrôleur gère les requêtes liées aux équipements et à leurs demandes au sein de l'application.
 * </p>
 */
@RestController
@RequestMapping("/equipments")
public class EquipmentController {

    /**
     * Service gérant la logique métier liée aux équipements.
     */
    private final EquipmentService service;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param service le service des équipements
     */
    EquipmentController(EquipmentService service){
        this.service = service;
    }

    /**
     * Point d'accès pour récupérer toutes les demandes d'équipements.
     * <p>
     * Cette méthode répond aux requêtes HTTP GET pour lister toutes les demandes d'équipements
     * enregistrées.
     * </p>
     *
     * @return une {@link ResponseEntity} contenant la liste de toutes les demandes d'équipements sous forme de DTO avec un code HTTP 200 (OK)
     */
    @GetMapping("/requests/all")
    public ResponseEntity<List<EquipmentRequestDTO>> getAll(){
        return ResponseEntity
                .status(200)
                .body(this.service.getAllRequests());
    }

    @PostMapping("/save")
    public ResponseEntity<EquipmentRequestDTO> saveRequest(@RequestBody EquipmentRequestDTO dto) {
        return ResponseEntity.ok(this.service.saveRequest(dto));
    }
}
