package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.RelationshipRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class RelationshipServiceImpl implements IRelationship {

    private final RelationshipRepository relationshipRepository;
    private final MessageSource messageSource;

    @Autowired
    public RelationshipServiceImpl(RelationshipRepository relationshipRepository, MessageSource messageSource) {
        this.relationshipRepository = relationshipRepository;
        this.messageSource = messageSource;
    }

    @Override
    public List<RelationshipResponseDto> getRelationships() {
        return relationshipRepository.findAllByOrderByName();
    }

    @Override
    public RelationshipEntity getRelationshipById(Long id) {
        return relationshipRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("relationship.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }
}
