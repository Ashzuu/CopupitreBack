package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.organization.*;
import ashzuu.project.copupitreback.jpa.entities.*;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin;

import java.util.ArrayList;
import java.util.List;

/**
 * Composant responsable de la conversion entre les entités liées aux organisations et le {@link OrganizationDTO}.
 * <p>
 * But : Regrouper toutes les informations d'un orchestre (concerts, plannings, admins) en un seul objet logique complet.
 * <p>
 * Liens : Gère l'entité {@link Organization} et ses multiples entités filles (Concert, AnnualNeeds, etc.) pour produire un {@link OrganizationDTO} et ses sous-DTOs.
 */
public class OrganizationMapper {

    public static OrganizationDTO toDto(Organization organization) {
        if (organization == null) {
            return null;
        }
        return OrganizationDTO.builder()
                .id(organization.getId())
                .name(organization.getName())
                .city(organization.getCity())
                .formationType(organization.getFormationType())
                .description(organization.getDescription())
                .concertDress(organization.getConcertDress())
                .logisticsInfo(organization.getLogisticsInfo())
                .musicianCount(organization.getMusicianCount())
                .rehearsalAddress(organization.getRehearsalAddress())
                .rehearsalSchedule(organization.getRehearsalSchedule())
                .createdAt(organization.getCreatedAt())
                .adminUsernames(toAdminUsernames(organization.getOrganizationAdmins()))
                .adminDisplayNames(toAdminDisplayNames(organization.getOrganizationAdmins()))
                .rehearsalManuals(organization.getRehearsalManuals() != null ?
                        organization.getRehearsalManuals().stream().map(OrganizationMapper::toDto).toList() : new ArrayList<>())
                .rehearsalSchedules(organization.getRehearsalSchedules() != null ?
                        organization.getRehearsalSchedules().stream().map(OrganizationMapper::toDto).toList() : new ArrayList<>())
                .annualNeeds(organization.getAnnualNeeds() != null ?
                        organization.getAnnualNeeds().stream().map(OrganizationMapper::toDto).toList() : new ArrayList<>())
                .concerts(organization.getConcerts() != null ?
                        organization.getConcerts().stream().map(OrganizationMapper::toDto).toList() : new ArrayList<>())
                .equipmentRequests(organization.getEquipmentRequests() != null ?
                        EquipmentMapper.toDto(organization.getEquipmentRequests()) : new ArrayList<>())
                .reinforcementRequests(organization.getReinforcementRequests() != null ?
                        ReinforcementMapper.toAnnouncementDto(organization.getReinforcementRequests()) : new ArrayList<>())
                .build();
    }

    public static List<OrganizationDTO> toDto(List<Organization> organizations){
        return organizations.stream().map(OrganizationMapper::toDto).toList();
    }

    public static String toAdminUsername(OrganizationAdmin admin) {
        if (admin == null || admin.getUser() == null || admin.getUser().getUsername() == null) {
            return "";
        }
        return admin.getUser().getUsername();
    }

