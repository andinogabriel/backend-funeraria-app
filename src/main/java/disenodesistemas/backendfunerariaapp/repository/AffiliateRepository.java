package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffiliateRepository extends PagingAndSortingRepository<AffiliateEntity, Long> {


    AffiliateEntity findByAffiliateId(String affiliateId);

}
