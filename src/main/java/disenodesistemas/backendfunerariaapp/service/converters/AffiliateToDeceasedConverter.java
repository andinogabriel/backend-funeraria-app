package disenodesistemas.backendfunerariaapp.service.converters;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component(value = "affiliateToDeceasedConverter")
@RequiredArgsConstructor
public class AffiliateToDeceasedConverter
    implements AbstractConverter<DeceasedEntity, AffiliateRequestDto> {

  private final ModelMapper mapper;

  @Override
  public DeceasedEntity fromDto(final AffiliateRequestDto affiliateRequestDto) {
    return DeceasedEntity.builder()
        // .registerDate(LocalDateTime.now())
        .affiliated(Boolean.TRUE)
        .deceasedRelationship(
            mapper.map(affiliateRequestDto.getRelationship(), RelationshipEntity.class))
        .gender(mapper.map(affiliateRequestDto.getGender(), GenderEntity.class))
        // .deceasedUser(userService.getUserByEmail(affiliateRequestDto.getUser().getEmail()))
        .dni(affiliateRequestDto.getDni())
        .lastName(affiliateRequestDto.getLastName())
        .firstName(affiliateRequestDto.getFirstName())
        .birthDate(affiliateRequestDto.getBirthDate())
        .build();
  }

  @Override
  public AffiliateRequestDto toDTO(final DeceasedEntity deceasedEntity) {
    return null;
  }
}
