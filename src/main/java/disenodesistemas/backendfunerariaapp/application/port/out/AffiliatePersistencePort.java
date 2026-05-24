package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AffiliatePersistencePort {

  Optional<AffiliateEntity> findByDni(Integer dni);

  Boolean existsByDni(Integer dni);

  List<AffiliateEntity> findByUserEmailOrderByStartDateDesc(String email);

  List<AffiliateEntity> findAllByOrderByStartDateDesc();

  List<AffiliateEntity> findAllByDeceasedFalseOrderByStartDateDesc();

  List<AffiliateEntity> searchByFirstNameOrLastNameOrDni(String valueToSearch);

  /**
   * Filtered + paginated read for the operator UI. Sentinel contract: callers pass {@code ""}
   * for inactive string filters and {@code null} for inactive date filters. See
   * {@link disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AffiliateRepository#search}
   * for the JPQL behind it.
   */
  Page<AffiliateEntity> search(
      boolean deceased,
      String firstName,
      String lastName,
      String dni,
      String relationshipName,
      LocalDate from,
      LocalDate to,
      Pageable pageable);

  AffiliateEntity save(AffiliateEntity affiliate);

  void delete(AffiliateEntity affiliate);

  /**
   * Filtered + paginated admin-only read of the soft-deleted affiliates ordered
   * most-recent-first. Sentinel contract: callers pass {@code ""} for inactive text
   * filters and {@code null} for inactive {@code deletedAt} bounds. See
   * {@link disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AffiliateRepository#findAllDeleted}
   * for the JPQL behind it.
   */
  Page<AffiliateEntity> findAllDeleted(
      String firstName,
      String lastName,
      String dni,
      String deletedBy,
      Instant deletedFrom,
      Instant deletedTo,
      Pageable pageable);
}
