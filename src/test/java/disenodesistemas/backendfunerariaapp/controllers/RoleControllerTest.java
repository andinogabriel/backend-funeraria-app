package disenodesistemas.backendfunerariaapp.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.RolesDto;
import disenodesistemas.backendfunerariaapp.enums.Role;
import disenodesistemas.backendfunerariaapp.service.RoleService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

  @Mock private RoleService roleService;
  @InjectMocks private RoleController sut;
  private RolesDto rolesDto;

  @BeforeEach
  void setUp() {
    rolesDto = RolesDto.builder().name(Role.ROLE_ADMIN.name()).build();
  }

  @Test
  void findAll() {
    final List<RolesDto> expectedList = List.of(rolesDto);
    given(roleService.findAll()).willReturn(expectedList);
    final ResponseEntity<List<RolesDto>> actualResult = sut.findAll();
    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResult.getStatusCode()),
        () -> assertEquals(expectedList, actualResult.getBody()));
    then(roleService).should(times(1)).findAll();
  }
}
