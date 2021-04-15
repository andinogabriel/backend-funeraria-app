package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.AffiliateDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.models.requests.UserDetailsRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.AffiliateRest;
import disenodesistemas.backendfunerariaapp.models.responses.UserRest;
import disenodesistemas.backendfunerariaapp.service.UserServiceInterface;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    UserServiceInterface userService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public UserRest getUser() {
        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //Del metodo obtenemos el subject
        String email = authentication.getPrincipal().toString();

        UserDto userDto = userService.getUser(email);

        //Copia los argumentos de un bean a otro
        UserRest userToReturn = mapper.map(userDto, UserRest.class);

        return userToReturn;
    }

    @PostMapping
    public UserRest createUser(@RequestBody @Valid UserDetailsRequestModel userDetails) {

        UserDto userDto = mapper.map(userDetails, UserDto.class); //OBjeto que sirve para enviar a la logica de nuestra app

        //Se encarga de crear el usuario en la DB
        UserDto createdUser = userService.createUser(userDto);

        //BeanUtils.copyProperties(createdUser, userToReturn);
        UserRest userToReturn = mapper.map(createdUser, UserRest.class);

        return userToReturn;

    }

    @GetMapping(path = "/affiliates")
    public List<AffiliateRest> getAffiliates() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getPrincipal().toString();

        List<AffiliateDto> affiliatesDto = userService.getUserAffiliates(email);

        List<AffiliateRest> affiliatesRest = new ArrayList<>();

        for (AffiliateDto affiliate : affiliatesDto) {
            AffiliateRest affiliateRest = mapper.map(affiliate, AffiliateRest.class);
            affiliatesRest.add(affiliateRest);

        }
        return affiliatesRest;
    }

}
