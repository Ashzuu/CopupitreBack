package ashzuu.project.copupitreback.jpa.entities.equipment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Clé primaire composite pour l'entité {@link EquipmentRequestDate}.
 * <p>
 * Rôle : Identifie de façon unique une date de besoin en matériel pour une demande précise, en associant `requestId` et `eventDate`.
 */
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class EquipmentRequestDateId implements Serializable {
    private static final long serialVersionUID = 586610708957342672L;
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;


}