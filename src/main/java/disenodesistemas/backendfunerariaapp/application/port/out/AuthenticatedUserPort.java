package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;

public interface AuthenticatedUserPort {

  String getAuthenticatedEmail();

  UserEntity getAuthenticatedUser();
}
