package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.organization.*;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;

/**
 * Composant responsable de la conversion entre les entités liées aux organisations et le {@link OrganizationDTO}.
 * <p>
 * But : Regrouper toutes les informations d'un orchestre (concerts, plannings, admins) en un seul objet logique complet.
 * <p>
 * Liens : Gère l'entité {@link Organization} et ses multiples entités filles (Concert, AnnualNeeds, etc.) pour produire un {@link OrganizationDTO} et ses sous-DTOs.
 */
public class OrganizationMapper {

    public static OrganizationDTO toDto(Organization organization) {
        OrganizationDTO dto = OrganizationDTO.builder().build();
        return dto;
    }
}
