package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.web.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ReceiptTypeResponseDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface ReceiptTypeMapper {

  ReceiptTypeResponseDto toDto(ReceiptTypeEntity entity);

  ReceiptTypeEntity toEntity(ReceiptTypeDto dto);
}
