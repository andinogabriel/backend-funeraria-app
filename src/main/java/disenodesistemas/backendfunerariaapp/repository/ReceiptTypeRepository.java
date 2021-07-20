package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptTypeRepository extends JpaRepository<ReceiptTypeEntity, Long> {

    List<ReceiptTypeResponseDto> findAllByOrderByName();

    Optional<ReceiptTypeEntity> findById(Long id);

}
