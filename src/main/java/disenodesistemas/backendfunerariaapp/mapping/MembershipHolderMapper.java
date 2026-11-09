package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderResponseDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface MembershipHolderMapper {

  MembershipHolderResponseDto toDto(MembershipHolderEntity entity);
}
