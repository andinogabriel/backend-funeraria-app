package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface GenderRepository extends PagingAndSortingRepository<GenderEntity, Long> {

    GenderEntity findById(long id);

    List<GenderEntity> findAll();


}
