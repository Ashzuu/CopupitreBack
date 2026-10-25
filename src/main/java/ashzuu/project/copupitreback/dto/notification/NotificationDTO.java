package ashzuu.project.copupitreback.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String link;
    private String contextType;
    private Long contextId;

    @JsonProperty("isRead")
    private boolean isRead;

    private Instant createdAt;

    @JsonProperty("isRead")
    public boolean isRead() {
        return this.isRead;
    }

    @JsonProperty("isRead")
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}
