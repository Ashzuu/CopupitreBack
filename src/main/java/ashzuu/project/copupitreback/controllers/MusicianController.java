package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.musician.AssociateMusicianUserRequest;
import ashzuu.project.copupitreback.dto.musician.CreateMusicianRequest;
import ashzuu.project.copupitreback.dto.musician.CsvImportResultDTO;
import ashzuu.project.copupitreback.dto.musician.MusicianDTO;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.MusicianService;
import ashzuu.project.copupitreback.services.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/organizations/{id}/musicians")
public class MusicianController {

    private final MusicianService musicianService;
    private final OrganizationService organizationService;

    public MusicianController(MusicianService musicianService, OrganizationService organizationService) {
        this.musicianService = musicianService;
        this.organizationService = organizationService;
    }

    private Long resolveOrgId(String id) {
        return organizationService.findOrganizationByIdOrName(id).getId();
    }

    @GetMapping
    public ResponseEntity<List<MusicianDTO>> getMusicians(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(musicianService.getOrganizationMusicians(resolveOrgId(id), userDetails));
    }

    @PostMapping
    public ResponseEntity<MusicianDTO> addMusician(
            @PathVariable String id,
            @Valid @RequestBody CreateMusicianRequest req,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        MusicianDTO created = musicianService.addMusician(orgId, req, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvImportResultDTO> importMusiciansCsvFile(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws IOException {
        Long orgId = resolveOrgId(id);
        byte[] fileBytes = file.getBytes();

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (content.contains("\uFFFD")) {
            content = new String(fileBytes, Charset.forName("windows-1252"));
        }

        return ResponseEntity.ok(musicianService.importMusiciansCsv(orgId, content, userDetails));
    }

    @PostMapping(value = "/import-csv", consumes = {MediaType.TEXT_PLAIN_VALUE, "text/csv"})
    public ResponseEntity<CsvImportResultDTO> importMusiciansCsvText(
            @PathVariable String id,
            @RequestBody String csvContent,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        return ResponseEntity.ok(musicianService.importMusiciansCsv(orgId, csvContent, userDetails));
    }

    @DeleteMapping("/{musicianId}")
    public ResponseEntity<Void> removeMusician(
            @PathVariable String id,
            @PathVariable Long musicianId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        musicianService.removeMusician(orgId, musicianId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{musicianId}/associate-user")
    public ResponseEntity<MusicianDTO> associateUser(
            @PathVariable String id,
            @PathVariable Long musicianId,
            @Valid @RequestBody AssociateMusicianUserRequest req,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orgId = resolveOrgId(id);
        MusicianDTO updated = musicianService.associateUserByEmail(orgId, musicianId, req, userDetails);
        return ResponseEntity.ok(updated);
    }
}
