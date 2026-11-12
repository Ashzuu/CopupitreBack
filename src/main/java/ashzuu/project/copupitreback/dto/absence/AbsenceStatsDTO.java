package ashzuu.project.copupitreback.dto.absence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceStatsDTO {
    private Long musicianId;
    private String musicianName;
    private String musicianEmail;
    private String instrument;
    private long totalAbsences;
    private long justifiedCount;
    private long unjustifiedCount;
    private long pendingCount;
}
