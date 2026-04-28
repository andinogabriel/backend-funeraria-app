package disenodesistemas.backendfunerariaapp.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  /**
   * Loads the user aggregate by email and eagerly fetches the {@code roles} association in the
   * same query. The auth flow ({@code UserAuthenticationService}, {@code loadUserByUsername} and
   * the JWT issuance pipeline) reads {@code user.getRoles()} every login, so resolving it via an
   * {@link EntityGraph} keeps the path N+1-free now that {@code roles} is mapped as {@code LAZY}.
   */
  @EntityGraph(attributePaths = "roles")
  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findById(Long id);

  Page<UserEntity> findAll(Pageable pageable);

  List<UserEntity> findAllByOrderByStartDateDesc();
}
