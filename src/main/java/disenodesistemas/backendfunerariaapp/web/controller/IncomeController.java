package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.service.IncomeService;
import disenodesistemas.backendfunerariaapp.mapping.UserMapper;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

  private final IncomeService incomeService;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final UserMapper userMapper;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<IncomeResponseDto>> findAll() {
    return ResponseEntity.ok(incomeService.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/{receiptNumber}")
  public ResponseEntity<IncomeResponseDto> findById(@PathVariable final Long receiptNumber) {
    return ResponseEntity.ok(incomeService.findByReceiptNumber(receiptNumber));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/paginated")
  public Page<IncomeResponseDto> getIncomesPaginated(
      @RequestParam(value = "isDeleted", required = false, defaultValue = "false")
          final boolean isDeleted,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "5") final int limit,
      @RequestParam(value = "sortBy", defaultValue = "incomeDate") final String sortBy,
      @RequestParam(value = "sortDir", defaultValue = "desc") final String sortDir) {
    return incomeService.getIncomesPaginated(isDeleted, page, limit, sortBy, sortDir);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<IncomeResponseDto> create(
      @RequestBody @Valid final IncomeRequestDto incomeRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(incomeService.create(withAuthenticatedUser(incomeRequest)));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{receiptNumber}")
  public ResponseEntity<IncomeResponseDto> update(
      @PathVariable final Long receiptNumber,
      @Valid @RequestBody final IncomeRequestDto incomeRequest) {
    return ResponseEntity.ok(incomeService.update(receiptNumber, withAuthenticatedUser(incomeRequest)));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{receiptNumber}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long receiptNumber) {
    incomeService.delete(receiptNumber);
    return ResponseEntity.ok(
        OperationStatusModel.builder().name("DELETE INCOME").result("SUCCESSFUL").build());
  }

  private IncomeRequestDto withAuthenticatedUser(final IncomeRequestDto incomeRequest) {
    return incomeRequest.toBuilder()
        .incomeUser(userMapper.toReferenceDto(authenticatedUserPort.getAuthenticatedUser()))
        .build();
  }
}

