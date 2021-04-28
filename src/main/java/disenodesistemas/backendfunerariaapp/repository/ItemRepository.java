package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends PagingAndSortingRepository<ItemEntity, Long> {

    ItemEntity findById(long id);

    Page<ItemEntity> findByCategoryOrderByName(Pageable pageableRequest, CategoryEntity categoryEntity);

    Page<ItemEntity> findAllByOrderByName(Pageable pageableRequest);

    Page<ItemEntity> findByNameContaining(Pageable pageableRequest, String name);

}
