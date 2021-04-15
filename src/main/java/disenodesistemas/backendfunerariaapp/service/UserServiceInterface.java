package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.AffiliateDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserServiceInterface extends UserDetailsService {

    public UserDto createUser(UserDto user);

    public UserDto getUser(String email);

    public List<AffiliateDto> getUserAffiliates(String email);

}
