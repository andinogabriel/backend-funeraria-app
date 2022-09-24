package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends CrudRepository<BrandEntity, Long> {

    Optional<BrandEntity> findById(Long id);

    List<BrandResponseDto> findAllByOrderByName();

}
