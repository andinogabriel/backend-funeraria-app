package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.RefreshToken;
import disenodesistemas.backendfunerariaapp.entities.UserDevice;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken);
    Optional<UserDevice> findByUserId(Long userId);
    Optional<UserDevice> findByUser(UserEntity user);
}
