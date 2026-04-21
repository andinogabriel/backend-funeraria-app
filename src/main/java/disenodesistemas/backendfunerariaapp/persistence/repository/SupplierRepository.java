package disenodesistemas.backendfunerariaapp.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {

    Optional<SupplierEntity> findByNif(String nif);

    List<SupplierEntity> findAllByOrderByIdDesc();

}
