package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.entities.UserEntity;

import javax.mail.MessagingException;

public interface IEmail {

    void sendHtmlMail(UserEntity userEntity) throws MessagingException;

    String sendForgotPassword(String email);

    UserEntity getUserByEmail(String email);

}
