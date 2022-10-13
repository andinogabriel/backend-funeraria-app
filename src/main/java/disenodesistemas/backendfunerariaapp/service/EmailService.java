package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.entities.UserEntity;

public interface EmailService {
    void sendHtmlMail(UserEntity userEntity);
    String sendForgotPassword(String email);
}
