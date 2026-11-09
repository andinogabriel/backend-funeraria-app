package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface DeceasedMapper {

  @Mapping(target = "coverageReasons", source = "coverageReasons", qualifiedByName = "splitReasons")
  DeceasedResponseDto toDto(DeceasedEntity entity);

  /** Stored as comma-separated codes; exposed as a list, or null when there is none. */
  @Named("splitReasons")
  default List<String> splitReasons(final String reasons) {
    return reasons == null || reasons.isBlank() ? null : List.of(reasons.split(","));
  }

  // The cause is a reference by id: its catalog flags are never taken from the request body.
  @Mapping(target = "deathCause.accidental", ignore = true)
  DeceasedEntity toEntity(DeceasedRequestDto dto);

  @Mapping(target = "deathCause.accidental", ignore = true)
  void updateEntity(DeceasedRequestDto dto, @MappingTarget DeceasedEntity entity);
}
