package disenodesistemas.backendfunerariaapp.persistence.repository;

public interface EmailSender {
    void send(String to, String email);
}
