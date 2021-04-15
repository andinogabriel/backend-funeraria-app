package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.repository.RelationshipRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RelationshipService implements  RelationshipServiceInterface{

    @Autowired
    RelationshipRepository relationshipRepository;

    @Autowired
    ModelMapper mapper;

    @Override
    public List<RelationshipDto> getRelationships() {

        List<RelationshipEntity> relationshipEntities = relationshipRepository.findAll();

        List<RelationshipDto> relationshipsDto = new ArrayList<>();

        for (RelationshipEntity relationship : relationshipEntities) {
            RelationshipDto relationshipDto = mapper.map(relationship, RelationshipDto.class);
            relationshipsDto.add(relationshipDto);
        }

        return relationshipsDto;
    }
}
