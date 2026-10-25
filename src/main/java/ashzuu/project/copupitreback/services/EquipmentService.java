package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequestDate;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequestDateId;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.repositories.EquipmentRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.mapper.EquipmentMapper;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.email.EmailService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service gérant la logique métier liée aux équipements et demandes de prêt de matériel (Epic 04).
 * <p>
 * Ce service permet de créer, modifier, clôturer et supprimer des demandes d'équipements,
 * en vérifiant rigoureusement les droits d'administration de l'organisation émettrice.
 * </p>
 */
@Service
public class EquipmentService {

    private final EquipmentRepository repo;
    private final OrganizationRepository orgRepo;
    private final EmailService emailService;

    public EquipmentService(EquipmentRepository repo, OrganizationRepository orgRepo, EmailService emailService) {
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.emailService = emailService;
    }

    /**
     * Récupère l'ensemble des demandes d'équipements publiques (US-MAT-02).
     */
    public List<EquipmentRequestDTO> getAllRequests() {
        return EquipmentMapper.toDto(this.repo.findAll());
    }

    /**
     * Récupère les demandes d'équipements émises par les organisations administrées par l'utilisateur connecté (US-MAT-03).
     */
    public List<EquipmentRequestDTO> getMyEquipmentRequests(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        List<Organization> userOrgs = orgRepo.getAllByUserId(userDetails.getId());
        List<Long> orgIds = userOrgs.stream().map(Organization::getId).toList();
        if (orgIds.isEmpty()) {
            return List.of();
        }
        return EquipmentMapper.toDto(this.repo.findByOrganizationIdIn(orgIds));
    }

