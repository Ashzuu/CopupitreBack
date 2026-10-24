package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.auth.AuthResponse;
import ashzuu.project.copupitreback.dto.auth.LoginRequest;
import ashzuu.project.copupitreback.dto.auth.RegisterRequest;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegisterUsernameTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("taken");
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertEquals("Nom d'utilisateur déjà pris", ex.getMessage());
    }

    @Test
    void testRegisterEmailTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("free");
        req.setEmail("taken@test.com");
        when(userRepository.existsByUsername("free")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertEquals("Email déjà utilisé", ex.getMessage());
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user");
        req.setEmail("user@test.com");
        req.setPassword("pass");
        req.setFirstName("First");
        req.setLastName("Last");
        req.setMainInstrument("Flute");

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fakeToken");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("fakeToken", resp.getToken());
        assertEquals("user", resp.getUsername());
        assertEquals("USER", resp.getRole());
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void testRegisterWithSecondaryInstruments() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user_instruments");
        req.setEmail("inst@test.com");
        req.setPassword("password123");
        req.setFirstName("Jean");
        req.setLastName("Dupont");
        req.setMainInstrument("Violon");
        req.setSecondaryInstruments("Alto, Violoncelle,  Piano ");

        when(userRepository.existsByUsername("user_instruments")).thenReturn(false);
        when(userRepository.existsByEmail("inst@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId() == null) {
                u.setId(42L);
            }
            return u;
        });
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwtToken");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("jwtToken", resp.getToken());
        assertEquals("user_instruments", resp.getUsername());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture());

        User lastSavedUser = userCaptor.getAllValues().get(1);
        assertNotNull(lastSavedUser.getUserSecondaryInstruments());
        assertEquals(3, lastSavedUser.getUserSecondaryInstruments().size());
        assertEquals("Alto", lastSavedUser.getUserSecondaryInstruments().get(0).getId().getInstrument());
        assertEquals("Violoncelle", lastSavedUser.getUserSecondaryInstruments().get(1).getId().getInstrument());
        assertEquals("Piano", lastSavedUser.getUserSecondaryInstruments().get(2).getId().getInstrument());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("user", "pass");
        User user = User.builder()
                .username("user")
                .email("user@test.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsernameOrEmail("user", "user")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fakeToken");

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("fakeToken", resp.getToken());
        assertEquals("user", resp.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLoginUserNotFound() {
        LoginRequest req = new LoginRequest("nonexistent", "pass");
        when(userRepository.findByUsernameOrEmail("nonexistent", "nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertEquals("Utilisateur non trouvé", ex.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
