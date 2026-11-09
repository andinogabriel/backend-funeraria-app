package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipHolderPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.MembershipHolderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaMembershipHolderAdapter implements MembershipHolderPersistencePort {

  private final MembershipHolderRepository repository;

  @Override
  public Optional<MembershipHolderEntity> findByDni(final Integer dni) {
    return repository.findByDni(dni);
  }

  @Override
  @Transactional
  public MembershipHolderEntity save(final MembershipHolderEntity holder) {
    return repository.save(holder);
  }

  @Override
  public List<MembershipHolderEntity> search(final String value, final int limit) {
    return repository.search(value, PageRequest.of(0, limit));
  }

  @Override
  public List<AffiliateEntity> findAffiliatesOf(final Long holderId) {
    return repository.findAffiliatesOf(holderId);
  }
}
