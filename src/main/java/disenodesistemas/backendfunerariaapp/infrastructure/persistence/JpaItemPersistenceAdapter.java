package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.ItemRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaItemPersistenceAdapter implements ItemPersistencePort {

  private final ItemRepository itemRepository;

  @Override
  public Optional<ItemEntity> findByCode(final String code) {
    return itemRepository.findByCode(code);
  }

  @Override
  public List<ItemEntity> findAllByCodeIn(final List<String> codes) {
    return itemRepository.findAllByCodeIn(codes);
  }

  @Override
  public List<ItemEntity> findAll() {
    return itemRepository.findAll();
  }

  @Override
  public List<ItemEntity> findByCategoryOrderByName(final CategoryEntity categoryEntity) {
    return itemRepository.findByCategoryOrderByName(categoryEntity);
  }

  @Override
  @Transactional
  public ItemEntity save(final ItemEntity item) {
    return itemRepository.save(item);
  }

  @Override
  @Transactional
  public List<ItemEntity> saveAll(final List<ItemEntity> items) {
    return itemRepository.saveAll(items);
  }

  @Override
  @Transactional
  public void delete(final ItemEntity item) {
    itemRepository.delete(item);
  }
}
