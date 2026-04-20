package disenodesistemas.backendfunerariaapp.application.port.out;

public interface ReceiptNumberGeneratorPort {
  Long nextSerialNumber();

  Long nextReceiptNumber();
}
