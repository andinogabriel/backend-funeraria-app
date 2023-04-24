package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.DeathCauseResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeathCauseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeathCauseRepository extends JpaRepository<DeathCauseEntity, Long> {
    List<DeathCauseResponseDto> findAllByOrderByNameAsc();
}
