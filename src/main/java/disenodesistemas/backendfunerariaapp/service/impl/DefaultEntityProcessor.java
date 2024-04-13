package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.service.EntityProcessor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultEntityProcessor<E, D> implements EntityProcessor<E, D> {

  @Override
  public List<E> getDeletedEntities(
      List<E> entities, List<D> requestDtoList, Function<E, D> entityToDtoConverter) {
    if (entities.isEmpty()) {
      return List.of();
    }
    final Set<D> requestDtoSet = new HashSet<>(requestDtoList);
    return entities.stream()
        .filter(entity -> !requestDtoSet.contains(entityToDtoConverter.apply(entity)))
        .collect(Collectors.toUnmodifiableList());
  }
}
