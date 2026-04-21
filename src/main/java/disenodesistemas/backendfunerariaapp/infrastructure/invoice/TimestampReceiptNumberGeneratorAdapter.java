package disenodesistemas.backendfunerariaapp.infrastructure.invoice;

import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptNumberGeneratorPort;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class TimestampReceiptNumberGeneratorAdapter implements ReceiptNumberGeneratorPort {

  private static final String PATTERN = "yyyyMMddHHmmssSSS";
  private static final DateTimeFormatter RECEIPT_FORMATTER = DateTimeFormatter.ofPattern(PATTERN);

  private final AtomicLong serialNumber;

  public TimestampReceiptNumberGeneratorAdapter() {
    this.serialNumber = new AtomicLong(0L);
  }

  @Override
  public Long nextSerialNumber() {
    return serialNumber.incrementAndGet();
  }

  @Override
  public Long nextReceiptNumber() {
    return Long.valueOf(LocalDateTime.now().format(RECEIPT_FORMATTER));
  }
}
