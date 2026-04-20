package disenodesistemas.backendfunerariaapp.application.port.out;

public interface MessageResolverPort {

  String getMessage(String code, Object... args);
}
