package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.entities.Funeral;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.DeceasedRepository;
import disenodesistemas.backendfunerariaapp.repository.FuneralRepository;
import disenodesistemas.backendfunerariaapp.service.FuneralService;
import disenodesistemas.backendfunerariaapp.service.PlanService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

@Service
@RequiredArgsConstructor
public class FuneralServiceImpl implements FuneralService {

    private final FuneralRepository funeralRepository;
    private final DeceasedRepository deceasedRepository;
    private final PlanService planService;
    private final ProjectionFactory projectionFactory;
    private final ModelMapper modelMapper;
    private final AbstractConverter<DeceasedEntity, DeceasedRequestDto> deceasedConverter;
    public static final BigDecimal DEFAULT_TAX = BigDecimal.valueOf(21);

    @Override
    @Transactional
    public FuneralResponseDto create(final FuneralRequestDto funeralRequest) {
        validateReceiptNumber(funeralRequest.getReceiptNumber());
        final Plan funeralPlan = planService.findById(funeralRequest.getPlan().getId());
        final DeceasedEntity deceased = saveDeceased(funeralRequest.getDeceased());
        final BigDecimal tax = requireNonNullElse(funeralRequest.getTax(), DEFAULT_TAX);
        final Funeral funeral = buildFuneral(funeralRequest, funeralPlan, deceased, tax);
        funeral.setDeceased(deceased);
        final Funeral savedFuneral = funeralRepository.save(funeral);
        return projectionFactory.createProjection(FuneralResponseDto.class, savedFuneral);
    }

    @Override
    @Transactional
    public FuneralResponseDto update(final Long id, final FuneralRequestDto funeralRequest) {
        final Funeral funeralToUpdate = findEntityById(id);
        validateUniqueReceiptNumber(funeralRequest.getReceiptNumber(), funeralToUpdate.getReceiptNumber());
        funeralToUpdate.setFuneralDate(funeralToUpdate.getFuneralDate());
        funeralToUpdate.setReceiptSeries(funeralRequest.getReceiptSeries());
        final BigDecimal tax = requireNonNullElse(funeralRequest.getTax(), DEFAULT_TAX);
        funeralToUpdate.setTax(tax);
        funeralToUpdate.setTotalAmount(getTotalAmount(funeralToUpdate.getPlan(), tax));
        return projectionFactory.createProjection(FuneralResponseDto.class, funeralRepository.save(funeralToUpdate));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        funeralRepository.delete(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuneralResponseDto> findAll() {
        return funeralRepository.findAllByOrderByRegisterDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public FuneralResponseDto findById(final Long id) {
        return projectionFactory.createProjection(FuneralResponseDto.class, findEntityById(id));
    }

    private Funeral findEntityById(final Long id) {
        return funeralRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("funeral.error.not.found"));
    }

    private void validateReceiptNumber(final String receiptNumber) {
        if (existsByReceiptNumber(receiptNumber))
            throw new ConflictException("funeral.error.receiptNumber.already.exists");
    }

    private void validateUniqueReceiptNumber(final String newReceiptNumber, final String oldReceiptNumber) {
        if (existsByReceiptNumber(newReceiptNumber) && !Objects.equals(newReceiptNumber, oldReceiptNumber)) {
            throw new ConflictException("funeral.error.receiptNumber.already.exists");
        }
    }

    private DeceasedEntity saveDeceased(final DeceasedRequestDto deceasedRequest) {
        return deceasedRepository.save(deceasedConverter.fromDto(deceasedRequest));
    }

    private BigDecimal getTotalAmount(final Plan funeralPlan, final BigDecimal tax) {
        final BigDecimal taxDecimal = tax.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        final BigDecimal priceWithTax = funeralPlan.getPrice().multiply(BigDecimal.ONE.add(taxDecimal));
        return priceWithTax.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean existsByReceiptNumber(final String receiptNumber) {
        return funeralRepository.existsByReceiptNumber(receiptNumber);
    }

    private Funeral buildFuneral(final FuneralRequestDto funeralRequest, final Plan funeralPlan,
                                 final DeceasedEntity deceased, final BigDecimal tax) {
        return Funeral.builder()
                .funeralDate(funeralRequest.getFuneralDate())
                .receiptSeries(funeralRequest.getReceiptSeries())
                .tax(tax)
                .receiptType(modelMapper.map(funeralRequest.getReceiptType(), ReceiptTypeEntity.class))
                .receiptNumber(funeralRequest.getReceiptNumber())
                .plan(funeralPlan)
                .deceased(deceased)
                .totalAmount(getTotalAmount(funeralPlan, tax))
                .build();
    }

}
