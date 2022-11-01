package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.RolesDto;
import disenodesistemas.backendfunerariaapp.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public List<RolesDto> findAll() {
        return repository.findAll().stream().map(role -> RolesDto.builder()
                .id(role.getId())
                .name(StringUtils.capitalize(role.getName().name().replace(ROLE_PREFIX, "").toLowerCase()))
                .build()
        ).collect(Collectors.toUnmodifiableList());
    }
}
