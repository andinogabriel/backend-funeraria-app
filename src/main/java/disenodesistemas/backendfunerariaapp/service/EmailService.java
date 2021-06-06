package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.ConfirmationTokenRepository;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.UUID;

@Service
public class EmailService {

    private final ConfirmationTokenService confirmationTokenService;
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;

    @Autowired
    ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    UserRepository userRepository;


    @Autowired
    public EmailService(ConfirmationTokenService confirmationTokenService, TemplateEngine templateEngine, JavaMailSender javaMailSender) {
        this.confirmationTokenService = confirmationTokenService;
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
    }

    public void sendHtmlMail(UserEntity userEntity) throws MessagingException {
        ConfirmationTokenEntity confirmationTokenEntity = confirmationTokenService.findByUser(userEntity);

        //Check if the user has a token
        if(confirmationTokenEntity != null) {
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
    }

    public String sendForgotPassword(String email) {
        UserEntity existingUser  = userRepository.findByEmail(email);
        String message = "";
        if(existingUser != null) {
            // create token
            ConfirmationTokenEntity confirmationToken = new ConfirmationTokenEntity(existingUser, UUID.randomUUID().toString());
            confirmationToken.setExpiryDate(confirmationTokenService.calculateExpiryDate(24*60));
            // save it
            confirmationTokenRepository.save(confirmationToken);

            // create the email
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(existingUser.getEmail());
            mailMessage.setSubject("Reestablecer contraseña");
            mailMessage.setFrom("javaalkemychallenge@gmail.com");
            mailMessage.setText("Para completar el proceso de reestablecimiento de contraseña, por favor haga click aquí: "
                    +"http://localhost:8081/api/v1/users/reset-password?token="+confirmationToken.getToken());

            javaMailSender.send(mailMessage);

            message = "Solicitud de reestablecimiento de contraseña. Revise su bandeja de entrada de correo electrónico para reestablecer su contraseña.";
        } else {
            throw new UsernameNotFoundException("No existe cuenta asociada con: " + email);
        }
        return message;
    }



}
