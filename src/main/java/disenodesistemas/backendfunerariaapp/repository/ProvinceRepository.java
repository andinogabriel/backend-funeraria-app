package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceRepository extends PagingAndSortingRepository<ProvinceEntity, Long> {

    Optional<ProvinceEntity> findById(Long id);

    List<ProvinceResponseDto> findAllByOrderByName();
}
