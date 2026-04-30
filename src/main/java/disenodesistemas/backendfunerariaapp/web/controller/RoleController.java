package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.role.RoleQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.RolesDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

  private final RoleQueryUseCase roleQueryUseCase;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<RolesDto>> findAll() {
    return ResponseEntity.ok(roleQueryUseCase.findAll());
  }
}
