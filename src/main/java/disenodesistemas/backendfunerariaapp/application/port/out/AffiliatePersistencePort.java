package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import java.util.List;
import java.util.Optional;

public interface AffiliatePersistencePort {

  Optional<AffiliateEntity> findByDni(Integer dni);

  Boolean existsByDni(Integer dni);

  List<AffiliateEntity> findByUserEmailOrderByStartDateDesc(String email);

  List<AffiliateEntity> findAllByOrderByStartDateDesc();

  List<AffiliateEntity> findAllByDeceasedFalseOrderByStartDateDesc();

  List<AffiliateEntity> searchByFirstNameOrLastNameOrDni(String valueToSearch);

  AffiliateEntity save(AffiliateEntity affiliate);

  void delete(AffiliateEntity affiliate);
}
