package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationTokenEntity, Long> {

    ConfirmationTokenEntity findByToken(String token);
    ConfirmationTokenEntity findByUser(UserEntity user);

}