    public static List<String> toAdminUsernames(List<OrganizationAdmin> admins) {
        if (admins == null) {
            return new ArrayList<>();
        }
        return admins.stream()
                .map(OrganizationMapper::toAdminUsername)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public static String toAdminDisplayName(OrganizationAdmin admin) {
        if (admin == null || admin.getUser() == null) {
            return "";
        }
        String first = admin.getUser().getFirstName() != null ? admin.getUser().getFirstName() : "";
        String last = admin.getUser().getLastName() != null ? admin.getUser().getLastName() : "";
        return (first + " " + last).trim();
    }

    public static List<String> toAdminDisplayNames(List<OrganizationAdmin> admins) {
        if (admins == null) {
            return new ArrayList<>();
        }
        return admins.stream()
                .map(OrganizationMapper::toAdminDisplayName)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Deprecated
    public static String toStringAdmin(OrganizationAdmin admin){
        return toAdminDisplayName(admin);
    }

    @Deprecated
    public static List<String> toStringAdmin(List<OrganizationAdmin> admins){
        return toAdminDisplayNames(admins);
    }

    public static Organization toEntity(OrganizationDTO dto) {
        if (dto == null) {
            return null;
        }
        Organization org = Organization.builder()
                .id(dto.getId())
                .name(dto.getName())
                .city(dto.getCity())
                .description(dto.getDescription())
                .concertDress(dto.getConcertDress())
                .logisticsInfo(dto.getLogisticsInfo())
                .formationType(dto.getFormationType())
                .musicianCount(dto.getMusicianCount())
                .rehearsalAddress(dto.getRehearsalAddress())
                .rehearsalSchedule(dto.getRehearsalSchedule())
                .createdAt(dto.getCreatedAt())
                .annualNeeds(new ArrayList<>())
                .concerts(new ArrayList<>())
                .rehearsalManuals(new ArrayList<>())
                .rehearsalSchedules(new ArrayList<>())
                .organizationAdmins(new ArrayList<>())
                .equipmentRequests(new ArrayList<>())
                .reinforcementRequests(new ArrayList<>())
                .build();

        if (dto.getConcerts() != null) {
            for (ConcertDTO concertDto : dto.getConcerts()) {
                org.addConcert(toEntity(concertDto));
            }
        }
        if (dto.getAnnualNeeds() != null) {
            for (AnnualNeedDTO needDto : dto.getAnnualNeeds()) {
                org.addAnnualNeeds(toEntity(needDto));
            }
        }
        if (dto.getRehearsalManuals() != null) {
            for (RehearsalManualDTO manualDto : dto.getRehearsalManuals()) {
                org.addRehersalManual(toEntity(manualDto));
            }
        }
        if (dto.getRehearsalSchedules() != null) {
            for (RehearsalScheduleDTO scheduleDto : dto.getRehearsalSchedules()) {
                org.addSchedules(toEntity(scheduleDto));
            }
        }
        if (dto.getEquipmentRequests() != null) {
            for (var eqDto : dto.getEquipmentRequests()) {
                org.addEquipment(EquipmentMapper.toEntity(eqDto));
            }
        }
        if (dto.getReinforcementRequests() != null) {
            for (var reDto : dto.getReinforcementRequests()) {
                org.addReinforcement(ReinforcementMapper.toEntity(reDto));
            }
        }
        return org;
    }

    public static Concert toEntity(ConcertDTO dto){
        if (dto == null) {
            return null;
        }
        return Concert.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .eventDate(dto.getEventDate())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .build();
    }

    public static ConcertDTO toDto(Concert concert) {
        if (concert == null) {
            return null;
        }
        return ConcertDTO.builder()
                .id(concert.getId())
                .title(concert.getTitle())
                .eventDate(concert.getEventDate())
                .location(concert.getLocation())
                .description(concert.getDescription())
                .build();
    }

    public static AnnualNeeds toEntity(AnnualNeedDTO dto) {
        if (dto == null) {
            return null;
        }
        Integer count = dto.getRequiredCount() != null ? dto.getRequiredCount() : dto.getCount();
        return AnnualNeeds.builder()
                .id(dto.getId())
                .instrument(dto.getInstrument())
                .requiredCount(count != null ? count : 0)
                .description(dto.getDescription())
                .build();
    }

    public static AnnualNeedDTO toDto(AnnualNeeds need) {
        if (need == null) {
            return null;
        }
        return AnnualNeedDTO.builder()
                .id(need.getId())
                .instrument(need.getInstrument())
                .requiredCount(need.getRequiredCount())
                .description(need.getDescription())
                .build();
    }

    public static RehearsalManual toEntity(RehearsalManualDTO dto) {
        if (dto == null) {
            return null;
        }
        return RehearsalManual.builder()
                .id(dto.getId())
                .manualDate(dto.getManualDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .location(dto.getLocation())
                .build();
    }

    public static RehearsalManualDTO toDto(RehearsalManual manual) {
        if (manual == null) {
            return null;
        }
        return RehearsalManualDTO.builder()
                .id(manual.getId())
                .manualDate(manual.getManualDate())
                .startTime(manual.getStartTime())
                .endTime(manual.getEndTime())
                .location(manual.getLocation())
                .build();
    }

    public static RehearsalSchedule toEntity(RehearsalScheduleDTO dto) {
        if (dto == null) {
            return null;
        }
        return RehearsalSchedule.builder()
                .id(dto.getId())
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .location(dto.getLocation())
                .build();
    }

    public static RehearsalScheduleDTO toDto(RehearsalSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        return RehearsalScheduleDTO.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .location(schedule.getLocation())
                .organizationName(schedule.getOrganization() != null ? schedule.getOrganization().getName() : null)
                .build();
    }
}
