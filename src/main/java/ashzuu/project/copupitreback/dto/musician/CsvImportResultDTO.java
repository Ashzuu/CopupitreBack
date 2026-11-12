package ashzuu.project.copupitreback.dto.musician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvImportResultDTO {
    private int totalRows;
    private int importedCount;
    private int skippedCount;
    private int errorCount;

    @Builder.Default
    private List<String> messages = new ArrayList<>();

    @Builder.Default
    private List<MusicianDTO> musicians = new ArrayList<>();
}
