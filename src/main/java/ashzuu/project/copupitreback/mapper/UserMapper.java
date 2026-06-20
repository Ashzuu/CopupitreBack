package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrument;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Composant responsable de la conversion entre les entités liées aux utilisateurs et le {@link UserDTO}.
 * <p>
 * But : Centraliser la logique de mapping pour isoler la couche base de données de la couche présentation.
 * <p>
 * Liens : Prend en entrée {@link User} et {@link UserSecondaryInstrument} pour produire un {@link UserDTO}.
 */
public class UserMapper {
    // Todo : ajouter les différentes relations (et donc les nouveaux mappers à faire).
    public static UserDTO toDto(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .mainInstrument(user.getMainInstrument())
            .musicalDescription(user.getMusicalDescription())
            .yearsOfPractice(user.getYearsOfPractice())
            .build();
    }

    public static List<UserDTO> toDto(List<User> users){
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }
}
