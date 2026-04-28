package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateRepository extends JpaRepository<AffiliateEntity, Long> {
    Optional<AffiliateEntity> findByDni(final Integer dni);
    Boolean existsAffiliateEntitiesByDni(Integer dni);
    List<AffiliateEntity> findByUserOrderByStartDateDesc(final UserEntity userEntity);
    List<AffiliateEntity> findByUserEmailOrderByStartDateDesc(String email);
    List<AffiliateEntity> findAllByOrderByStartDateDesc();
    List<AffiliateEntity> findAllByDeceasedFalseOrderByStartDateDesc();
    @Query("""
        SELECT a FROM affiliates a
        WHERE lower(a.firstName) LIKE lower(concat('%', :valueToSearch, '%'))
           OR lower(a.lastName) LIKE lower(concat('%', :valueToSearch, '%'))
           OR str(a.dni) LIKE concat('%', :valueToSearch, '%')
        ORDER BY a.startDate DESC
        """)
    List<AffiliateEntity> searchByFirstNameOrLastNameOrDni(@Param("valueToSearch") String valueToSearch);
}
