package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.EntryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.*;
import disenodesistemas.backendfunerariaapp.repository.EntryRepository;
import disenodesistemas.backendfunerariaapp.repository.ReceiptTypeRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IEntry;
import disenodesistemas.backendfunerariaapp.service.Interface.ISupplier;
import disenodesistemas.backendfunerariaapp.service.Interface.IUser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Locale;

@Service
public class EntryServiceImpl implements IEntry {

    private final EntryRepository entryRepository;
    private final ReceiptTypeRepository receiptTypeRepository;
    private final ISupplier supplierService;
    private final IUser userService;
    private final ModelMapper mapper;
    private final MessageSource messageSource;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public EntryServiceImpl(EntryRepository entryRepository, ReceiptTypeRepository receiptTypeRepository, ISupplier supplierService, IUser userService, ModelMapper mapper, MessageSource messageSource, ProjectionFactory projectionFactory) {
        this.entryRepository = entryRepository;
        this.receiptTypeRepository = receiptTypeRepository;
        this.supplierService = supplierService;
        this.userService = userService;
        this.mapper = mapper;
        this.messageSource = messageSource;
        this.projectionFactory = projectionFactory;
    }


    @Override
    public EntryResponseDto createEntry(EntryCreationDto entryCreationDto) {
        SupplierEntity supplierEntity = supplierService.getSupplierById(entryCreationDto.getEntrySupplier());
        ReceiptTypeEntity receiptTypeEntity = getReceiptTypeEntity(entryCreationDto.getReceiptType());
        UserEntity userEntity = userService.getUserByEmail(entryCreationDto.getEntryUser());

        if(entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()).isPresent())
            throw new RuntimeException(
                    messageSource.getMessage("entry.error.receiptNumber.already.registered", null, Locale.getDefault())
            );

        EntryEntity entryEntity = new EntryEntity();

        entryEntity.setReceiptNumber(entryCreationDto.getReceiptNumber());
        entryEntity.setReceiptSeries(entryCreationDto.getReceiptSeries());
        entryEntity.setTax(entryCreationDto.getTax());
        entryEntity.setReceiptType(receiptTypeEntity);
        entryEntity.setEntrySupplier(supplierEntity);
        entryEntity.setEntryUser(userEntity);

        EntryEntity entryEntitySaved = entryRepository.save(entryEntity);
        return projectionFactory.createProjection(EntryResponseDto.class, entryEntitySaved);
    }

    @Override
    public List<EntryResponseDto> getAllEntries() {
        return entryRepository.findAllByOrderByIdDesc();
    }

    @Override
    public EntryResponseDto updateEntry(long id, EntryCreationDto entryCreationDto) {
        EntryEntity entryEntity = getEntryById(id);
        SupplierEntity supplierEntity = supplierService.getSupplierById(entryCreationDto.getEntrySupplier());
        ReceiptTypeEntity receiptTypeEntity = getReceiptTypeEntity(entryCreationDto.getReceiptType());

        if(entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()).isPresent() && entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()).get() != entryEntity) {
            throw new RuntimeException(
                    messageSource.getMessage("entry.error.receiptNumber.already.registered", null, Locale.getDefault())
            );
        }
        entryEntity.setEntrySupplier(supplierEntity);
        entryEntity.setReceiptType(receiptTypeEntity);
        entryEntity.setTax(entryCreationDto.getTax());
        entryEntity.setReceiptNumber(entryCreationDto.getReceiptNumber());
        entryEntity.setReceiptSeries(entryCreationDto.getReceiptSeries());

        totalAmountCalculator(entryEntity, entryCreationDto);

        EntryEntity entryUpdated = entryRepository.save(entryEntity);
        return projectionFactory.createProjection(EntryResponseDto.class, entryUpdated);
    }

    @Override
    public void deleteEntry(long id) {
        EntryEntity entryEntity = getEntryById(id);
        entryRepository.delete(entryEntity);
    }

    @Override
    public Page<EntryResponseDto> getEntriesPaginated(int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );
        return entryRepository.findAllProjectedBy(pageable);
    }

    @Override
    public EntryEntity getEntryById(long id) {
        return entryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("entry.error.not.found", null, Locale.getDefault())
                )
        );
    }

    @Override
    public EntryResponseDto getProjectedEntryById(long id) {
        return entryRepository.getById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("entry.error.not.found", null, Locale.getDefault())
                )
        );
    }

    @Override
    public ReceiptTypeEntity getReceiptTypeEntity(long id) {
        return receiptTypeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("receiptType.error.not.found", null, Locale.getDefault())
                )
        );
    }

}
