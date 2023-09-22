package disenodesistemas.backendfunerariaapp.dto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RelationshipDtoMother {

    public static RelationshipDto getParentRelationship() {
        return RelationshipDto.builder()
                .id(1L)
                .name("Padre")
                .build();
    }
}
