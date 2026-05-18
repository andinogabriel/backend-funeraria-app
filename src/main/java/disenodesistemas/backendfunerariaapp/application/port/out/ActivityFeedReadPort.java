package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.ActivityLogEntry;
import java.util.List;

/**
 * Outbound port the activity-feed query use case reads through. Keeping a thin port in front
 * of the JPA repository preserves the architectural rule that application code talks to
 * {@code ..application.port..} interfaces, not to {@code ..infrastructure..} classes
 * (enforced by {@code ArchitectureGuardrailsTest.application_business_code_must_not_depend_on_repositories_or_adapters}).
 *
 * <p>The port returns the domain entity directly because {@link ActivityLogEntry} already
 * lives in the {@code domain.entity} package and carries no JPA-managed associations the use
 * case would have to be careful about — the read path is intentionally a flat projection.
 */
public interface ActivityFeedReadPort {

  /**
   * Returns the most recent activity entries, newest first. The supplied {@code limit} is
   * the maximum number of rows the caller wants; the implementation must honour it exactly
   * (the use case already clamps it to a safe range upstream).
   */
  List<ActivityLogEntry> findLatest(int limit);
}
