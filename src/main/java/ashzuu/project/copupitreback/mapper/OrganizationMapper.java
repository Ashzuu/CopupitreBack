package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.organization.*;
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
        return OrganizationDTO.builder()
                .id(organization.getId())
                .name(organization.getName())
                .city(organization.getCity())
                .description(organization.getDescription())
                .concertDress(organization.getConcertDress())
                .musicianCount(organization.getMusicianCount())
                .adminUsernames(toStringAdmin(organization.getOrganizationAdmins()))
                .rehearsalManuals(new ArrayList<>())
                .rehearsalSchedules(new ArrayList<>())
                .annualNeeds(new ArrayList<>())
                .concerts(new ArrayList<>())
            .build();
    }

    public static List<OrganizationDTO> toDto(List<Organization> organizations){
        return organizations.stream().map(OrganizationMapper::toDto).toList();
    }

    public static String toStringAdmin(OrganizationAdmin admin){
        return admin.getUser().getFirstName() + " " + admin.getUser().getLastName();
    }

    public static List<String> toStringAdmin(List<OrganizationAdmin> admins){
        return admins.stream().map(OrganizationMapper::toStringAdmin).toList();
    }
}
