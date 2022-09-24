package disenodesistemas.backendfunerariaapp.utils;


import disenodesistemas.backendfunerariaapp.entities.RoleEntity;
import disenodesistemas.backendfunerariaapp.enums.Role;
import disenodesistemas.backendfunerariaapp.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//*Comentar o borrar clase despues del primer run de la aplicación*
/*@Component
public class CreateRoles implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public CreateRoles(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(final String... args) throws Exception {
        final RoleEntity rolAdmin = new RoleEntity(Role.ROLE_ADMIN);
        final RoleEntity rolUser = new RoleEntity(Role.ROLE_USER);
        roleRepository.save(rolAdmin);
        roleRepository.save(rolUser);
    }
}*/
