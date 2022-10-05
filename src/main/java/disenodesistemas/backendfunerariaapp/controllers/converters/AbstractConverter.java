package disenodesistemas.backendfunerariaapp.controllers.converters;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractConverter<T, DTO> {

    public abstract T fromDto(DTO dto);

    public abstract DTO toDTO(T t);

    public List<T> fromDTOs(final List<DTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        } else {
            return dtos.stream().map(this::fromDto).collect(Collectors.toUnmodifiableList());
        }
    }

    public List<DTO> toDTOs(List<T> ts) {
        if (ts == null || ts.isEmpty()) {
            return List.of();
        } else {
            return ts.stream().map(this::toDTO).collect(Collectors.toUnmodifiableList());
        }
    }

}
