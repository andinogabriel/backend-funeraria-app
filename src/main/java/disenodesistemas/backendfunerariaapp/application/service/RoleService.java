package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.RolesDto;

import java.util.List;

public interface RoleService {
    List<RolesDto> findAll();
}
