package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.auth.AuthResponse;
import ashzuu.project.copupitreback.dto.auth.LoginRequest;
import ashzuu.project.copupitreback.dto.auth.RegisterRequest;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * <h1>AuthService</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> Service gérant la logique métier de l'authentification (connexion et inscription).</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Centraliser la logique de hachage de mot de passe, de création d'utilisateur, de vérification des identifiants et de génération de token JWT.</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle utilise UserRepository, PasswordEncoder, AuthenticationManager, et JwtUtil. Elle est appelée par AuthController.</p>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Nom d'utilisateur déjà pris");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setMainInstrument(request.getMainInstrument());
        user.setMusicalDescription(request.getMusicalDescription());
        user.setYearsOfPractice(request.getYearsOfPractice());
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now());

        userRepository.save(user);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
