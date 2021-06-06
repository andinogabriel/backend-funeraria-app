package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByEmail(String email);

    UserEntity findById(long id);

}

