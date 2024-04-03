package disenodesistemas.backendfunerariaapp.service.converters;

import static java.util.Objects.nonNull;

import disenodesistemas.backendfunerariaapp.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.CityDto;
import disenodesistemas.backendfunerariaapp.dto.request.ProvinceDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component(value = "addressConverter")
@RequiredArgsConstructor
public class AddressConverter implements AbstractConverter<AddressEntity, AddressRequestDto> {

  @Override
  public AddressEntity fromDto(final AddressRequestDto dto) {
    return nonNull(dto)
        ? AddressEntity.builder()
            .apartment(dto.getApartment())
            .blockStreet(dto.getBlockStreet())
            .flat(dto.getFlat())
            .city(
                CityEntity.builder()
                    .id(dto.getCity().getId())
                    .zipCode(dto.getCity().getZipCode())
                    .name(dto.getCity().getName())
                    .province(
                        ProvinceEntity.builder()
                            .code31662(
                                nonNull(dto.getCity().getProvince())
                                    ? dto.getCity().getProvince().getCode31662()
                                    : null)
                            .name(
                                nonNull(dto.getCity().getProvince())
                                    ? dto.getCity().getProvince().getName()
                                    : null)
                            .id(
                                nonNull(dto.getCity().getProvince())
                                    ? dto.getCity().getProvince().getId()
                                    : null)
                            .build())
                    .build())
            .id(nonNull(dto.getId()) ? dto.getId() : null)
            .streetName(dto.getStreetName())
            .build()
        : null;
  }

  @Override
  public AddressRequestDto toDTO(final AddressEntity entity) {
    return nonNull(entity)
        ? AddressRequestDto.builder()
            // .id(entity.getId())
            .apartment(entity.getApartment())
            .blockStreet(entity.getBlockStreet())
            .city(
                CityDto.builder()
                    .id(entity.getCity().getId())
                    .name(entity.getCity().getName())
                    .zipCode(entity.getCity().getZipCode())
                    .province(
                        ProvinceDto.builder()
                            .id(entity.getCity().getProvince().getId())
                            .name(entity.getCity().getProvince().getName())
                            .code31662(entity.getCity().getProvince().getCode31662())
                            .build())
                    .build())
            .flat(entity.getFlat())
            .streetName(entity.getStreetName())
            .build()
        : null;
  }

  @Override
  public List<AddressEntity> fromDTOs(final List<AddressRequestDto> addressRequestDtos) {
    return AbstractConverter.super.fromDTOs(addressRequestDtos);
  }
}
