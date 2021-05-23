package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends PagingAndSortingRepository<SupplierEntity, Long> {

    SupplierEntity findById(long id);

    Page<SupplierEntity> findByNameContaining(Pageable pageableRequest, String name);

    Page<SupplierEntity> findAll(Pageable pageableRequest);

    List<SupplierEntity> findAllByOrderByName();

}
