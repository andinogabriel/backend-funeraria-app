package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RelationshipTestDataFactory {

  public static RelationshipEntity getParentRelationship() {
    final RelationshipEntity parentRelationship = new RelationshipEntity("Padre");
    parentRelationship.setId(1L);
    return parentRelationship;
  }

  public static RelationshipEntity getGrandMotherRelationshipEntity() {
    final RelationshipEntity parentRelationship = new RelationshipEntity("Abuela");
    parentRelationship.setId(1L);
    return parentRelationship;
  }

  public static RelationshipDto getParentRelationshipDto() {
    return RelationshipDto.builder().id(1L).name("Padre").build();
  }

  public static RelationshipDto getGrandMotherRelationship() {
    return RelationshipDto.builder().id(10L).name("Abuela").build();
  }

  public static RelationshipDto getGrandParentRelationship() {
    return RelationshipDto.builder().id(10L).name("Abuelo").build();
  }
}
