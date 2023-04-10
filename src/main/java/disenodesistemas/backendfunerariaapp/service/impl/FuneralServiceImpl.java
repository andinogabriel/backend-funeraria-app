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

@Service
@RequiredArgsConstructor
public class FuneralServiceImpl implements FuneralService {

    private final FuneralRepository funeralRepository;
    private final DeceasedRepository deceasedRepository;
    private final PlanService planService;
    private final ProjectionFactory projectionFactory;
    private final ModelMapper modelMapper;
    private final AbstractConverter<DeceasedEntity, DeceasedRequestDto> deceasedConverter;
    public static final BigDecimal TAX = BigDecimal.valueOf(21);

    @Override
    @Transactional
    public FuneralResponseDto create(final FuneralRequestDto funeralRequest) {
        if (existsByReceiptNumber(funeralRequest.getReceiptNumber()))
            throw new ConflictException("funeral.error.receiptNumber.already.exists");
        final Plan funeralPlan = planService.findById(funeralRequest.getPlan().getId());
        final DeceasedEntity deceased = deceasedRepository.save(deceasedConverter.fromDto(funeralRequest.getDeceased()));
        final BigDecimal tax = Objects.nonNull(funeralRequest.getTax()) ? funeralRequest.getTax() : TAX;
        final Funeral funeral = Funeral.builder()
                .funeralDate(funeralRequest.getFuneralDate())
                .receiptSeries(funeralRequest.getReceiptSeries())
                .tax(tax)
                .receiptType(modelMapper.map(funeralRequest.getReceiptType(), ReceiptTypeEntity.class))
                .receiptNumber(funeralRequest.getReceiptNumber())
                .plan(funeralPlan)
                .deceased(deceased)
                .build();

        funeral.setTotalAmount(getTotalAmount(funeralPlan, tax));
        final Funeral funeralCreated = funeralRepository.save(funeral);
        funeralCreated.setDeceased(deceased);

        return projectionFactory.createProjection(FuneralResponseDto.class, funeralRepository.save(funeralCreated));
    }

    @Override
    @Transactional
    public FuneralResponseDto update(final Long id, final FuneralRequestDto funeralRequest) {
        final Funeral funeralToUpdate = findById(id);
        if (existsByReceiptNumber(funeralRequest.getReceiptNumber())
                && !Objects.equals(funeralRequest.getReceiptNumber(), funeralToUpdate.getReceiptNumber()))
            throw new ConflictException("funeral.error.receiptNumber.already.exists");
        funeralToUpdate.setFuneralDate(funeralToUpdate.getFuneralDate());
        funeralToUpdate.setReceiptSeries(funeralRequest.getReceiptSeries());
        funeralToUpdate.setTax(Objects.nonNull(funeralRequest.getTax()) ? funeralRequest.getTax() : BigDecimal.valueOf(21));
        funeralToUpdate.setTotalAmount(getTotalAmount(funeralToUpdate.getPlan(), Objects.nonNull(funeralRequest.getTax()) ? funeralRequest.getTax() : BigDecimal.valueOf(21)));
        return projectionFactory.createProjection(FuneralResponseDto.class, funeralRepository.save(funeralToUpdate));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        funeralRepository.delete(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuneralResponseDto> findAll() {
        return funeralRepository.findAllByOrderByRegisterDateDesc();
    }

    private Funeral findById(final Long id) {
        return funeralRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("funeral.error.not.found"));
    }

    private boolean existsByReceiptNumber(final String receiptNumber) {
        return funeralRepository.existsByReceiptNumber(receiptNumber);
    }

    private static BigDecimal getTotalAmount(final Plan funeralPlan, final BigDecimal tax) {
        return funeralPlan.getPrice()
                .add(funeralPlan.getPrice()
                    .multiply(tax.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                )
                .setScale(2, RoundingMode.HALF_UP);
    }

}
