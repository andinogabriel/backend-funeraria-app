package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends PagingAndSortingRepository<ItemEntity, Long> {

    ItemEntity findById(long id);

    List<ItemEntity> findAll();

    Page<ItemEntity> findByCategory(Pageable pageableRequest, CategoryEntity categoryEntity);

    List<ItemEntity> findByCategoryOrderByName(CategoryEntity categoryEntity);

    Page<ItemEntity> findAll(Pageable pageableRequest);

    Page<ItemEntity> findByNameContaining(Pageable pageableRequest, String name);

    @Query(value = "SELECT * FROM items WHERE category_id = :categoryId AND name LIKE %:nameToSearch%", countQuery = "SELECT count(*) FROM items WHERE category_id = :categoryId AND name LIKE %:nameToSearch%", nativeQuery = true)
    Page<ItemEntity> findByCategoryAndNameContaining(Pageable pageable, @Param("categoryId") long categoryId, @Param("nameToSearch") String nameToSearch);

}
