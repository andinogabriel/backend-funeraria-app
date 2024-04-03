package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RelationshipEntityMother {

  public static RelationshipEntity getParentRelationship() {
    final RelationshipEntity parentRelationship = new RelationshipEntity("Padre");
    parentRelationship.setId(1L);
    return parentRelationship;
  }
}
