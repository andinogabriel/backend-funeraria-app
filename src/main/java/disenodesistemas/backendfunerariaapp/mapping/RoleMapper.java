package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.web.dto.RolesDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.RolRequestDto;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface RoleMapper {

  @Mapping(target = "name", source = "name", qualifiedByName = "mapRoleName")
  RolesDto toDto(RoleEntity entity);

  RolRequestDto toRequestDto(RoleEntity entity);

  @Named("mapRoleName")
  default String mapRoleName(final Role role) {
    if (role == null) {
      return null;
    }

    return StringUtils.capitalize(
        role.name().replace("ROLE_", StringUtils.EMPTY).toLowerCase());
  }
}
