package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.service.impl.ReceiptTypeServiceImpl;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/receiptTypes")
public class ReceiptTypeController {

  private final ReceiptTypeServiceImpl receiptTypeServiceImpl;

  public ReceiptTypeController(final ReceiptTypeServiceImpl receiptTypeServiceImpl) {
    this.receiptTypeServiceImpl = receiptTypeServiceImpl;
  }

  @GetMapping
  public ResponseEntity<List<ReceiptTypeResponseDto>> findAll() {
    return ResponseEntity.ok(receiptTypeServiceImpl.getAllReceiptTypes());
  }
}
