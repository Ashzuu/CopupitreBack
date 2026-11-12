package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.musician.AssociateMusicianUserRequest;
import ashzuu.project.copupitreback.dto.musician.CreateMusicianRequest;
import ashzuu.project.copupitreback.dto.musician.CsvImportResultDTO;
import ashzuu.project.copupitreback.dto.musician.MusicianDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.organization.Musician;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.MusicianRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.mapper.MusicianMapper;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.*;

@Service
public class MusicianService {

    private final MusicianRepository musicianRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public MusicianService(
            MusicianRepository musicianRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository
    ) {
        this.musicianRepository = musicianRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Récupère la liste des musiciens appartenant à une organisation.
     * Pour les administrateurs de l'ensemble, renvoie la liste complète (avec email et compte lié).
     * Pour les visiteurs ou utilisateurs non-administrateurs (consultation publique), masque les emails conformément au RGPD.
     */
    public List<MusicianDTO> getOrganizationMusicians(Long orgId, UserDetailsImpl userDetails) {
        if (!organizationRepository.existsById(orgId)) {
            throw new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId);
        }

        List<Musician> musicians = musicianRepository.findAllByOrganizationId(orgId);
        boolean isAdmin = userDetails != null && organizationRepository.isAdminOfOrganization(userDetails.getId(), orgId);

        if (isAdmin) {
            return MusicianMapper.toDtoList(musicians);
        } else {
            return musicians.stream().map(m -> MusicianDTO.builder()
                    .id(m.getId())
                    .firstName(m.getFirstName())
                    .lastName(m.getLastName())
                    .email(null) // RGPD : pas d'exposition d'email public
                    .instrument(m.getInstrument())
                    .userId(null)
                    .username(null)
                    .createdAt(m.getCreatedAt())
                    .build()
            ).toList();
        }
    }

