package disenodesistemas.backendfunerariaapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.BDDMockito.given;

import disenodesistemas.backendfunerariaapp.dto.RolesDto;
import disenodesistemas.backendfunerariaapp.entities.RoleEntity;
import disenodesistemas.backendfunerariaapp.repository.RoleRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

  @Mock private RoleRepository repository;
  @InjectMocks private RoleServiceImpl sut;
  private List<RoleEntity> roles;
  private ModelMapper modelMapper;

  @BeforeEach
  void setUp() {
    modelMapper = new ModelMapper();
    roles =
        Stream.of(
                RolesDto.builder().id(1L).name("ROLE_ADMIN").build(),
                RolesDto.builder().id(2L).name("ROLE_USER").build())
            .map(rolDto -> modelMapper.map(rolDto, RoleEntity.class))
            .collect(Collectors.toUnmodifiableList());
  }

  @Test
  void findAll() {
    given(repository.findAll()).willReturn(roles);

    final List<RolesDto> actualResult = sut.findAll();

    assertAll(
        () -> assertFalse(actualResult.isEmpty(), "Role list should not be empty"),
        () ->
            assertEquals(
                2,
                adminAndUserCount(actualResult),
                "Role list should at least contain user and admin roles"));
  }

  private long adminAndUserCount(final List<RolesDto> roleList) {
    return roleList.stream()
        .filter(
            role ->
                "ADMIN".equalsIgnoreCase(role.getName()) || "USER".equalsIgnoreCase(role.getName()))
        .count();
  }
}
