package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.jpa.repositories.ReinforcementRepository;
import ashzuu.project.copupitreback.mapper.ReinforcementMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reinforcements")
public class ReinforcementController {
    private final ReinforcementRepository repo;

    public ReinforcementController(ReinforcementRepository repo){
        this.repo = repo;
    }

    @RequestMapping("/all")
    public ResponseEntity<List<ReinforcementAnnouncementDTO>> getAllAnnouncements(){
        return ResponseEntity
                .ok(ReinforcementMapper.toAnnouncementDto(this.repo.findAll()));
    }
}
