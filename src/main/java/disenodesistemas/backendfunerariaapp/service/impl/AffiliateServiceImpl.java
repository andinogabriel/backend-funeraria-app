package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IAffiliate;
import disenodesistemas.backendfunerariaapp.service.Interface.IGender;
import disenodesistemas.backendfunerariaapp.service.Interface.IRelationship;
import disenodesistemas.backendfunerariaapp.service.Interface.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

@Service
public class AffiliateServiceImpl implements IAffiliate {

    private final AffiliateRepository affiliateRepository;
    private final IUser userService;
    private final IGender genderService;
    private final IRelationship relationshipService;
    private final MessageSource messageSource;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public AffiliateServiceImpl(AffiliateRepository affiliateRepository, IUser userService, IGender genderService, IRelationship relationshipService, MessageSource messageSource, ProjectionFactory projectionFactory) {
        this.affiliateRepository = affiliateRepository;
        this.userService = userService;
        this.genderService = genderService;
        this.relationshipService = relationshipService;
        this.messageSource = messageSource;
        this.projectionFactory = projectionFactory;
    }


    @Override
    public AffiliateResponseDto createAffiliate(AffiliateCreationDto affiliate) {

        UserEntity userEntity = userService.getUserByEmail(affiliate.getUserEmail());
        GenderEntity genderEntity = genderService.getGenderById(affiliate.getAffiliateGender());
        RelationshipEntity relationshipEntity = relationshipService.getRelationshipById(affiliate.getAffiliateRelationship());

        AffiliateEntity affiliateEntity = AffiliateEntity.builder()
                .dni(affiliate.getDni())
                .affiliateGender(genderEntity)
                .affiliateRelationship(relationshipEntity)
                .lastName(affiliate.getLastName())
                .firstName(affiliate.getFirstName())
                .birthDate(affiliate.getBirthDate())
                .user(userEntity)
                .build();

        AffiliateEntity createdAffiliate = affiliateRepository.save(affiliateEntity);
        return projectionFactory.createProjection(AffiliateResponseDto.class, createdAffiliate);
    }
}
