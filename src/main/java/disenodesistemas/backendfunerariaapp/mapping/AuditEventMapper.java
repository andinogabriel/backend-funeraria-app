package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.web.dto.response.AuditEventResponseDto;
import org.mapstruct.Mapper;

/**
 * Translates the immutable {@link AuditEvent} entity into the API response record. The mapping is
 * a one-to-one field copy, so MapStruct generates the implementation without explicit overrides.
 */
@Mapper(config = MapStructConfig.class)
public interface AuditEventMapper {

  AuditEventResponseDto toDto(AuditEvent entity);
}
