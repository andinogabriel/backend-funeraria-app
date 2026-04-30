package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.relationship.RelationshipQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/relationships")
public class RelationshipController {

  private final RelationshipQueryUseCase relationshipQueryUseCase;

  @GetMapping
  public ResponseEntity<List<RelationshipResponseDto>> findAll() {
    return ResponseEntity.ok(relationshipQueryUseCase.getRelationships());
  }
}
