package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.entities.*;
import disenodesistemas.backendfunerariaapp.enums.RoleName;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.EmailExistsException;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.service.Interface.IConfirmationToken;
import disenodesistemas.backendfunerariaapp.service.Interface.IEmail;
import disenodesistemas.backendfunerariaapp.service.Interface.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
public class UserServiceImpl implements IUser {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final IConfirmationToken confirmationTokenService;
    private final IEmail emailService;
    private final RoleRepository roleRepository;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, IConfirmationToken confirmationTokenService, IEmail emailService, RoleRepository roleRepository, JwtProvider jwtProvider, AuthenticationManager authenticationManager, MessageSource messageSource, ProjectionFactory projectionFactory) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
        this.messageSource = messageSource;
        this.projectionFactory = projectionFactory;
    }


    @Override
    @Transactional
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public UserResponseDto createUser(UserRegisterDto user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            throw new EmailExistsException(
                    messageSource.getMessage("user.error.email.already.registered", null, Locale.getDefault())
            );

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

        return projectionFactory.createProjection(UserResponseDto.class, userSaved.get());
    }

    //Iniciar Sesion
    @Override
    public JwtDto login(UserLoginDto loginUser) {

        if(!userRepository.findByEmail(loginUser.getEmail()).isPresent()) throw new UsernameNotFoundException(
                messageSource.getMessage("user.error.email.not.registered", null, Locale.getDefault())
        );

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUser.getEmail(),
                        loginUser.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new JwtDto(jwt, userDetails.getUsername(), userDetails.getAuthorities());
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("user.error.id.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("user.error.email.not.registered", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

    @Override
    public String confirmationUser(String token) {
        String message = "";
        ConfirmationTokenEntity confirmationTokenEntity = confirmationTokenService.findByToken(token);
        UserEntity userEntity = confirmationTokenEntity.getUser();
        //if the user account isn't activated
        if(!userEntity.isEnabled()) {
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            //check if the token is expired
            if(confirmationTokenEntity.getExpiryDate().before(currentTimestamp)) {
                message = messageSource.getMessage("confirmationToken.error.expired", null, Locale.getDefault());
            } else {
                //token is valid so activate the user account
                userEntity.setEnabled(true);
                userRepository.save(userEntity);
                message = messageSource.getMessage("confirmationToken.successful.activation", null, Locale.getDefault());
            }
        } else {
            //User account is already activated
            throw new RuntimeException(
                    messageSource.getMessage("confirmationToken.error.already.activated", null, Locale.getDefault())
            );
        }
        return message;
    }

    @Override
    public String resetUserPassword(PasswordResetDto passwordResetDto, String token) {
        ConfirmationTokenEntity tokenEntity = confirmationTokenService.findByToken(token);
        String message = "";
        if (tokenEntity.getUser() != null && tokenEntity.getExpiryDate().after(new Timestamp(System.currentTimeMillis()))) {
            tokenEntity.getUser().setEncryptedPassword(bCryptPasswordEncoder.encode(passwordResetDto.getPassword()));
            userRepository.save(tokenEntity.getUser());
            //Password successfully reset. You can now log in with the new credentials.
            message = messageSource.getMessage("confirmationToken.successful.reset.password", null, Locale.getDefault());
        } else {
            throw new RuntimeException(
                    messageSource.getMessage("confirmationToken.error.invalid.link", null, Locale.getDefault())
            );
        }
        return message;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(
                        messageSource.getMessage("user.error.loadUserByUsername", null, Locale.getDefault())
                )
        );
        return UserMain.build(user);
    }

    @Override
    public Page<UserResponseDto> getAllUsers(int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        return userRepository.findAllProjectedBy(pageable);
    }

    /*
    public List<AffiliateDto> getUserAffiliates(String email) {
        UserEntity userEntity = getUserByEmail(email);
        List<AffiliateEntity> affiliates = affiliateRepository.getByUserIdOrderByStartDateDesc(userEntity.getId());
        List<AffiliateDto> affiliatesDto = new ArrayList<>();
        for (AffiliateEntity affiliate : affiliates) {
            AffiliateDto affiliateDto = mapper.map(affiliate, AffiliateDto.class);
            affiliatesDto.add(affiliateDto);
        }
        return affiliatesDto;
    }
    */



}
