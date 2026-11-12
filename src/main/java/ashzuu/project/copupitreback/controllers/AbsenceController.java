package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.absence.*;
import ashzuu.project.copupitreback.jpa.entities.organization.AbsenceStatus;
import ashzuu.project.copupitreback.jpa.entities.organization.EventType;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.AbsenceService;
import ashzuu.project.copupitreback.services.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/organizations/{id}/absences")
public class AbsenceController {

    private final AbsenceService absenceService;
    private final OrganizationService organizationService;

    public AbsenceController(AbsenceService absenceService, OrganizationService organizationService) {
        this.absenceService = absenceService;
        this.organizationService = organizationService;
    }

    private Long resolveOrgId(String id) {
        return organizationService.findOrganizationByIdOrName(id).getId();
    }

    @PostMapping
    public ResponseEntity<AbsenceDTO> registerAbsence(
            @PathVariable String id,
            @Valid @RequestBody CreateAbsenceRequest req,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        AbsenceDTO created = absenceService.registerAbsence(orgId, req, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AbsenceDTO>> getAbsences(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) AbsenceStatus status,
            @RequestParam(required = false) Long musicianId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        List<AbsenceDTO> list = absenceService.getAbsences(orgId, startDate, endDate, eventType, status, musicianId, userDetails);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<AbsenceStatsDTO>> getMusicianAbsenceStats(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        List<AbsenceStatsDTO> stats = absenceService.getMusicianAbsenceStats(orgId, startDate, endDate, userDetails);
        return ResponseEntity.ok(stats);
    }

    @PatchMapping("/{absenceId}/status")
    public ResponseEntity<AbsenceDTO> updateAbsenceStatus(
            @PathVariable String id,
            @PathVariable Long absenceId,
            @Valid @RequestBody UpdateAbsenceStatusRequest req,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        AbsenceDTO updated = absenceService.updateAbsenceStatus(orgId, absenceId, req, userDetails);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/cancel-event")
    public ResponseEntity<Void> notifyEventCancellation(
            @PathVariable String id,
            @Valid @RequestBody CancelEventNotificationRequest req,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        absenceService.notifyEventCancellation(orgId, req, userDetails);
        return ResponseEntity.ok().build();
    }
}
