package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.AffiliateDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.EmailExistsException;
import disenodesistemas.backendfunerariaapp.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.entities.ConfirmationToken;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserServiceInterface{

    @Autowired
    UserRepository userRepository;

    @Autowired
    AffiliateRepository affiliateRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    ConfirmationTokenService confirmationTokenService;

    @Autowired
    ModelMapper mapper;

    @Override
    public String createUser(UserEntity user) {
        if (userRepository.findByEmail(user.getEmail()) != null)
            throw new EmailExistsException("El email ya se encuentra registrado.");

        UserEntity userEntity = mapper.map(user, UserEntity.class);

        // Encriptamos la password.
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        UUID userId = UUID.randomUUID();
        userEntity.setUserId(userId.toString());

        UserEntity storedUserDetails = userRepository.save(userEntity);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(60), storedUserDetails);
        confirmationTokenService.saveConfirmationToken(confirmationToken);


        return token;

    }

    public void enableAppUser(String email) {
        userRepository.enableAppUser(email);
    }


    //Iniciar Sesion
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }
        // El tercer parametro es una coleccion donde se especifica una serie de
        // autoridades
        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
    }

    @Override
    public UserDto getUser(String email) {

        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }

        UserDto userToReturn = new UserDto();

        BeanUtils.copyProperties(userEntity, userToReturn);

        return userToReturn;
    }

    @Override
    public List<AffiliateDto> getUserAffiliates(String email) {

        UserEntity userEntity = userRepository.findByEmail(email);

        List<AffiliateEntity> affiliates = affiliateRepository.getByUserIdOrderByStartDateDesc(userEntity.getId());

        List<AffiliateDto> affiliatesDto = new ArrayList<>();

        for (AffiliateEntity affiliate : affiliates) {
            AffiliateDto affiliateDto = mapper.map(affiliate, AffiliateDto.class);
            affiliatesDto.add(affiliateDto);
        }

        return affiliatesDto;
    }


}
