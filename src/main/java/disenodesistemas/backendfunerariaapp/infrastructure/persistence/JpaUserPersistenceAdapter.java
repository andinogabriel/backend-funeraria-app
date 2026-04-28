package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.UserRepository;
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
public class JpaUserPersistenceAdapter implements UserPersistencePort {

  private final UserRepository userRepository;

  @Override
  public Optional<UserEntity> findById(final Long id) {
    return userRepository.findById(id);
  }

  @Override
  public Optional<UserEntity> findByEmail(final String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public Page<UserEntity> findAll(final Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Override
  public List<UserEntity> findAllByOrderByStartDateDesc() {
    return userRepository.findAllByOrderByStartDateDesc();
  }

  @Override
  @Transactional
  public UserEntity save(final UserEntity user) {
    return userRepository.save(user);
  }
}
