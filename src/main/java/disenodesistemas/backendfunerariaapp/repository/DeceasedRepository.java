package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeceasedRepository extends JpaRepository<DeceasedEntity, Long> {
    Optional<DeceasedEntity> findByDni(Integer dni);
    List<DeceasedResponseDto> findAllByOrderByRegisterDateDesc();
    boolean existsByDni(Integer dni);
}
