package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.service.impl.RelationshipServiceImpl;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/relationships")
public class RelationshipController {

  private final RelationshipServiceImpl relationshipService;

  public RelationshipController(final RelationshipServiceImpl relationshipService) {
    this.relationshipService = relationshipService;
  }

  @GetMapping
  public ResponseEntity<List<RelationshipResponseDto>> findAll() {
    return ResponseEntity.ok(relationshipService.getRelationships());
  }
}
