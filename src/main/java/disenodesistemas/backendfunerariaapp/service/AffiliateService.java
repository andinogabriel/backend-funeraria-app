package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.AffiliateCreationDto;
import disenodesistemas.backendfunerariaapp.dto.AffiliateDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.repository.GenderRepository;
import disenodesistemas.backendfunerariaapp.repository.RelationshipRepository;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Service
public class AffiliateService implements AffiliateServiceInterface {

    @Autowired
    AffiliateRepository affiliateRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GenderRepository genderRepository;

    @Autowired
    RelationshipRepository relationshipRepository;

    @Autowired
    ModelMapper mapper;

    @Override
    public AffiliateDto createAffiliate(AffiliateCreationDto affiliate) {

        UserEntity userEntity = userRepository.findByEmail(affiliate.getUserEmail());
        GenderEntity genderEntity = genderRepository.findById(affiliate.getAffiliateGender());
        RelationshipEntity relationshipEntity = relationshipRepository.findById(affiliate.getAffiliateRelationship());

        AffiliateEntity affiliateEntity = new AffiliateEntity();
        affiliateEntity.setUser(userEntity);
        affiliateEntity.setAffiliateGender(genderEntity);
        affiliateEntity.setAffiliateRelationship(relationshipEntity);
        affiliateEntity.setDni(affiliate.getDni());
        affiliateEntity.setFirstName(affiliate.getFirstName());
        affiliateEntity.setLastName(affiliate.getLastName());
        affiliateEntity.setBirthDate(affiliate.getBirthDate());
        affiliateEntity.setAffiliateId(UUID.randomUUID().toString());

        AffiliateEntity createdAffiliate = affiliateRepository.save(affiliateEntity);

        AffiliateDto affiliateToReturn = mapper.map(createdAffiliate, AffiliateDto.class);

        return affiliateToReturn;
    }
}
