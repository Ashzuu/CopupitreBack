package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.jpa.entities.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Composant responsable de la conversion entre les entités liées aux utilisateurs et le {@link UserDTO}.
 * <p>
 * But : Centraliser la logique de mapping pour isoler la couche base de données de la couche présentation.
 * <p>
 * Liens : Prend en entrée {@link User} et ses instruments secondaires pour produire un {@link UserDTO}.
 */
public class UserMapper {

    public static UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        List<String> secondaryInstruments = (user.getUserSecondaryInstruments() == null)
                ? Collections.emptyList()
                : user.getUserSecondaryInstruments().stream()
                    .map(usi -> usi.getId() != null ? usi.getId().getInstrument() : null)
                    .filter(Objects::nonNull)
                    .toList();

        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .mainInstrument(user.getMainInstrument())
            .secondaryInstruments(secondaryInstruments)
            .musicalDescription(user.getMusicalDescription())
            .yearsOfPractice(user.getYearsOfPractice())
            .build();
    }

    public static List<UserDTO> toDto(List<User> users){
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    public static String toString(User user){
        if (user == null) {
            return "";
        }
        return user.getFirstName() + " " + user.getLastName();
    }

    public static List<String> toString(List<User> users){
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream().map(UserMapper::toString).toList();
    }
}
