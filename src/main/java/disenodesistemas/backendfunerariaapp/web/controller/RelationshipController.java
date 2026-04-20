package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.application.service.RelationshipService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/relationships")
public class RelationshipController {

  private final RelationshipService relationshipService;

  public RelationshipController(final RelationshipService relationshipService) {
    this.relationshipService = relationshipService;
  }

  @GetMapping
  public ResponseEntity<List<RelationshipResponseDto>> findAll() {
    return ResponseEntity.ok(relationshipService.getRelationships());
  }
}
