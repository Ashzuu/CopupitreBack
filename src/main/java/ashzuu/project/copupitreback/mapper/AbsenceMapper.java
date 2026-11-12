package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.absence.AbsenceDTO;
import ashzuu.project.copupitreback.jpa.entities.organization.Absence;

import java.util.Collections;
import java.util.List;

public class AbsenceMapper {

    public static AbsenceDTO toDto(Absence absence) {
        if (absence == null) {
            return null;
        }

        return AbsenceDTO.builder()
                .id(absence.getId())
                .organizationId(absence.getOrganization() != null ? absence.getOrganization().getId() : null)
                .musicianId(absence.getMusician() != null ? absence.getMusician().getId() : null)
                .musicianFirstName(absence.getMusician() != null ? absence.getMusician().getFirstName() : null)
                .musicianLastName(absence.getMusician() != null ? absence.getMusician().getLastName() : null)
                .musicianEmail(absence.getMusician() != null ? absence.getMusician().getEmail() : null)
                .instrument(absence.getMusician() != null ? absence.getMusician().getInstrument() : null)
                .eventType(absence.getEventType())
                .eventDate(absence.getEventDate())
                .endDate(absence.getEndDate())
                .reason(absence.getReason())
                .status(absence.getStatus())
                .submittedByAdmin(absence.isSubmittedByAdmin())
                .createdAt(absence.getCreatedAt())
                .build();
    }

    public static List<AbsenceDTO> toDtoList(List<Absence> absences) {
        if (absences == null) {
            return Collections.emptyList();
        }
        return absences.stream().map(AbsenceMapper::toDto).toList();
    }
}
