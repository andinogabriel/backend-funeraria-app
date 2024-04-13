package disenodesistemas.backendfunerariaapp.service;

import java.util.List;
import java.util.function.Function;

public interface EntityProcessor<E, D> {
  List<E> getDeletedEntities(
      List<E> entities, List<D> requestDtoList, Function<E, D> entityToDtoConverter);
}
