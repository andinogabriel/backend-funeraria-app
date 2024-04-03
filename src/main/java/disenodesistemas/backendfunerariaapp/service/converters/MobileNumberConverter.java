package disenodesistemas.backendfunerariaapp.service.converters;

import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.nonNull;

@Component(value = "mobileNumberConverter")
public class MobileNumberConverter
    implements AbstractConverter<MobileNumberEntity, MobileNumberRequestDto> {

  @Override
  public MobileNumberEntity fromDto(final MobileNumberRequestDto dto) {
    return nonNull(dto)
        ? MobileNumberEntity.builder().id(dto.getId()).mobileNumber(dto.getMobileNumber()).build()
        : null;
  }

  @Override
  public MobileNumberRequestDto toDTO(final MobileNumberEntity entity) {
    return nonNull(entity)
        ? MobileNumberRequestDto.builder()
            .id(entity.getId())
            .mobileNumber(entity.getMobileNumber())
            .build()
        : null;
  }

  @Override
  public List<MobileNumberEntity> fromDTOs(
      final List<MobileNumberRequestDto> mobileNumberRequestDtos) {
    return AbstractConverter.super.fromDTOs(mobileNumberRequestDtos);
  }
}
