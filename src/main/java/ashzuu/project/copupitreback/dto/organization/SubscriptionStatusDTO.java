package ashzuu.project.copupitreback.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusDTO {
    private Long organizationId;
    private String organizationName;
    private boolean isSubscribed;
}
