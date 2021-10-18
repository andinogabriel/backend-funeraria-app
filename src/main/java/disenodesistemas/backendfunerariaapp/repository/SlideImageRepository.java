package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.SlideImageResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SlideImageEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlideImageRepository extends CrudRepository<SlideImageEntity, Long> {

    Optional<SlideImageEntity> findById(Long id);

    List<SlideImageResponseDto> findAllProjectedBy();

}
