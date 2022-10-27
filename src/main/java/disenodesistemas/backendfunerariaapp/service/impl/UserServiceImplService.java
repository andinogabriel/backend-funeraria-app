package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.entities.RoleEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.entities.UserMain;
import disenodesistemas.backendfunerariaapp.enums.Role;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.EmailExistsException;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.service.ConfirmationTokenService;
import disenodesistemas.backendfunerariaapp.service.EmailService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImplService implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final ProjectionFactory projectionFactory;
    private static final String ASC = "asc";


    @Override
    @Transactional
    public UserEntity save(final UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(final UserRegisterDto user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            throw new EmailExistsException("user.error.email.already.registered");

        val userEntity = new UserEntity(user.getEmail(), user.getFirstName(),
                user.getLastName(), bCryptPasswordEncoder.encode(user.getPassword()));

        final Set<RoleEntity> roles = new HashSet<>();
        roleRepository.findByName(Role.ROLE_USER).ifPresent(roles::add);
        userEntity.setRoles(roles);

        final Optional<UserEntity> userSaved = Optional.of(save(userEntity));
        Optional.of(save(userEntity)).ifPresent(
                u -> {
                    confirmationTokenService.save(u, UUID.randomUUID().toString());
                    emailService.sendHtmlMail(u);
                }
        );
        return projectionFactory.createProjection(UserResponseDto.class, userSaved);
    }


    @Override
    @Transactional(readOnly = true)
    public JwtDto login(final UserLoginDto loginUser) {
        final UserEntity user = userRepository.findByEmail(loginUser.getEmail())
                .orElseThrow(() -> new AppException("user.error.email.not.registered", HttpStatus.UNAUTHORIZED));

        if(!bCryptPasswordEncoder.matches(loginUser.getPassword(), user.getEncryptedPassword()))
            throw new AppException("password.error.wrong", HttpStatus.UNAUTHORIZED);

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUser.getEmail(),
                        loginUser.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String jwt = jwtProvider.generateToken(authentication);
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return JwtDto.builder()
                .authorities(userDetails.getAuthorities())
                .email(userDetails.getUsername())
                .authorization(SecurityConstants.TOKEN_PREFIX + jwt)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserById(final Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new AppException("user.error.id.not.found", HttpStatus.NOT_FOUND)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserByEmail(final String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new AppException("user.error.email.not.registered", HttpStatus.NOT_FOUND)
        );
    }

    @Override
    public String confirmationUser(final String token) {
        String message;
        val confirmationTokenEntity = confirmationTokenService.findByToken(token);
        val userEntity = confirmationTokenEntity.getUser();
        //if the user account isn't activated
        if(!userEntity.isEnabled()) {
            //check if the token is expired
            if(confirmationTokenEntity.getExpiryDate().isBefore(Instant.now())) {
                message = "confirmationToken.error.expired";
            } else {
                //token is valid so activate the user account
                userEntity.setEnabled(true);
                userRepository.save(userEntity);
                message = "confirmationToken.successful.activation";
            }
        } else {
            //User account is already activated
            throw new AppException("confirmationToken.error.already.activated", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return message;
    }

    @Override
    public String resetUserPassword(final PasswordResetDto passwordResetDto, final String token) {
        val tokenEntity = confirmationTokenService.findByToken(token);
        if (Objects.nonNull(tokenEntity.getUser()) && tokenEntity.getExpiryDate().isAfter(Instant.now())) {
            tokenEntity.getUser().setEncryptedPassword(bCryptPasswordEncoder.encode(passwordResetDto.getPassword()));
            userRepository.save(tokenEntity.getUser());
            //Password successfully reset. You can now log in with the new credentials.
            return  "confirmationToken.successful.reset.password";
        } else {
            throw new AppException("confirmationToken.error.invalid.link",HttpStatus.GONE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        val user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("user.error.load.user.by.username")
        );
        return UserMain.build(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserEntity> getAllUsers(int page,
                                             final int limit,
                                             final String sortBy,
                                             final String sortDir) {
        page = page > 0 ? page - 1 : page;
        final Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase(ASC) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );
        return userRepository.findAll(pageable);
    }

    @Override
    public List<UserResponseDto> findAll() {
        return userRepository.findAllByOrderByStartDateDesc();
    }

}
