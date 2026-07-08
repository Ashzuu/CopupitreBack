package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
