package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobileNumberRepository extends PagingAndSortingRepository<MobileNumberEntity, Long> {

    Optional<MobileNumberEntity> findById(long id);

}
