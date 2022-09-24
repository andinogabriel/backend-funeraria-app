package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.service.impl.RelationshipServiceImplService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/relationships")
public class RelationshipController {

    private final RelationshipServiceImplService relationshipService;

    public RelationshipController(final RelationshipServiceImplService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @GetMapping
    public List<RelationshipResponseDto> getRelationships() {
        return relationshipService.getRelationships();
    }

}
