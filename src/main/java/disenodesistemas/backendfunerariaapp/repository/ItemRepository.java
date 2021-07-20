package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends PagingAndSortingRepository<ItemEntity, Long> {

    Optional<ItemEntity> findById(Long id);

    List<ItemResponseDto> findAllProjectedBy();

    List<ItemResponseDto> findByCategoryOrderByName(CategoryEntity categoryEntity);

}
