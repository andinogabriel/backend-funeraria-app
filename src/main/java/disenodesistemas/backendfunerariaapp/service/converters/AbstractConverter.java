package disenodesistemas.backendfunerariaapp.service.converters;

import java.util.List;
import java.util.stream.Collectors;

public interface AbstractConverter<T, DTO> {

  T fromDto(DTO dto);

  DTO toDTO(T t);

  default List<T> fromDTOs(final List<DTO> dtos) {
    return dtos == null || dtos.isEmpty()
        ? List.of()
        : dtos.stream().map(this::fromDto).collect(Collectors.toUnmodifiableList());
  }

  default List<DTO> toDTOs(final List<T> entity) {
    return entity == null || entity.isEmpty()
        ? List.of()
        : entity.stream().map(this::toDTO).collect(Collectors.toUnmodifiableList());
  }
}
