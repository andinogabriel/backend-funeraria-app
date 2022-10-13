package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.IncomeService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;
    private final EntityManager entityManager;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<IncomeResponseDto> getIncomes() {
        return incomeService.getAllIncomes();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{receiptNumber}")
    public IncomeResponseDto getIncomeById(@PathVariable final Long receiptNumber) {
        return incomeService.findByReceiptNumber(receiptNumber);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/paginated")
    public Page<IncomeResponseDto> getIncomesPaginated(@RequestParam(value = "isDeleted", required = false, defaultValue = "false") final boolean isDeleted,
                                                       @RequestParam(value = "page", defaultValue = "0") final int page,
                                                       @RequestParam(value="limit", defaultValue = "5") final int limit,
                                                       @RequestParam(value = "sortBy", defaultValue = "incomeDate") final String sortBy,
                                                       @RequestParam(value = "sortDir", defaultValue = "desc") final String sortDir) {
        final Session session = entityManager.unwrap(Session.class);
        final Filter filter = session.enableFilter("deletedIncomesFilter");
        filter.setParameter("isDeleted", isDeleted);
        final Page<IncomeResponseDto> entries = incomeService.getIncomesPaginated(page, limit, sortBy, sortDir);
        session.disableFilter("deletedIncomesFilter");
        return entries;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public IncomeResponseDto createIncome(@RequestBody @Valid final IncomeRequestDto incomeRequest) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        val incomeRequestDto = IncomeRequestDto.builder()
                .supplier(incomeRequest.getSupplier())
                .incomeDetails(incomeRequest.getIncomeDetails())
                .incomeUser(UserDto.builder().email(email).build())
                .receiptNumber(incomeRequest.getReceiptNumber())
                .receiptSeries(incomeRequest.getReceiptSeries())
                .receiptType(incomeRequest.getReceiptType())
                .tax(incomeRequest.getTax())
                .build();
        return incomeService.createIncome(incomeRequestDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{receiptNumber}")
    public IncomeResponseDto updateIncome(@PathVariable final Long receiptNumber, @Valid @RequestBody final IncomeRequestDto incomeRequest) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        final IncomeRequestDto incomeRequestDto = IncomeRequestDto.builder()
                .supplier(incomeRequest.getSupplier())
                .incomeDetails(incomeRequest.getIncomeDetails())
                .incomeUser(UserDto.builder().email(email).build())
                .receiptNumber(incomeRequest.getReceiptNumber())
                .receiptSeries(incomeRequest.getReceiptSeries())
                .receiptType(incomeRequest.getReceiptType())
                .tax(incomeRequest.getTax())
                .build();
        return incomeService.updateIncome(receiptNumber, incomeRequestDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{receiptNumber}")
    public OperationStatusModel deleteIncome(@PathVariable final Long receiptNumber) {
        incomeService.deleteIncome(receiptNumber);
        return OperationStatusModel.builder()
                .name("DELETE")
                .name("SUCCESS")
                .build();
    }

}
