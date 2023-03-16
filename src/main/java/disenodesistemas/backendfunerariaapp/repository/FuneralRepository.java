package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;
import disenodesistemas.backendfunerariaapp.entities.Funeral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuneralRepository extends JpaRepository<Funeral, Long> {
    List<FuneralResponseDto> findAllByOrderByRegisterDateDesc();
    boolean existsByReceiptNumber(String receiptNumber);
}
