package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.UserModel;
import disenodesistemas.backendfunerariaapp.dto.UserModelAssembler;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.service.EmailService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final ProjectionFactory projectionFactory;
    private final UserModelAssembler userModelAssembler;
    private final PagedResourcesAssembler<UserEntity> pagedResourcesAssembler;



    @GetMapping(path = "/me")
    public UserResponseDto getUser() {
        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Del metodo obtenemos el subject name que seria nuestro email
        final String email = authentication.getName();
        //Copia los argumentos de un bean a otro
        return projectionFactory.createProjection(UserResponseDto.class, userService.getUserByEmail(email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/paginated")
    public PagedModel<UserModel> getAllUsersPaginated(@RequestParam(value = "page", defaultValue = "0") final int page,
                                             @RequestParam(value="size", defaultValue = "1") final int size,
                                             @RequestParam(value = "sortBy", defaultValue = "startDate") final String sortBy,
                                             @RequestParam(value = "sortDir", defaultValue = "desc") final String sortDir) {
        final Page<UserEntity> userPage =  userService.getAllUsers(page, size, sortBy, sortDir);
        return pagedResourcesAssembler.toModel(userPage, userModelAssembler);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid final UserRegisterDto userRegisterDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRegisterDto));
    }

    @PostMapping(path = "/login")
    public ResponseEntity<JwtDto> login(@Valid @RequestBody final UserLoginDto userLoginDto) {
        return ResponseEntity.ok(userService.login(userLoginDto));
    }

    @GetMapping(path = "/activation")
    public String confirmation(@RequestParam("token") final String token) {
        return userService.confirmationUser(token);
    }

    @PostMapping(path = "/forgot-password")
    public String forgotUserPassword(@RequestParam(value = "email") final String email) {
        return emailService.sendForgotPassword(email);
    }

    @PostMapping(path = "/reset-password")
    public String resetUserPassword(@Valid @RequestBody final PasswordResetDto passwordResetDto, @RequestParam("token") final String token) {
        return userService.resetUserPassword(passwordResetDto, token);
    }

    /*
    @GetMapping(path = "/affiliates")
    public List<AffiliateRest> getAffiliates() {

        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //Del metodo obtenemos el subject name que seria nuestro email
        String email = authentication.getName();

        List<AffiliateDto> affiliatesDto = userServiceImpl.getUserAffiliates(email);

        List<AffiliateRest> affiliatesRest = new ArrayList<>();

        for (AffiliateDto affiliate : affiliatesDto) {
            AffiliateRest affiliateRest = mapper.map(affiliate, AffiliateRest.class);
            affiliatesRest.add(affiliateRest);

        }
        return affiliatesRest;
    }*/

}
