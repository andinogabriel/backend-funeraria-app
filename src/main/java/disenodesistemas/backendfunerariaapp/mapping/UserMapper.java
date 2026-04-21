package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserResponseDto;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = {AddressMapper.class, MobileNumberMapper.class})
public interface UserMapper {

  UserResponseDto toDto(UserEntity entity);

  UserDto toReferenceDto(UserEntity entity);

  UserEntity toReferenceEntity(UserDto dto);

  @BeanMapping(builder = @Builder(disableBuilder = true))
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "encryptedPassword", ignore = true)
  @Mapping(target = "startDate", ignore = true)
  @Mapping(target = "enabled", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "affiliates", ignore = true)
  @Mapping(target = "deceasedList", ignore = true)
  @Mapping(target = "incomes", ignore = true)
  @Mapping(target = "confirmationTokens", ignore = true)
  UserEntity toRegisterEntity(UserRegisterDto dto);

  @AfterMapping
  default void initializeCollections(@MappingTarget final UserEntity entity) {
    if (entity.getAddresses() == null) {
      entity.setAddresses(List.of());
    }
    if (entity.getMobileNumbers() == null) {
      entity.setMobileNumbers(List.of());
    }
    if (entity.getAffiliates() == null) {
      entity.setAffiliates(new ArrayList<>());
    }
    if (entity.getDeceasedList() == null) {
      entity.setDeceasedList(new ArrayList<>());
    }
    if (entity.getIncomes() == null) {
      entity.setIncomes(new ArrayList<>());
    }
    if (entity.getConfirmationTokens() == null) {
      entity.setConfirmationTokens(new ArrayList<>());
    }
  }
}
