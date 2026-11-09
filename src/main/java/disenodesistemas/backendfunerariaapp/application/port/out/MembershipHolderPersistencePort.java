package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import java.util.List;
import java.util.Optional;

/** Persistence contract for membership holders ("titulares") and the affiliates they cover. */
public interface MembershipHolderPersistencePort {

  Optional<MembershipHolderEntity> findByDni(Integer dni);

  MembershipHolderEntity save(MembershipHolderEntity holder);

  /**
   * Holders whose DNI starts with, or whose first/last name contains, {@code value}
   * (case-insensitive), ordered by last name. Callers pass a non-blank value.
   */
  List<MembershipHolderEntity> search(String value, int limit);

  /** Affiliates covered by the holder that are not soft-deleted (deceased ones included). */
  List<AffiliateEntity> findAffiliatesOf(Long holderId);
}
