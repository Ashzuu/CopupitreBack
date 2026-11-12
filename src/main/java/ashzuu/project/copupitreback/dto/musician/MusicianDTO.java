package ashzuu.project.copupitreback.dto.musician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicianDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String instrument;
    private Long userId;
    private String username;
    private Instant createdAt;
}
