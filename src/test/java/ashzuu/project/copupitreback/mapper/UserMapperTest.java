package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void testToDto() {
        User user = User.builder()
                .id(1L)
                .username("john_doe")
                .firstName("John")
                .lastName("Doe")
                .mainInstrument("Trumpet")
                .musicalDescription("Intermediate player")
                .yearsOfPractice(5)
                .build();

        UserDTO dto = UserMapper.toDto(user);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals(user.getMainInstrument(), dto.getMainInstrument());
        assertEquals(user.getMusicalDescription(), dto.getMusicalDescription());
        assertEquals(user.getYearsOfPractice(), dto.getYearsOfPractice());
    }

    @Test
    void testToDtoList() {
        User user1 = User.builder().id(1L).username("user1").build();
        User user2 = User.builder().id(2L).username("user2").build();

        List<UserDTO> dtos = UserMapper.toDto(List.of(user1, user2));

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("user1", dtos.get(0).getUsername());
        assertEquals("user2", dtos.get(1).getUsername());
    }

    @Test
    void testToStringSingle() {
        User user = User.builder().firstName("John").lastName("Doe").build();
        String result = UserMapper.toString(user);
        assertEquals("John Doe", result);
    }

    @Test
    void testToStringList() {
        User user1 = User.builder().firstName("John").lastName("Doe").build();
        User user2 = User.builder().firstName("Jane").lastName("Smith").build();

        List<String> result = UserMapper.toString(List.of(user1, user2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0));
        assertEquals("Jane Smith", result.get(1));
    }
}
