package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SupplierRepository extends PagingAndSortingRepository<SupplierEntity, Long> {

    SupplierEntity findById(long id);

    List<SupplierEntity> findAll();

}
