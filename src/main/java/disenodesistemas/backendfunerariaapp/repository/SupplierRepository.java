package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends PagingAndSortingRepository<SupplierEntity, Long> {

    Optional<SupplierEntity> findById(Long id);

    List<SupplierResponseDto> findAllProjectedByOrderByIdDesc();

}
