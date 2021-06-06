package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.AffiliateDto;
import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.entities.*;
import disenodesistemas.backendfunerariaapp.enums.RoleName;
import disenodesistemas.backendfunerariaapp.exceptions.EmailExistsException;
import disenodesistemas.backendfunerariaapp.models.requests.PasswordResetRequest;
import disenodesistemas.backendfunerariaapp.models.requests.UserLoginRequestModel;
import disenodesistemas.backendfunerariaapp.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.repository.ConfirmationTokenRepository;
import disenodesistemas.backendfunerariaapp.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
public class UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    AffiliateRepository affiliateRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    ConfirmationTokenService confirmationTokenService;

    @Autowired
    ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ModelMapper mapper;

    @Transactional
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }


    public UserDto createUser(UserDto user) {
        if (userRepository.findByEmail(user.getEmail()) != null)
            throw new EmailExistsException("El email ya se encuentra registrado.");

        // Encriptamos la password.
        UserEntity userEntity = new UserEntity(user.getEmail(), user.getFirstName(), user.getLastName(), bCryptPasswordEncoder.encode(user.getPassword()));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleName.ROLE_USER).get());
        if(user.getRoles().contains("admin")) roles.add(roleRepository.findByName(RoleName.ROLE_ADMIN).get());
        userEntity.setRoles(roles);

        Optional<UserEntity> userSaved = Optional.of(save(userEntity));

        userSaved.ifPresent(u -> {
            try {
                String token = UUID.randomUUID().toString();
                confirmationTokenService.save(userSaved.get(), token);
                //send confirmation email
                emailService.sendHtmlMail(u);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return mapper.map(userSaved.get(), UserDto.class);
    }

    //Iniciar Sesion
    public JwtDto login(UserLoginRequestModel loginUser) {

        if(userRepository.findByEmail(loginUser.getEmail()) == null) throw new UsernameNotFoundException("No existe usuario asociado a ese email.");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUser.getEmail(),
                        loginUser.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        JwtDto jwtDto = new JwtDto(jwt, userDetails.getUsername(), userDetails.getAuthorities());
        return jwtDto;
    }



    public UserDto getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException("El email " + email + " no se encuentra registrado.");
        }

        return mapper.map(userEntity, UserDto.class);
    }


    public String confirmationUser(String token) {
        String message = "";
        ConfirmationTokenEntity confirmationTokenEntity = confirmationTokenService.findByToken(token);
        if(confirmationTokenEntity == null) {
            throw new RuntimeException("Tu token de confirmación es inválido.");
        } else {
            UserEntity userEntity = confirmationTokenEntity.getUser();
            //if the user account isn't activated
            if(!userEntity.isEnabled()) {
                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                //check if the token is expired
                if(confirmationTokenEntity.getExpiryDate().before(currentTimestamp)) {
                    message = "Tu token de confirmación ha expirado.";
                } else {
                    //token is valid so activate the user account
                    userEntity.setEnabled(true);
                    userRepository.save(userEntity);
                    message = "Tu cuenta ha sido satisfactoriamente activada.";
                }
            } else {
                //User account is already activated
                throw new RuntimeException("Su cuenta ya se encuentra activada.");
            }
        }
        return message;
    }



    public String resetUserPassword(PasswordResetRequest passwordResetRequest, String token) {
        ConfirmationTokenEntity tokenEntity = confirmationTokenRepository.findByToken(token);
        if(tokenEntity == null) throw new RuntimeException("El token no es valido.");
        String message = "";
        if (tokenEntity.getUser() != null && tokenEntity.getExpiryDate().after(new Timestamp(System.currentTimeMillis()))) {
            tokenEntity.getUser().setEncryptedPassword(bCryptPasswordEncoder.encode(passwordResetRequest.getPassword()));
            userRepository.save(tokenEntity.getUser());
            //Password successfully reset. You can now log in with the new credentials.
            message = "Contraseña actualizada satisfactoriamente.";
        } else {
            throw new RuntimeException("El link es inválido.");
        }
        return message;
    }



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
