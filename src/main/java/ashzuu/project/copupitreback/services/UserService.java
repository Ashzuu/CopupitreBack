package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les opérations liées aux utilisateurs.
 * <p>
 * Rôle : Fournit des méthodes pour interagir avec les données des utilisateurs, en utilisant le {@link UserRepository} pour accéder à la base de données.
 * <p>
 * Méthodes principales :
 * - {@link #getAll()}: Récupère tous les utilisateurs de la base de données.
 * <p>
 * Liens : Utilise {@link UserRepository} pour les opérations CRUD sur l'entité {@link User}.
 */
@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo){
        this.repo = repo;
    }

    public List<UserDTO> getAll(){
        return UserMapper.toDto(repo.findAll());
    }

    public UserDTO getByUsername(String username){
        Optional<User> user = this.repo.findByUsername(username);
        if(user.isEmpty()){
            throw new RuntimeException("Username inconnu");
        }
        return UserMapper.toDto(user.get());
    }
}
