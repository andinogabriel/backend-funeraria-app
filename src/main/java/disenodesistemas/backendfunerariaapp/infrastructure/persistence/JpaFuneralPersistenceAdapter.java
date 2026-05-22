package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.FuneralRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaFuneralPersistenceAdapter implements FuneralPersistencePort {

  private final FuneralRepository funeralRepository;

  @Override
  public Optional<Funeral> findById(final Long id) {
    return funeralRepository.findById(id);
  }

  @Override
  public List<Funeral> findAllByOrderByRegisterDateDesc() {
    return funeralRepository.findAllByOrderByRegisterDateDesc();
  }

  @Override
  public List<Funeral> findFuneralsByUserEmail(final String userEmail) {
    return funeralRepository.findFuneralsByUserEmail(userEmail);
  }

  @Override
  public boolean existsByReceiptNumber(final String receiptNumber) {
    return funeralRepository.existsByReceiptNumber(receiptNumber);
  }

  @Override
  public Page<Funeral> search(
      final String deceasedName,
      final String dni,
      final String receiptNumber,
      final String planName,
      final LocalDateTime from,
      final LocalDateTime to,
      final Pageable pageable) {
    return funeralRepository.search(
        deceasedName, dni, receiptNumber, planName, from, to, pageable);
  }

  @Override
  @Transactional
  public Funeral save(final Funeral funeral) {
    return funeralRepository.save(funeral);
  }

  @Override
  @Transactional
  public void delete(final Funeral funeral) {
    funeralRepository.delete(funeral);
  }
}
