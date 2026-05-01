package disenodesistemas.backendfunerariaapp.application.usecase.role;

import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.config.CacheConfig;
import disenodesistemas.backendfunerariaapp.mapping.RoleMapper;
import disenodesistemas.backendfunerariaapp.web.dto.RolesDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleQueryUseCase {

  private final RolePersistencePort rolePersistencePort;
  private final RoleMapper roleMapper;

  @Cacheable(CacheConfig.ROLE_CACHE)
  @Transactional(readOnly = true)
  public List<RolesDto> findAll() {
    return rolePersistencePort.findAll().stream().map(roleMapper::toDto).toList();
  }
}
