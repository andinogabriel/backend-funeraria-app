package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateRepository extends JpaRepository<AffiliateEntity, Long> {
    Optional<AffiliateEntity> findByDni(final Integer dni);
    Boolean existsAffiliateEntitiesByDni(Integer dni);
    List<AffiliateResponseDto> findByUserOrderByStartDateDesc(final UserEntity userEntity);
    List<AffiliateResponseDto> findAllByOrderByStartDateDesc();
}
