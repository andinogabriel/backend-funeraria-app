package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserPersistencePort {

  Optional<UserEntity> findById(Long id);

  Optional<UserEntity> findByEmail(String email);

  Page<UserEntity> findAll(Pageable pageable);

  List<UserEntity> findAllByOrderByStartDateDesc();

  UserEntity save(UserEntity user);
}
