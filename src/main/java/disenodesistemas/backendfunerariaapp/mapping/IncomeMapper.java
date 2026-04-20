package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface IncomeMapper {

  IncomeResponseDto toDto(IncomeEntity entity);

  @BeanMapping(builder = @Builder(disableBuilder = true))
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "receiptNumber", ignore = true)
  @Mapping(target = "receiptSeries", ignore = true)
  @Mapping(target = "incomeDate", ignore = true)
  @Mapping(target = "totalAmount", ignore = true)
  @Mapping(target = "receiptType", ignore = true)
  @Mapping(target = "supplier", ignore = true)
  @Mapping(target = "incomeUser", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  @Mapping(target = "incomeDetails", ignore = true)
  IncomeEntity toEntity(IncomeRequestDto dto);

  @BeanMapping(builder = @Builder(disableBuilder = true))
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "receiptNumber", ignore = true)
  @Mapping(target = "receiptSeries", ignore = true)
  @Mapping(target = "incomeDate", ignore = true)
  @Mapping(target = "totalAmount", ignore = true)
  @Mapping(target = "receiptType", ignore = true)
  @Mapping(target = "supplier", ignore = true)
  @Mapping(target = "incomeUser", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  @Mapping(target = "incomeDetails", ignore = true)
  void updateEntity(IncomeRequestDto dto, @MappingTarget IncomeEntity entity);
}
