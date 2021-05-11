package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends CrudRepository<BrandEntity, Long> {

    BrandEntity findById(long id);

    Page<BrandEntity> findAll(Pageable pageable);

    Page<BrandEntity> findByNameContaining(Pageable pageableRequest, String name);

    List<BrandEntity> findAllByOrderByName();

}
