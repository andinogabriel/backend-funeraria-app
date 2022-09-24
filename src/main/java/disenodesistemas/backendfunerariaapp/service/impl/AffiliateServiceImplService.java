package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.AffiliateService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AffiliateServiceImplService implements AffiliateService {

    private final AffiliateRepository affiliateRepository;
    private final ModelMapper mapper;
    private final ProjectionFactory projectionFactory;

    @Override
    public AffiliateResponseDto createAffiliate(final AffiliateRequestDto affiliate) {

        //UserEntity userEntity = userService.getUserByEmail(affiliate.getUserEmail());
        //GenderEntity genderEntity = genderService.getGenderById(affiliate.getAffiliateGender());
        //RelationshipEntity relationshipEntity = relationshipService.getRelationshipById(affiliate.getAffiliateRelationship());

        final AffiliateEntity affiliateEntity = AffiliateEntity.builder()
                .dni(affiliate.getDni())
                .gender(mapper.map(affiliate.getGender(), GenderEntity.class))
                .relationship(mapper.map(affiliate.getRelationship(), RelationshipEntity.class))
                .lastName(affiliate.getLastName())
                .firstName(affiliate.getFirstName())
                .birthDate(affiliate.getBirthDate())
                .user(mapper.map(affiliate.getUser(), UserEntity.class))
                .build();

        return projectionFactory.createProjection(AffiliateResponseDto.class, affiliateRepository.save(affiliateEntity));
    }
}
