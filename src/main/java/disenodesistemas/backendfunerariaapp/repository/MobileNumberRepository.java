package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MobileNumberRepository extends PagingAndSortingRepository<MobileNumberEntity, Long> {

    MobileNumberEntity findById(long id);

    List<MobileNumberEntity> findBySupplierNumber(SupplierEntity supplierNumber);


}
