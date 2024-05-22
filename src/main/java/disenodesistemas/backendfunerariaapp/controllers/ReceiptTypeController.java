package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.service.ReceiptTypeService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/receiptTypes")
public class ReceiptTypeController {

  private final ReceiptTypeService receiptTypeService;

  public ReceiptTypeController(final ReceiptTypeService receiptTypeService) {
    this.receiptTypeService = receiptTypeService;
  }

  @GetMapping
  public ResponseEntity<List<ReceiptTypeResponseDto>> findAll() {
    return ResponseEntity.ok(receiptTypeService.getAllReceiptTypes());
  }
}
