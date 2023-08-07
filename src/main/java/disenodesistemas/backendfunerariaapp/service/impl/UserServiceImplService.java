package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetByEmailDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.RoleEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.entities.UserMain;
import disenodesistemas.backendfunerariaapp.enums.Role;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;

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
            throw new ConflictException("user.error.email.already.registered");

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
        final ConfirmationTokenEntity tokenEntity = confirmationTokenService.findByToken(token);
        final UserEntity userEntity = tokenEntity.getUser();

        if (userEntity.isEnabled())
            throw new AppException("confirmationToken.error.already.activated", HttpStatus.INTERNAL_SERVER_ERROR);

        if (tokenEntity.getExpiryDate().isBefore(Instant.now()))
            return "confirmationToken.error.expired";

        userEntity.setEnabled(true);
        userRepository.save(userEntity);
        return "confirmationToken.successful.activation";
    }

    @Override
    public String resetUserPasswordByEmail(final PasswordResetByEmailDto passwordResetDto, final String token) {
        final ConfirmationTokenEntity tokenEntity = confirmationTokenService.findByToken(token);
        if (isNull(tokenEntity.getUser()) || tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            throw new AppException("confirmationToken.error.invalid.link", HttpStatus.GONE);
        }

        final UserEntity userEntity = tokenEntity.getUser();
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(passwordResetDto.getPassword()));
        userRepository.save(userEntity);

        return "confirmationToken.successful.reset.password";
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
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return userRepository.findAllByOrderByStartDateDesc();
    }

}
