package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptTypeRepository extends JpaRepository<ReceiptTypeEntity, Long> {

    List<ReceiptTypeEntity> findAllByOrderByName();

    ReceiptTypeEntity findById(long id);

}
