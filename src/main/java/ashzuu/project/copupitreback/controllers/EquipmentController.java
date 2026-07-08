package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.repositories.EquipmentRepository;
import ashzuu.project.copupitreback.mapper.EquipmentMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/equipments")
public class EquipmentController {
    private final EquipmentRepository repo;

    EquipmentController(EquipmentRepository repo){
        this.repo = repo;
    }

    @GetMapping("/requests/all")
    public ResponseEntity<List<EquipmentRequestDTO>> getAll(){
        return ResponseEntity
                .status(200)
                .body(EquipmentMapper.toDto(this.repo.findAll()));
    }

}
