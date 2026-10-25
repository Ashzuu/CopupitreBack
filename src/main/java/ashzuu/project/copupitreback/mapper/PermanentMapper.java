package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.organization.RehearsalScheduleDTO;
import ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeedApplication;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeeds;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;

import java.util.ArrayList;
import java.util.List;

public class PermanentMapper {

    public static PermanentAnnouncementDTO toDto(AnnualNeeds need) {
        if (need == null) {
            return null;
        }

        Organization org = need.getOrganization();
        List<RehearsalScheduleDTO> regularSchedules = new ArrayList<>();
        List<String> adminUsernames = new ArrayList<>();

        if (org != null) {
            if (org.getRehearsalSchedules() != null) {
                regularSchedules = org.getRehearsalSchedules().stream()
                        .map(OrganizationMapper::toDto)
                        .toList();
            }
            if (org.getOrganizationAdmins() != null) {
                adminUsernames = OrganizationMapper.toAdminUsernames(org.getOrganizationAdmins());
            }
        }

        long acceptedCount = 0;
        List<ReinforcementResponseDTO> responses = new ArrayList<>();
        if (need.getApplications() != null) {
            acceptedCount = need.getApplications().stream()
                    .filter(a -> "ACCEPTED".equalsIgnoreCase(a.getStatus()))
                    .count();
            responses = need.getApplications().stream()
                    .map(PermanentMapper::toResponseDto)
                    .toList();
        }

        int reqCount = need.getRequiredCount() != null ? need.getRequiredCount() : 1;
        String status = (acceptedCount >= reqCount) ? "OBJECTIVE_MET" : "OPEN";

        return PermanentAnnouncementDTO.builder()
                .id(need.getId())
                .instrument(need.getInstrument())
                .requiredCount(need.getRequiredCount())
                .validatedCount((int) acceptedCount)
                .description(need.getDescription())
                .status(status)
                .type("PERMANENT")
                .organizationId(org != null ? org.getId() : null)
                .organizationName(org != null ? org.getName() : null)
                .organizationCity(org != null ? org.getCity() : null)
                .formationType(org != null ? org.getFormationType() : null)
                .musicianCount(org != null ? org.getMusicianCount() : null)
                .organizationDescription(org != null ? org.getDescription() : null)
                .rehearsalAddress(org != null ? org.getRehearsalAddress() : null)
                .rehearsalSchedule(org != null ? org.getRehearsalSchedule() : null)
                .regularSchedules(regularSchedules)
                .adminUsernames(adminUsernames)
                .responses(responses)
                .build();
    }

    public static List<PermanentAnnouncementDTO> toDtoList(List<AnnualNeeds> needs) {
        if (needs == null) {
            return new ArrayList<>();
        }
        return needs.stream().map(PermanentMapper::toDto).toList();
    }

    public static ReinforcementResponseDTO toResponseDto(AnnualNeedApplication app) {
        if (app == null) {
            return null;
        }

        String orgName = (app.getAnnualNeed() != null && app.getAnnualNeed().getOrganization() != null)
                ? app.getAnnualNeed().getOrganization().getName()
                : "";
        String inst = (app.getAnnualNeed() != null) ? app.getAnnualNeed().getInstrument() : "";
        String title = (inst + (orgName.isEmpty() ? "" : " - " + orgName)).trim();

        String fullName = null;
        String username = null;
        String userInstrument = null;
        if (app.getUser() != null) {
            username = app.getUser().getUsername();
            String first = app.getUser().getFirstName() != null ? app.getUser().getFirstName() : "";
            String last = app.getUser().getLastName() != null ? app.getUser().getLastName() : "";
            fullName = (first + " " + last).trim();
            userInstrument = app.getUser().getMainInstrument();
        }

        return ReinforcementResponseDTO.builder()
                .id(app.getId())
                .announcementId(app.getAnnualNeed() != null ? app.getAnnualNeed().getId() : null)
                .announcementTitle(title)
                .respondentUsername(username)
                .respondentFullName(fullName)
                .respondentInstrument(userInstrument)
                .respondedAt(app.getAppliedAt())
                .status(app.getStatus())
                .message(app.getMessage())
                .build();
    }

    public static List<ReinforcementResponseDTO> toResponseDtoList(List<AnnualNeedApplication> apps) {
        if (apps == null) {
            return new ArrayList<>();
        }
        return apps.stream().map(PermanentMapper::toResponseDto).toList();
    }
}
