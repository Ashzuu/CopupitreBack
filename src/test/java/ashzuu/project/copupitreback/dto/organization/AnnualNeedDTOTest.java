package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnualNeedDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerializationIncludesBothCountAndRequiredCount() throws Exception {
        AnnualNeedDTO dto = AnnualNeedDTO.builder()
                .id(1L)
                .instrument("Trompette")
                .requiredCount(3)
                .description("Pupitre 1")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertTrue(json.contains("\"requiredCount\":3"));
        assertTrue(json.contains("\"count\":3"));
        assertEquals(3, dto.getCount());
        assertEquals(3, dto.getRequiredCount());
    }

    @Test
    void testDeserializationWithCountProperty() throws Exception {
        String json = "{\"id\":2,\"instrument\":\"Hautbois\",\"count\":4,\"description\":\"Permanent\"}";

        AnnualNeedDTO dto = objectMapper.readValue(json, AnnualNeedDTO.class);

        assertEquals(2L, dto.getId());
        assertEquals("Hautbois", dto.getInstrument());
        assertEquals(4, dto.getRequiredCount());
        assertEquals(4, dto.getCount());
    }

    @Test
    void testDeserializationWithRequiredCountProperty() throws Exception {
        String json = "{\"id\":3,\"instrument\":\"Clarinette\",\"requiredCount\":5,\"description\":\"Niveau fin de 2e cycle\"}";

        AnnualNeedDTO dto = objectMapper.readValue(json, AnnualNeedDTO.class);

        assertEquals(3L, dto.getId());
        assertEquals("Clarinette", dto.getInstrument());
        assertEquals(5, dto.getRequiredCount());
        assertEquals(5, dto.getCount());
    }

    @Test
    void testBuilderWithCount() {
        AnnualNeedDTO dto = AnnualNeedDTO.builder()
                .id(10L)
                .instrument("Cor")
                .count(2)
                .build();

        assertEquals(2, dto.getRequiredCount());
        assertEquals(2, dto.getCount());
    }
}