    /**
     * Récupère les demandes de matériel d'une organisation donnée.
     */
    public List<EquipmentRequestDTO> getRequestsByOrganization(Long organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("L'identifiant d'organisation est obligatoire");
        }
        return EquipmentMapper.toDto(this.repo.findByOrganizationId(organizationId));
    }

    /**
     * Crée ou met à jour une demande de matériel (US-MAT-01 / RG-MAT-01 / RG-MAT-02 / RG-MAT-03).
     */
    @Transactional
    public EquipmentRequestDTO saveRequest(EquipmentRequestDTO dto, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        // RG-MAT-02 : Validation des champs obligatoires
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de la demande est obligatoire (RG-MAT-02)");
        }
        if (dto.getEquipmentName() == null || dto.getEquipmentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du matériel recherché est obligatoire (RG-MAT-02)");
        }
        if (dto.getDuration() == null || dto.getDuration().trim().isEmpty()) {
            throw new IllegalArgumentException("La durée estimée de l'emprunt est obligatoire (RG-MAT-02)");
        }

        EquipmentRequest request;
        Organization organization = null;

        if (dto.getId() != null) {
            // Modification d'une demande existante
            request = repo.findById(dto.getId())
                    .orElseThrow(() -> new UnknownID("Demande d'équipement inconnue : " + dto.getId()));

            // Vérification des droits administrateur sur l'organisation émettrice actuelle (RG-MAT-01)
            if (request.getOrganization() != null) {
                if (!orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
                    throw new AccessDeniedException("Vous n'êtes pas administrateur de l'organisation");
                }
            }

            // Si changement d'organisation, vérification des droits sur la nouvelle organisation
            if (dto.getOrganizationId() != null && (request.getOrganization() == null || !dto.getOrganizationId().equals(request.getOrganization().getId()))) {
                if (!orgRepo.isAdminOfOrganization(userDetails.getId(), dto.getOrganizationId())) {
                    throw new AccessDeniedException("Vous n'êtes pas administrateur de la nouvelle organisation");
                }
                organization = orgRepo.findById(dto.getOrganizationId())
                        .orElseThrow(() -> new UnknownID("Organisation inconnue : " + dto.getOrganizationId()));
                request.setOrganization(organization);
            }

            request.setTitle(dto.getTitle().trim());
            request.setEquipmentName(dto.getEquipmentName().trim());
            request.setDuration(dto.getDuration().trim());
            request.setDescription(dto.getDescription());
            if (dto.getStatus() != null && isValidStatus(dto.getStatus())) {
                request.setStatus(dto.getStatus().trim().toUpperCase());
            }

            updateRequestDates(request, dto.getEventDates());
        } else {
            // Création d'une nouvelle demande (RG-MAT-01)
            if (dto.getOrganizationId() == null) {
                throw new IllegalArgumentException("L'identifiant de l'organisation est obligatoire pour créer une demande (RG-MAT-01)");
            }
            if (!orgRepo.isAdminOfOrganization(userDetails.getId(), dto.getOrganizationId())) {
                throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation (RG-MAT-01)");
            }

            organization = orgRepo.findById(dto.getOrganizationId())
                    .orElseThrow(() -> new UnknownID("Organisation inconnue : " + dto.getOrganizationId()));

            request = EquipmentMapper.toEntity(dto);
            request.setOrganization(organization);
            request.setCreatedAt(Instant.now());
            if (request.getStatus() == null || !isValidStatus(request.getStatus())) {
                request.setStatus("OPEN"); // RG-MAT-04
            }

            if (dto.getEventDates() != null) {
                List<EquipmentRequestDate> dates = new ArrayList<>();
                for (LocalDate date : dto.getEventDates()) {
                    EquipmentRequestDate requestDate = new EquipmentRequestDate();
                    EquipmentRequestDateId dateId = new EquipmentRequestDateId();
                    dateId.setEventDate(date);
                    requestDate.setId(dateId);
                    requestDate.setRequest(request);
                    dates.add(requestDate);
                }
                request.setEquipmentRequestDates(dates);
            }
        }

        EquipmentRequest saved = repo.save(request);
        return EquipmentMapper.toDto(saved);
    }

    /**
     * Met à jour le statut d'une demande de matériel (US-MAT-03 / RG-MAT-04).
     */
    @Transactional
    public EquipmentRequestDTO updateStatus(Long id, String newStatus, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        EquipmentRequest request = repo.findById(id)
                .orElseThrow(() -> new UnknownID("Demande d'équipement inconnue : " + id));

        if (request.getOrganization() == null || !orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de l'organisation");
        }

        String normalized = newStatus != null ? newStatus.trim().toUpperCase() : "";
        if (!isValidStatus(normalized)) {
            throw new IllegalArgumentException("Statut invalide : " + newStatus + ". Statuts autorisés : OPEN, MET, CLOSED (RG-MAT-04)");
        }

        request.setStatus(normalized);
        EquipmentRequest saved = repo.save(request);

        // Notification email découplée
        String orgName = request.getOrganization() != null ? request.getOrganization().getName() : "";
        emailService.sendEquipmentRequestStatusUpdate(userDetails.getUsername(), orgName, request.getEquipmentName(), normalized);

        return EquipmentMapper.toDto(saved);
    }

    /**
     * Clôture ou supprime une demande de matériel (UC-MAT-05 / US-MAT-03).
     */
    @Transactional
    public void deleteRequest(Long id, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        EquipmentRequest request = repo.findById(id)
                .orElseThrow(() -> new UnknownID("Demande d'équipement inconnue : " + id));

        if (request.getOrganization() == null || !orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de l'organisation");
        }

        repo.delete(request);
    }

    private boolean isValidStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase();
        return "OPEN".equals(s) || "MET".equals(s) || "CLOSED".equals(s);
    }

    private void updateRequestDates(EquipmentRequest request, List<LocalDate> eventDates) {
        if (eventDates == null) return;
        List<EquipmentRequestDate> current = request.getEquipmentRequestDates();

        // Supprimer les dates qui ne sont pas dans eventDates
        current.removeIf(d -> !eventDates.contains(d.getId().getEventDate()));

        // Ajouter les nouvelles dates
        for (LocalDate date : eventDates) {
            boolean exists = current.stream().anyMatch(d -> d.getId().getEventDate().equals(date));
            if (!exists) {
                EquipmentRequestDate requestDate = new EquipmentRequestDate();
                EquipmentRequestDateId dateId = new EquipmentRequestDateId();
                dateId.setEventDate(date);
                if (request.getId() != null) {
                    dateId.setRequestId(request.getId());
                }
                requestDate.setId(dateId);
                requestDate.setRequest(request);
                current.add(requestDate);
            }
        }
    }
}
