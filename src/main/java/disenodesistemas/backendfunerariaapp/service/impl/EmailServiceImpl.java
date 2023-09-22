package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.service.ConfirmationTokenService;
import disenodesistemas.backendfunerariaapp.service.EmailService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import lombok.val;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {

    private final ConfirmationTokenService confirmationTokenService;
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final UserService userService;

    public EmailServiceImpl(final ConfirmationTokenService confirmationTokenService,
                            final TemplateEngine templateEngine,
                            final JavaMailSender javaMailSender,
                            @Lazy final UserService userService) {
        this.confirmationTokenService = confirmationTokenService;
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
        this.userService = userService;
    }


    @Override
    public void sendHtmlMail(final UserEntity userEntity)  {
        //Check if the user has a token
        val confirmationTokenEntity = confirmationTokenService.findByUser(userEntity);

        final String token = confirmationTokenEntity.getToken();
        val context = new Context();
        context.setVariable("title", "Funeraria Nuñez y Hnos.- Confirmar tu email");
        context.setVariable("link", "http://localhost:8081/api/v1/users/activation?token=" + token);
        //html template
        final String body = templateEngine.process("confirmation", context); //html template name

        try {
            final MimeMessage message = javaMailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(userEntity.getEmail());
            helper.setSubject("email address confirmation");
            helper.setText(body, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new AppException("No se pudo enviar el email", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public String sendForgotPassword(final String email) {
        val user  = userService.getUserByEmail(email);
        String message;

        // create token
        val confirmationToken = new ConfirmationTokenEntity(user, UUID.randomUUID().toString());
        // save it
        confirmationTokenService.save(user, confirmationToken.getToken());

        // create the email
        val mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Reestablecer contraseña");
        mailMessage.setFrom("javaalkemychallenge@gmail.com");
        mailMessage.setText("Para completar el proceso de reestablecimiento de contraseña, por favor haga click aquí: "
                +"http://localhost:8081/api/v1/users/reset-password?token="+confirmationToken.getToken());

        javaMailSender.send(mailMessage);

        message = "resetPassword.successful.message";
        return message;
    }

}
