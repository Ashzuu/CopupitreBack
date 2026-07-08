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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service gérant la logique métier liée aux équipements.
 * <p>
 * Ce service permet de récupérer et de traiter les demandes d'équipements de l'application.
 * </p>
 */
@Service
public class EquipmentService {

    /**
     * Dépôt permettant de communiquer avec la table des équipements en base de données.
     */
    private final EquipmentRepository repo;

    /**
     * Dépôt permettant de communiquer avec la table des organisations en base de données.
     */
    private final OrganizationRepository orgRepo;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param repo le dépôt des équipements
     * @param orgRepo le dépôt des organisations
     */
    public EquipmentService(EquipmentRepository repo, OrganizationRepository orgRepo){
        this.repo = repo;
        this.orgRepo = orgRepo;
    }

    /**
     * Récupère l'ensemble des demandes d'équipements sous forme de DTO.
     * <p>
     * Cette méthode récupère tous les équipements enregistrés en base de données,
     * puis les convertit en objets de transfert de données (DTO) via {@link EquipmentMapper}.
     * </p>
     *
     * @return la liste de toutes les demandes d'équipements sous forme de {@link EquipmentRequestDTO}
     */
    public List<EquipmentRequestDTO> getAllRequests(){
        return EquipmentMapper.toDto(this.repo.findAll());
    }

    /**
     * Crée ou met à jour une demande d'équipement et met à jour ses dates associées.
     *
     * @param dto la demande d'équipement sous forme de DTO
     * @return la demande d'équipement enregistrée sous forme de DTO
     */
    @Transactional
    public EquipmentRequestDTO saveRequest(EquipmentRequestDTO dto, ashzuu.project.copupitreback.security.UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new org.springframework.security.access.AccessDeniedException("Utilisateur non authentifié");
        }

        EquipmentRequest request;
        Organization organization = null;

        if (dto.getId() != null) {
            request = repo.findById(dto.getId())
                    .orElseThrow(() -> new UnknownID("Demande d'équipement inconnue"));

            // Verify admin rights on current organization
            if (request.getOrganization() != null) {
                if (!orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
                    throw new org.springframework.security.access.AccessDeniedException("Vous n'êtes pas administrateur de l'organisation");
                }
            }

            // If changing organization, verify admin rights on new one
            if (dto.getOrganizationId() != null && (request.getOrganization() == null || !dto.getOrganizationId().equals(request.getOrganization().getId()))) {
                if (!orgRepo.isAdminOfOrganization(userDetails.getId(), dto.getOrganizationId())) {
                    throw new org.springframework.security.access.AccessDeniedException("Vous n'êtes pas administrateur de la nouvelle organisation");
                }
            }

            if (dto.getOrganizationId() != null) {
                organization = orgRepo.findById(dto.getOrganizationId())
                        .orElseThrow(() -> new UnknownID("Organisation inconnue"));
            }

            request.setTitle(dto.getTitle());
            request.setEquipmentName(dto.getEquipmentName());
            request.setDuration(dto.getDuration());
            request.setDescription(dto.getDescription());
            request.setStatus(dto.getStatus());
            if (organization != null) {
                request.setOrganization(organization);
            }

            updateRequestDates(request, dto.getEventDates());
        } else {
            // New request - check admin rights on organizationId
            if (dto.getOrganizationId() == null) {
                throw new IllegalArgumentException("L'identifiant de l'organisation est obligatoire pour créer une demande");
            }
            if (!orgRepo.isAdminOfOrganization(userDetails.getId(), dto.getOrganizationId())) {
                throw new org.springframework.security.access.AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
            }

            if (dto.getOrganizationId() != null) {
                organization = orgRepo.findById(dto.getOrganizationId())
                        .orElseThrow(() -> new UnknownID("Organisation inconnue"));
            }

            request = EquipmentMapper.toEntity(dto);
            if (organization != null) {
                request.setOrganization(organization);
            }
            request.setCreatedAt(Instant.now());
            if (request.getStatus() == null) {
                request.setStatus("OPEN");
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
