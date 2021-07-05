package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateRepository extends PagingAndSortingRepository<AffiliateEntity, Long> {

    Optional<AffiliateEntity> findById(long id);

    List<AffiliateResponseDto> findByUserOrderByStartDateDesc(UserEntity userEntity);

    Page<AffiliateResponseDto> findAllProjectedBy(Pageable pageable);

}
