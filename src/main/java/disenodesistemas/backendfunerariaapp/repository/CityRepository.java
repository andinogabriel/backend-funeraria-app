package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends PagingAndSortingRepository<CityEntity, Long> {

    List<CityResponseDto> findByProvinceOrderByName(ProvinceEntity provinceEntity);

    Optional<CityEntity> findById(long id);

    Optional<CityResponseDto> getById(long id);
}
