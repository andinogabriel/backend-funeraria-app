package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.service.Interface.IEmail;
import disenodesistemas.backendfunerariaapp.service.Interface.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final IUser userService;
    private final IEmail emailService;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public UserController(IUser userService, IEmail emailService, ProjectionFactory projectionFactory) {
        this.userService = userService;
        this.emailService = emailService;
        this.projectionFactory = projectionFactory;
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(path = "/me")
    public UserResponseDto getUser() {
        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Del metodo obtenemos el subject name que seria nuestro email
        String email = authentication.getName();
        //Copia los argumentos de un bean a otro
        return projectionFactory.createProjection(UserResponseDto.class, userService.getUserByEmail(email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<UserResponseDto> getAllUsers(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "5") int limit, @RequestParam(value = "sortBy", defaultValue = "startDate") String sortBy, @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        return userService.getAllUsers(page, limit, sortBy, sortDir);
    }


    @PostMapping
    public UserResponseDto createUser(@RequestBody @Valid UserRegisterDto userRegisterDto) {
        return userService.createUser(userRegisterDto);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<JwtDto> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        return ResponseEntity.ok(userService.login(userLoginDto));
    }

    @GetMapping(path = "/activation")
    public String confirmation(@RequestParam("token") String token) {
        return userService.confirmationUser(token);
    }

    @PostMapping(path = "/forgot-password")
    public String forgotUserPassword(@RequestParam(value = "email") String email) {
        return emailService.sendForgotPassword(email);
    }

    @PostMapping(path = "/reset-password")
    public String resetUserPassword(@Valid @RequestBody PasswordResetDto passwordResetDto, @RequestParam("token") String token) {
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
