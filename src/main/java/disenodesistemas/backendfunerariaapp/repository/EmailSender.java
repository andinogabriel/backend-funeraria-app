package disenodesistemas.backendfunerariaapp.repository;

public interface EmailSender {
    void send(String to, String email);
}
