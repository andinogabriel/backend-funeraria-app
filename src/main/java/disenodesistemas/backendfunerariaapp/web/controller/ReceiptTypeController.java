package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.receipttype.ReceiptTypeQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.ReceiptTypeResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/receiptTypes")
public class ReceiptTypeController {

  private final ReceiptTypeQueryUseCase receiptTypeQueryUseCase;

  @GetMapping
  public ResponseEntity<List<ReceiptTypeResponseDto>> findAll() {
    return ResponseEntity.ok(receiptTypeQueryUseCase.getAllReceiptTypes());
  }
}
