package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.AffiliateDto;
import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.models.requests.PasswordResetRequest;
import disenodesistemas.backendfunerariaapp.models.requests.UserDetailsRequestModel;
import disenodesistemas.backendfunerariaapp.models.requests.UserLoginRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.AffiliateRest;
import disenodesistemas.backendfunerariaapp.models.responses.UserRest;
import disenodesistemas.backendfunerariaapp.service.EmailService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public UserRest getUser() {
        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //Del metodo obtenemos el subject name que seria nuestro email
        String email = authentication.getName();
        UserDto userDto = userService.getUser(email);

        //Copia los argumentos de un bean a otro
        return mapper.map(userDto, UserRest.class);
    }

    @PostMapping
    public UserRest createUser(@RequestBody @Valid UserDetailsRequestModel userDetails) {
        UserDto userDto = userService.createUser(mapper.map(userDetails, UserDto.class));

        return mapper.map(userDto, UserRest.class);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<JwtDto> login(@Valid @RequestBody UserLoginRequestModel userLoginRequestModel) {
        return ResponseEntity.ok(userService.login(userLoginRequestModel));
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
    public String resetUserPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest, @RequestParam("token")String token) {
        return userService.resetUserPassword(passwordResetRequest, token);
    }

    @GetMapping(path = "/affiliates")
    public List<AffiliateRest> getAffiliates() {

        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //Del metodo obtenemos el subject name que seria nuestro email
        String email = authentication.getName();

        List<AffiliateDto> affiliatesDto = userService.getUserAffiliates(email);

        List<AffiliateRest> affiliatesRest = new ArrayList<>();

        for (AffiliateDto affiliate : affiliatesDto) {
            AffiliateRest affiliateRest = mapper.map(affiliate, AffiliateRest.class);
            affiliatesRest.add(affiliateRest);

        }
        return affiliatesRest;
    }

}
