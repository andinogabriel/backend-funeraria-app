package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeceasedRepository extends JpaRepository<DeceasedEntity, Long> {
    Optional<DeceasedEntity> findByDni(Integer dni);
    List<DeceasedEntity> findAllByOrderByRegisterDateDesc();
    boolean existsByDni(Integer dni);
}
