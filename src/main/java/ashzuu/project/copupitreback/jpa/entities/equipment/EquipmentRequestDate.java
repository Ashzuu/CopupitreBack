package ashzuu.project.copupitreback.jpa.entities.equipment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Entité de jointure/détail représentant les dates associées à une demande d'équipement.
 * <p>
 * Rôle : Permet de définir un ensemble de jours précis où l'équipement est requis, palliant ainsi aux durées non-contiguës.
 * <p>
 * Liens : ManyToOne avec {@link EquipmentRequest} (la demande d'équipement principale).
 * Utilise la clé composite {@link EquipmentRequestDateId}.
 */
@Getter
@Setter
@Entity
@Table(name = "equipment_request_dates", schema = "main")
public class EquipmentRequestDate {
    @EmbeddedId
    private EquipmentRequestDateId id;

    @MapsId("requestId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "request_id", nullable = false)
    private EquipmentRequest request;


}