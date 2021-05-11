package disenodesistemas.backendfunerariaapp.controllers;


import disenodesistemas.backendfunerariaapp.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.models.responses.RelationshipRest;
import disenodesistemas.backendfunerariaapp.service.RelationshipService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/relationships")
public class RelationshipController {

    @Autowired
    RelationshipService relationshipService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public List<RelationshipRest> getRelationships() {
        List<RelationshipDto> relationshipsDto = relationshipService.getRelationships();
        List<RelationshipRest> relationshipsRest = new ArrayList<>();

        for (RelationshipDto relationship : relationshipsDto) {
            RelationshipRest relationshipRest = mapper.map(relationship, RelationshipRest.class);
            relationshipsRest.add(relationshipRest);
        }
        return relationshipsRest;
    }

}
