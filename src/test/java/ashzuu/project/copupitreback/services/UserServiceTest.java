package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.user.UpdateProfileRequest;
import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrument;
import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrumentId;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetAll() {
        User u1 = User.builder().id(1L).username("u1").build();
        User u2 = User.builder().id(2L).username("u2").build();
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserDTO> result = userService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("u1", result.get(0).getUsername());
        assertEquals("u2", result.get(1).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void testGetByUsernameSuccess() {
        User u = User.builder().id(1L).username("u1").build();
        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(u));

        UserDTO dto = userService.getByUsername("u1");

        assertNotNull(dto);
        assertEquals("u1", dto.getUsername());
        verify(userRepository).findByUsername("u1");
    }

    @Test
    void testGetByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getByUsername("nonexistent");
        });

        assertEquals("Username inconnu", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void testUpdateProfileSuccess() {
        User existingUser = User.builder()
                .id(1L)
                .username("musician")
                .firstName("OldFirst")
                .lastName("OldLast")
                .mainInstrument("Trombone")
                .userSecondaryInstruments(new ArrayList<>())
                .build();

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("NewFirst")
                .lastName("NewLast")
                .mainInstrument("Tuba")
                .musicalDescription("Updated description")
                .yearsOfPractice(12)
                .secondaryInstruments(List.of("Trombone", "Euphonium"))
                .build();

        when(userRepository.findByUsername("musician")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO updated = userService.updateProfile("musician", request);

        assertNotNull(updated);
        assertEquals("NewFirst", updated.getFirstName());
        assertEquals("NewLast", updated.getLastName());
        assertEquals("Tuba", updated.getMainInstrument());
        assertEquals("Updated description", updated.getMusicalDescription());
        assertEquals(12, updated.getYearsOfPractice());
        assertEquals(2, updated.getSecondaryInstruments().size());
        assertTrue(updated.getSecondaryInstruments().contains("Trombone"));
        assertTrue(updated.getSecondaryInstruments().contains("Euphonium"));
        verify(userRepository).save(existingUser);
    }

    @Test
    void testUpdateProfileNotFound() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("NewFirst")
                .lastName("NewLast")
                .mainInstrument("Tuba")
                .build();

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateProfile("unknown", request));
        verify(userRepository, never()).save(any(User.class));
    }
}
