package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.musician.MusicianDTO;
import ashzuu.project.copupitreback.jpa.entities.organization.Musician;

import java.util.Collections;
import java.util.List;

public class MusicianMapper {

    public static MusicianDTO toDto(Musician musician) {
        if (musician == null) {
            return null;
        }

        return MusicianDTO.builder()
                .id(musician.getId())
                .firstName(musician.getFirstName())
                .lastName(musician.getLastName())
                .email(musician.getEmail())
                .instrument(musician.getInstrument())
                .userId(musician.getUser() != null ? musician.getUser().getId() : null)
                .username(musician.getUser() != null ? musician.getUser().getUsername() : null)
                .createdAt(musician.getCreatedAt())
                .build();
    }

    public static List<MusicianDTO> toDtoList(List<Musician> musicians) {
        if (musicians == null) {
            return Collections.emptyList();
        }
        return musicians.stream().map(MusicianMapper::toDto).toList();
    }
}
