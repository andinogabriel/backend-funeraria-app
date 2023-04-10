package disenodesistemas.backendfunerariaapp.service.converters;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;

@Component(value = "deceasedConverter")
@RequiredArgsConstructor
public class DeceasedConverter implements AbstractConverter<DeceasedEntity, DeceasedRequestDto> {

    private final UserService userService;
    private final ModelMapper mapper;

    @Override
    public DeceasedEntity fromDto(final DeceasedRequestDto dto) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        return DeceasedEntity.builder()
                .dni(dto.getDni())
                .birthDate(dto.getBirthDate())
                .deathDate(dto.getDeathDate())
                .deceasedUser(nonNull(dto.getUser()) ? userService.getUserByEmail(dto.getUser().getEmail()) :
                        userService.getUserByEmail(authentication.getName())
                )
                .deathCause(mapper.map(dto.getDeathCause(), DeathCauseEntity.class))
                .gender(mapper.map(dto.getGender(), GenderEntity.class))
                .placeOfDeath(mapper.map(dto.getPlaceOfDeath(), AddressEntity.class))
                .deceasedRelationship(mapper.map(dto.getUserRelationship(), RelationshipEntity.class))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .affiliated(Boolean.FALSE)
                .build();
    }

    @Override
    public DeceasedRequestDto toDTO(final DeceasedEntity entity) {
        return null;
    }
}
