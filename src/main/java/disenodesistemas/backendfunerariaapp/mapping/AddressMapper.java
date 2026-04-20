package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.AddressEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AddressResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface AddressMapper {

  AddressResponseDto toDto(AddressEntity entity);

  AddressEntity toEntity(AddressRequestDto dto);

  void updateEntity(AddressRequestDto dto, @MappingTarget AddressEntity entity);
}
