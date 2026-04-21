package disenodesistemas.backendfunerariaapp.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends CrudRepository<BrandEntity, Long> {

    Optional<BrandEntity> findById(Long id);

    List<BrandEntity> findAllByOrderByName();

}
