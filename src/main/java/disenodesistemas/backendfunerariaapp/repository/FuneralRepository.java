package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;
import disenodesistemas.backendfunerariaapp.entities.Funeral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuneralRepository extends JpaRepository<Funeral, Long> {
    List<FuneralResponseDto> findAllByOrderByRegisterDateDesc();
    boolean existsByReceiptNumber(String receiptNumber);

    @Query("SELECT f FROM funeral f JOIN f.deceased d JOIN d.deceasedUser u WHERE u.email = :userEmail ORDER BY f.funeralDate DESC")
    List<FuneralResponseDto> findFuneralsByUserEmail(@Param("userEmail") String userEmail);
}
