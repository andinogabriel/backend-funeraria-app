package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.entities.RoleEntity;
import disenodesistemas.backendfunerariaapp.enums.RoleName;
import disenodesistemas.backendfunerariaapp.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/*
//*Comentar o borrar clase despues del primer run de la aplicación*
@Component
public class CreateRoles implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        RoleEntity rolAdmin = new RoleEntity(RoleName.ROLE_ADMIN);
        RoleEntity rolUser = new RoleEntity(RoleName.ROLE_USER);
        roleRepository.save(rolAdmin);
        roleRepository.save(rolUser);
    }
}
*/