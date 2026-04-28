package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken);
    Optional<UserDevice> findByUserId(Long userId);
    Optional<UserDevice> findByUser(UserEntity user);
    Optional<UserDevice> findByUserEmailAndDeviceId(String email, String deviceId);
}
