package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IConfirmationToken;
import disenodesistemas.backendfunerariaapp.service.Interface.IEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;
import java.util.Locale;
import java.util.UUID;

@Service
public class EmailServiceImpl implements IEmail {

    private final IConfirmationToken confirmationTokenService;
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Autowired
    public EmailServiceImpl(IConfirmationToken confirmationTokenService, TemplateEngine templateEngine, JavaMailSender javaMailSender, UserRepository userRepository, MessageSource messageSource) {
        this.confirmationTokenService = confirmationTokenService;
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }


    @Override
    public void sendHtmlMail(UserEntity userEntity) throws MessagingException {
        //Check if the user has a token
        ConfirmationTokenEntity confirmationTokenEntity = confirmationTokenService.findByUser(userEntity);

        String token = confirmationTokenEntity.getToken();
        Context context = new Context();
        context.setVariable("title", "Funeraria Nuñez y Hnos.- Confirmar tu email");
        context.setVariable("link", "http://localhost:8081/api/v1/users/activation?token=" + token);
        //html template
        String body = templateEngine.process("confirmation", context); //html template name

        //Send the confirmation email
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(userEntity.getEmail());
        helper.setSubject("email address confirmation");
        helper.setText(body, true);
        javaMailSender.send(message);
    }

    @Override
    public String sendForgotPassword(String email) {
        UserEntity user  = getUserByEmail(email);
        String message = "";

        // create token
        ConfirmationTokenEntity confirmationToken = new ConfirmationTokenEntity(user, UUID.randomUUID().toString());
        // save it
        confirmationTokenService.save(user, confirmationToken.getToken());

        // create the email
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Reestablecer contraseña");
        mailMessage.setFrom("javaalkemychallenge@gmail.com");
        mailMessage.setText("Para completar el proceso de reestablecimiento de contraseña, por favor haga click aquí: "
                +"http://localhost:8081/api/v1/users/reset-password?token="+confirmationToken.getToken());

        javaMailSender.send(mailMessage);

        message = messageSource.getMessage("resetPassword.successful.message", null, Locale.getDefault());
        return message;
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("user.error.email.not.registered", null, Locale.getDefault())
                )
        );
    }

}
