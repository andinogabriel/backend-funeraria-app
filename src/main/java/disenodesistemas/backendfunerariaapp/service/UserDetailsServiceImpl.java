package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.entities.UserMain;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

//Clase que convierte la clase usuario en un UsuarioMain. UserDetailsService es propia de Spring Security
@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email);
        if(user == null) throw new UsernameNotFoundException("Email y/o contraseña incorrecto.");
        return UserMain.build(user);
    }

}
