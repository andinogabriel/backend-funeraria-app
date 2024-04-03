package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.service.InvoiceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InvoiceServiceImpl implements InvoiceService {

  private final AtomicLong serialNumber;
  private static final String PATTERN = "yyyyMMddHHmmssSSS";

  public InvoiceServiceImpl() {
    this.serialNumber = new AtomicLong(0L);
  }

  @Override
  public Long createSerialNumber() {
    return serialNumber.incrementAndGet();
  }

  @Override
  public Long createReceiptNumber() {
    return Long.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern(PATTERN)));
  }
}
