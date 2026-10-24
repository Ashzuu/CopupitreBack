package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.user.UpdateProfileRequest;
import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrument;
import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrumentId;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service gérant la logique métier liée aux utilisateurs.
 * <p>
 * Ce service fournit des méthodes pour interagir avec les données des utilisateurs,
 * en utilisant le {@link UserRepository} pour accéder à la base de données.
 * </p>
 */
@Service
public class UserService {

    /**
     * Dépôt permettant de communiquer avec la table des utilisateurs en base de données.
     */
    private final UserRepository repo;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param repo le dépôt des utilisateurs
     */
    public UserService(UserRepository repo){
        this.repo = repo;
    }

    /**
     * Récupère l'ensemble des utilisateurs de l'application sous forme de DTO.
     *
     * @return la liste de tous les utilisateurs sous forme de {@link UserDTO}
     */
    public List<UserDTO> getAll(){
        return UserMapper.toDto(repo.findAll());
    }

    /**
     * Récupère un utilisateur spécifique à partir de son nom d'utilisateur.
     * <p>
     * Cette méthode recherche l'utilisateur en base de données et renvoie son DTO
     * s'il est trouvé. Sinon, elle lève une exception.
     * </p>
     *
     * @param username le nom d'utilisateur recherché
     * @return les informations de l'utilisateur sous forme de {@link UserDTO}
     * @throws RuntimeException si aucun utilisateur avec ce nom d'utilisateur n'est trouvé
     */
    public UserDTO getByUsername(String username){
        Optional<User> user = this.repo.findByUsername(username);
        if(user.isEmpty()){
            throw new RuntimeException("Username inconnu");
        }
        return UserMapper.toDto(user.get());
    }

    /**
     * Met à jour les informations de profil de l'utilisateur connecté.
     *
     * @param username le nom d'utilisateur de l'utilisateur connecté
     * @param request  les nouvelles informations du profil
     * @return les informations mises à jour sous forme de {@link UserDTO}
     * @throws RuntimeException si l'utilisateur n'est pas trouvé
     */
    @Transactional
    public UserDTO updateProfile(String username, UpdateProfileRequest request) {
        User user = this.repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username inconnu"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMainInstrument(request.getMainInstrument());
        user.setMusicalDescription(request.getMusicalDescription());
        user.setYearsOfPractice(request.getYearsOfPractice());

        if (user.getUserSecondaryInstruments() == null) {
            user.setUserSecondaryInstruments(new ArrayList<>());
        } else {
            user.getUserSecondaryInstruments().clear();
        }

        if (request.getSecondaryInstruments() != null) {
            List<UserSecondaryInstrument> secondaryList = request.getSecondaryInstruments().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .map(instrumentName -> {
                        UserSecondaryInstrument usi = new UserSecondaryInstrument();
                        usi.setId(new UserSecondaryInstrumentId(user.getId(), instrumentName));
                        usi.setUser(user);
                        return usi;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
            user.getUserSecondaryInstruments().addAll(secondaryList);
        }

        User updated = this.repo.save(user);
        return UserMapper.toDto(updated);
    }
}
