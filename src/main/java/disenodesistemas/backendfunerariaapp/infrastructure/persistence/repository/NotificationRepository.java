package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.NotificationEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link NotificationEntity}. All listing reads filter by
 * {@code audience} (the alert center is scoped per audience descriptor) and order by
 * {@code created_at desc} so the freshest alerts surface first.
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

  /** Idempotency check used by the consumer before inserting a new row. */
  boolean existsByEventId(UUID eventId);

  /** Primary-key lookup used by the read endpoints to flip {@code read_at}. */
  Optional<NotificationEntity> findById(Long id);

  /**
   * Paginated listing of notifications for the given audience. {@code onlyUnread = true}
   * narrows to rows where {@code read_at is null}; {@code false} returns every row
   * regardless of read state. Order is fixed at {@code created_at desc} so the
   * Pageable's sort is ignored — the SQL ORDER BY is hard-coded.
   */
  @Query(
      """
      select n from NotificationEntity n
      where n.audience = :audience
        and (:onlyUnread = false or n.readAt is null)
      order by n.createdAt desc
      """)
  Page<NotificationEntity> findByAudience(
      @Param("audience") String audience,
      @Param("onlyUnread") boolean onlyUnread,
      Pageable pageable);

  /**
   * Bulk read-flag flip used by the "marcar todo como leido" action. Returns the count
   * of rows affected so the caller can log it.
   */
  @org.springframework.data.jpa.repository.Modifying
  @Query(
      """
      update NotificationEntity n
      set n.readAt = :now
      where n.audience = :audience
        and n.readAt is null
      """)
  int markAllAsRead(@Param("audience") String audience, @Param("now") java.time.Instant now);

  /** Unread count for the bell badge. */
  @Query(
      """
      select count(n) from NotificationEntity n
      where n.audience = :audience
        and n.readAt is null
      """)
  long countUnread(@Param("audience") String audience);
}
