package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.auth.AuthResponse;
import ashzuu.project.copupitreback.dto.auth.LoginRequest;
import ashzuu.project.copupitreback.dto.auth.RegisterRequest;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrument;
import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrumentId;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service gérant la logique métier de l'authentification (connexion et inscription).
 * <p>
 * Ce service centralise la logique de hachage de mot de passe, de création d'utilisateur,
 * de vérification des identifiants et de génération de token JWT.
 * </p>
 */
@Service
public class AuthService {

    /**
     * Dépôt permettant de communiquer avec la table des utilisateurs en base de données.
     */
    private final UserRepository userRepository;

    /**
     * Encodeur de mots de passe pour sécuriser les mots de passe des utilisateurs en base de données.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Utilitaire de gestion des tokens JWT (génération, validation).
     */
    private final JwtUtil jwtUtil;

    /**
     * Gestionnaire d'authentification de Spring Security pour valider les identifiants de connexion.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param userRepository        le dépôt des utilisateurs
     * @param passwordEncoder       l'encodeur de mots de passe
     * @param jwtUtil               l'utilitaire JWT
     * @param authenticationManager le gestionnaire d'authentification
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Enregistre un nouvel utilisateur dans le système.
     * <p>
     * Cette méthode vérifie d'abord que le nom d'utilisateur et l'adresse email ne sont pas déjà pris,
     * crée ensuite l'entité {@link User} avec le mot de passe haché, la sauvegarde en base de données,
     * associe les instruments secondaires éventuels,
     * et génère enfin un jeton JWT pour connecter automatiquement l'utilisateur nouvellement inscrit.
     * </p>
     *
     * @param request les informations d'inscription fournies par le client
     * @return un objet {@link AuthResponse} contenant le jeton JWT généré et les détails de l'utilisateur
     * @throws RuntimeException si le nom d'utilisateur ou l'adresse email est déjà utilisé
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Nom d'utilisateur déjà pris");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user =
            User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .mainInstrument(request.getMainInstrument())
                .musicalDescription(request.getMusicalDescription())
                .yearsOfPractice(request.getYearsOfPractice())
                .role(Role.USER)
                .createdAt(Instant.now())
                .applications(new ArrayList<>())
                .organizationAdmins(new ArrayList<>())
                .userSecondaryInstruments(new ArrayList<>())
                .build();

        final User savedUser = userRepository.save(user);
        User finalUser = savedUser;

        if (request.getSecondaryInstruments() != null && !request.getSecondaryInstruments().isBlank()) {
            List<UserSecondaryInstrument> secondaryList = Arrays.stream(request.getSecondaryInstruments().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .map(instrumentName -> {
                        UserSecondaryInstrument usi = new UserSecondaryInstrument();
                        usi.setId(new UserSecondaryInstrumentId(savedUser.getId(), instrumentName));
                        usi.setUser(savedUser);
                        return usi;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
            savedUser.getUserSecondaryInstruments().addAll(secondaryList);
            finalUser = userRepository.save(savedUser);
        }

        UserDetailsImpl userDetails = new UserDetailsImpl(finalUser);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
            .token(token)
            .username(finalUser.getUsername())
            .role(finalUser.getRole().name())
            .build();
    }

    /**
     * Authentifie un utilisateur existant avec ses identifiants de connexion.
     * <p>
     * Cette méthode utilise {@link AuthenticationManager} de Spring Security pour valider
     * la combinaison nom d'utilisateur / mot de passe, puis récupère l'utilisateur depuis
     * la base de données afin de générer un jeton JWT.
     * </p>
     *
     * @param request les identifiants de connexion (nom d'utilisateur et mot de passe)
     * @return un objet {@link AuthResponse} contenant le jeton JWT généré et les détails de l'utilisateur authentifié
     * @throws RuntimeException si l'utilisateur n'est pas trouvé ou si l'authentification échoue
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
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
