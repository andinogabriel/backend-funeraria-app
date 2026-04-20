package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.persistence.repository.RoleRepository;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserPersistencePostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired private UserPersistencePort userPersistencePort;
  @Autowired private RoleRepository roleRepository;

  @Test
  @DisplayName(
      "Given a migrated PostgreSQL database when a user is saved through the persistence port then it can be queried together with its assigned roles")
  void shouldPersistAndReadUsersThroughThePort() {
    final RoleEntity userRole =
        roleRepository.findByName(Role.ROLE_USER).orElseThrow();
    final String email = "integration-" + UUID.randomUUID() + "@example.com";

    final UserEntity user =
        new UserEntity(email, "Integration", "User", "argon2-placeholder-hash");
    user.setEnabled(true);
    user.activate();
    user.setRoles(Set.of(userRole));

    final UserEntity saved = userPersistencePort.save(user);
    final UserEntity reloaded = userPersistencePort.findByEmail(email).orElseThrow();

    assertThat(saved.getId()).isNotNull();
    assertThat(reloaded.getEmail()).isEqualTo(email);
    assertThat(reloaded.getRoles()).extracting(RoleEntity::getName).contains(Role.ROLE_USER);
  }
}
