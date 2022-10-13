package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.RelationshipRepository;
import disenodesistemas.backendfunerariaapp.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelationshipServiceImplService implements RelationshipService {

    private final RelationshipRepository relationshipRepository;

    @Override
    public List<RelationshipResponseDto> getRelationships() {
        return relationshipRepository.findAllByOrderByName();
    }

    @Override
    public RelationshipEntity getRelationshipById(final Long id) {
        return relationshipRepository.findById(id).orElseThrow(() -> new AppException("relationship.error.not.found", HttpStatus.NOT_FOUND));
    }
}
