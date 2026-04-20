package disenodesistemas.backendfunerariaapp.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FuneralRepository extends JpaRepository<Funeral, Long> {
  List<Funeral> findAllByOrderByRegisterDateDesc();
  boolean existsByReceiptNumber(String receiptNumber);
  @Query("SELECT f FROM funeral f JOIN f.deceased d JOIN d.deceasedUser u WHERE u.email = :userEmail ORDER BY f.funeralDate DESC")
  List<Funeral> findFuneralsByUserEmail(@Param("userEmail") String userEmail);
}
