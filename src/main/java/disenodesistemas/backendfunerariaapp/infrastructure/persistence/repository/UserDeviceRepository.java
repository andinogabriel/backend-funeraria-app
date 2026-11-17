package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken);
    Optional<UserDevice> findByUserAndDeviceId(UserEntity user, String deviceId);
    List<UserDevice> findByUserAndIsRefreshActiveTrueOrderByLastSeenAtDesc(UserEntity user);
    Optional<UserDevice> findByIdAndUser(Long id, UserEntity user);
    Optional<UserDevice> findByUserEmailAndDeviceId(String email, String deviceId);
}