    /**
     * Ajoute manuellement un musicien à une organisation (RG-ORG-11, US-ORG-10).
     */
    @Transactional
    public MusicianDTO addMusician(Long orgId, CreateMusicianRequest req, UserDetailsImpl userDetails) {
        checkAdminAccess(orgId, userDetails);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId));

        String firstName = req.getFirstName().trim();
        String lastName = req.getLastName().trim();
        String email = req.getEmail().trim().toLowerCase();
        String instrument = req.getInstrument().trim();

        Optional<Musician> existingOpt = musicianRepository
                .findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndEmailIgnoreCase(lastName, firstName, email);

        Musician musician;
        if (existingOpt.isPresent()) {
            musician = existingOpt.get();
            if (musician.getOrganizations().stream().anyMatch(o -> o.getId().equals(orgId))) {
                throw new IllegalArgumentException("Ce musicien fait déjà partie de cette organisation.");
            }
            musician.getOrganizations().add(org);
            // Si l'instrument a été mis à jour
            if (instrument != null && !instrument.isBlank()) {
                musician.setInstrument(instrument);
            }
        } else {
            musician = Musician.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .instrument(instrument)
                    .organizations(new HashSet<>(Collections.singletonList(org)))
                    .build();

            // Si l'email correspond à un utilisateur existant dans users, on l'associe automatiquement
            userRepository.findByEmail(email).ifPresent(musician::setUser);
        }

        Musician saved = musicianRepository.save(musician);
        return MusicianMapper.toDto(saved);
    }

    /**
     * Importe des musiciens depuis un contenu CSV (RG-ORG-10, US-ORG-09).
     */
    @Transactional
    public CsvImportResultDTO importMusiciansCsv(Long orgId, String csvContent, UserDetailsImpl userDetails) {
        checkAdminAccess(orgId, userDetails);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId));

        if (csvContent == null || csvContent.isBlank()) {
            throw new IllegalArgumentException("Le fichier CSV est vide.");
        }

        CsvImportResultDTO result = new CsvImportResultDTO();
        List<MusicianDTO> importedMusicians = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Le fichier CSV est vide.");
            }

            System.out.println(headerLine);

            // Supprimer l'éventuel BOM UTF-8
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            // Détection du délimiteur (, ou ;)
            String delimiter = headerLine.contains(";") ? ";" : ",";
            String[] headers = headerLine.split(delimiter);

            int lastNameCol = -1;
            int firstNameCol = -1;
            int emailCol = -1;
            int instrumentCol = -1;

            for (int i = 0; i < headers.length; i++) {
                String h = cleanHeader(headers[i]);
                System.out.println(h);
                if (h.equalsIgnoreCase("nom") || h.equalsIgnoreCase("lastname") || h.equalsIgnoreCase("last_name")) {
                    lastNameCol = i;
                } else if (h.equalsIgnoreCase("prenom") || h.equalsIgnoreCase("prénom") || h.equalsIgnoreCase("firstname") || h.equalsIgnoreCase("first_name")) {
                    firstNameCol = i;
                } else if (h.equalsIgnoreCase("email") || h.equalsIgnoreCase("courriel") || h.equalsIgnoreCase("mail")) {
                    emailCol = i;
                } else if (h.equalsIgnoreCase("instrument")) {
                    instrumentCol = i;
                }
            }

            if (lastNameCol == -1 || firstNameCol == -1 || emailCol == -1 || instrumentCol == -1) {
                throw new IllegalArgumentException("Le fichier CSV doit contenir au minimum les colonnes : Nom, Prénom, Email, Instrument.");
            }

            Set<String> processedKeys = new HashSet<>();
            String line;
            int lineNum = 1;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                result.setTotalRows(result.getTotalRows() + 1);
                String[] parts = line.split(delimiter, -1);
                int maxIdx = Math.max(Math.max(lastNameCol, firstNameCol), Math.max(emailCol, instrumentCol));

                if (parts.length <= maxIdx) {
                    result.setErrorCount(result.getErrorCount() + 1);
                    result.getMessages().add("Ligne " + lineNum + " : Nombre de colonnes insuffisant.");
                    continue;
                }

                String lastName = parts[lastNameCol].trim().replace("\"", "");
                String firstName = parts[firstNameCol].trim().replace("\"", "");
                String email = parts[emailCol].trim().replace("\"", "").toLowerCase();
                String instrument = parts[instrumentCol].trim().replace("\"", "");

                if (lastName.isBlank() || firstName.isBlank() || email.isBlank() || instrument.isBlank() || !email.contains("@")) {
                    result.setErrorCount(result.getErrorCount() + 1);
                    result.getMessages().add("Ligne " + lineNum + " : Données invalides (champs obligatoires ou email incorrect).");
                    continue;
                }

                String compositeKey = (lastName + "|" + firstName + "|" + email).toLowerCase();
                if (processedKeys.contains(compositeKey)) {
                    result.setSkippedCount(result.getSkippedCount() + 1);
                    result.getMessages().add("Ligne " + lineNum + " : Doublon ignoré dans le fichier pour " + firstName + " " + lastName + " (" + email + ").");
                    continue;
                }
                processedKeys.add(compositeKey);

                Optional<Musician> existingOpt = musicianRepository
                        .findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndEmailIgnoreCase(lastName, firstName, email);

                Musician musician;
                if (existingOpt.isPresent()) {
                    musician = existingOpt.get();
                    if (musician.getOrganizations().stream().anyMatch(o -> o.getId().equals(orgId))) {
                        result.setSkippedCount(result.getSkippedCount() + 1);
                        result.getMessages().add("Ligne " + lineNum + " : " + firstName + " " + lastName + " fait déjà partie de l'organisation.");
                        continue;
                    }
                    musician.getOrganizations().add(org);
                    if (!instrument.isBlank()) {
                        musician.setInstrument(instrument);
                    }
                } else {
                    musician = Musician.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .instrument(instrument)
                            .organizations(new HashSet<>(Collections.singletonList(org)))
                            .build();

                    userRepository.findByEmail(email).ifPresent(musician::setUser);
                }

                Musician saved = musicianRepository.save(musician);
                result.setImportedCount(result.getImportedCount() + 1);
                importedMusicians.add(MusicianMapper.toDto(saved));
            }

            result.setMusicians(importedMusicians);
            return result;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV : " + e.getMessage(), e);
        }
    }

    /**
     * Supprime un musicien d'une organisation (US-ORG-11).
     * Le musicien est retiré de organization_musicians mais reste dans musicians.
     */
    @Transactional
    public void removeMusician(Long orgId, Long musicianId, UserDetailsImpl userDetails) {
        checkAdminAccess(orgId, userDetails);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId));

        Musician musician = musicianRepository.findById(musicianId)
                .orElseThrow(() -> new UnknownID("Musicien non trouvé avec l'identifiant : " + musicianId));

        boolean removed = musician.getOrganizations().removeIf(o -> o.getId().equals(org.getId()));
        if (!removed) {
            throw new IllegalArgumentException("Ce musicien ne fait pas partie de cette organisation.");
        }

        musicianRepository.save(musician);
    }

    /**
     * Associe un musicien existant à un utilisateur via son adresse email (US-ORG-12).
     */
    @Transactional
    public MusicianDTO associateUserByEmail(Long orgId, Long musicianId, AssociateMusicianUserRequest req, UserDetailsImpl userDetails) {
        checkAdminAccess(orgId, userDetails);

        if (!organizationRepository.existsById(orgId)) {
            throw new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId);
        }

        Musician musician = musicianRepository.findById(musicianId)
                .orElseThrow(() -> new UnknownID("Musicien non trouvé avec l'identifiant : " + musicianId));

        if (musician.getOrganizations().stream().noneMatch(o -> o.getId().equals(orgId))) {
            throw new IllegalArgumentException("Ce musicien ne fait pas partie de cette organisation.");
        }

        String email = req.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnknownID("Aucun compte utilisateur trouvé avec l'adresse email : " + req.getEmail()));

        musician.setUser(user);
        Musician saved = musicianRepository.save(musician);
        return MusicianMapper.toDto(saved);
    }

    private void checkAdminAccess(Long orgId, UserDetailsImpl userDetails) {
        if (userDetails == null || !organizationRepository.isAdminOfOrganization(userDetails.getId(), orgId)) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
        }
    }

    private String cleanHeader(String header) {
        if (header == null) return "";

        String cleaned = header
                .replace("\u00A0", " ").trim()
                .replace("\"", "")
                .replace("'", "").toLowerCase();

        cleaned = Normalizer.normalize(cleaned, Normalizer.Form.NFD);
        cleaned = cleaned.replaceAll("\\p{InCombiningDiacriticalMarks}", "");

        cleaned = cleaned.replaceAll("\\s+", "");

        return cleaned;
    }
}
