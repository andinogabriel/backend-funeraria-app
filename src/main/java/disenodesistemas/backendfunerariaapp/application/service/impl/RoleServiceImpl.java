package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.RoleService;
import disenodesistemas.backendfunerariaapp.application.usecase.role.RoleQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.RolesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

  private final RoleQueryUseCase roleQueryUseCase;

  @Override
  public List<RolesDto> findAll() {
    return roleQueryUseCase.findAll();
  }
}
