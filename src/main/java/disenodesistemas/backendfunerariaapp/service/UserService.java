package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    UserEntity save(UserEntity user);

    UserResponseDto createUser(UserRegisterDto user);

    JwtDto login(UserLoginDto loginUser);

    UserEntity getUserById(Long id);

    UserEntity getUserByEmail(String email);

    String confirmationUser(String token);

    String resetUserPassword(PasswordResetDto passwordResetDto, String token);

    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    Page<UserResponseDto> getAllUsers(int page, int limit, String sortBy, String sortDir);

}
