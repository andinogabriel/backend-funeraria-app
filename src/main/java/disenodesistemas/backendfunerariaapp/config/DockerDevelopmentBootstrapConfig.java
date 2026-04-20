package disenodesistemas.backendfunerariaapp.config;

import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.persistence.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.persistence.repository.UserRepository;
import java.util.LinkedHashSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bootstraps a minimal development-ready admin account when the application starts with the Docker
 * profile enabled. The configuration keeps local onboarding simple by creating the required roles
 * and administrator user only when they are still missing from the target database.
 */
@Configuration
@Profile("docker")
@Slf4j
@EnableConfigurationProperties(DevelopmentAdminProperties.class)
public class DockerDevelopmentBootstrapConfig {

  /**
   * Registers the startup hook responsible for bootstrapping development credentials in Docker.
   * The hook only becomes active for the Docker profile and when the feature flag allows
   * automatic admin provisioning at application startup.
   */
  @Bean
  @ConditionalOnProperty(
      name = "app.bootstrap.admin.enabled",
      havingValue = "true",
      matchIfMissing = true)
  CommandLineRunner dockerDevelopmentAdminBootstrap(
      final DevelopmentAdminProperties developmentAdminProperties,
      final UserRepository userRepository,
      final RoleRepository roleRepository,
      final PasswordEncoder passwordEncoder) {
    return _ ->
        bootstrapAdmin(
            developmentAdminProperties, userRepository, roleRepository, passwordEncoder);
  }

  /**
   * Creates the development admin account and any missing roles required by that account. The
   * method is intentionally idempotent so repeated container boots do not recreate data that is
   * already present in the database.
   */
  @Transactional
  void bootstrapAdmin(
      final DevelopmentAdminProperties developmentAdminProperties,
      final UserRepository userRepository,
      final RoleRepository roleRepository,
      final PasswordEncoder passwordEncoder) {
    if (userRepository.findByEmail(developmentAdminProperties.email()).isPresent()) {
      log.atInfo()
          .addKeyValue("event", "bootstrap.dev.admin.skipped")
          .addKeyValue("email", developmentAdminProperties.email())
          .addKeyValue("reason", "already_exists")
          .log("bootstrap.dev.admin.skipped");
      return;
    }

    final RoleEntity adminRole =
        roleRepository.findByName(Role.ROLE_ADMIN).orElseGet(() -> roleRepository.save(new RoleEntity(Role.ROLE_ADMIN)));
    final RoleEntity userRole =
        roleRepository.findByName(Role.ROLE_USER).orElseGet(() -> roleRepository.save(new RoleEntity(Role.ROLE_USER)));

    final UserEntity adminUser =
        new UserEntity(
            developmentAdminProperties.email(),
            developmentAdminProperties.firstName(),
            developmentAdminProperties.lastName(),
            passwordEncoder.encode(developmentAdminProperties.password()));
    adminUser.setEnabled(true);
    adminUser.setActive(Boolean.TRUE);
    adminUser.setRoles(new LinkedHashSet<>(java.util.Set.of(adminRole, userRole)));
    userRepository.save(adminUser);

    log.atInfo()
        .addKeyValue("event", "bootstrap.dev.admin.created")
        .addKeyValue("email", developmentAdminProperties.email())
        .addKeyValue("roles", adminUser.getRoles().stream().map(role -> role.getName().name()).toList())
        .addKeyValue("note", "use_app_bootstrap_admin_password_for_login")
        .log("bootstrap.dev.admin.created");
  }
}
