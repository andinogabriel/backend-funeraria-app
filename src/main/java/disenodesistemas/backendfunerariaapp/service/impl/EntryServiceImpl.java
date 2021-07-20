package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.EntryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.*;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.EntryRepository;
import disenodesistemas.backendfunerariaapp.repository.ReceiptTypeRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IEntry;
import disenodesistemas.backendfunerariaapp.service.Interface.ISupplier;
import disenodesistemas.backendfunerariaapp.service.Interface.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class EntryServiceImpl implements IEntry {

    private final EntryRepository entryRepository;
    private final ReceiptTypeRepository receiptTypeRepository;
    private final ISupplier supplierService;
    private final IUser userService;
    private final MessageSource messageSource;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public EntryServiceImpl(EntryRepository entryRepository, ReceiptTypeRepository receiptTypeRepository, ISupplier supplierService, IUser userService, MessageSource messageSource, ProjectionFactory projectionFactory) {
        this.entryRepository = entryRepository;
        this.receiptTypeRepository = receiptTypeRepository;
        this.supplierService = supplierService;
        this.userService = userService;
        this.messageSource = messageSource;
        this.projectionFactory = projectionFactory;
    }

    @Override
    public EntryResponseDto createEntry(EntryCreationDto entryCreationDto) {
        SupplierEntity supplierEntity = supplierService.getSupplierById(entryCreationDto.getEntrySupplier());
        ReceiptTypeEntity receiptTypeEntity = getReceiptTypeEntity(entryCreationDto.getReceiptType());
        UserEntity userEntity = userService.getUserByEmail(entryCreationDto.getEntryUser());

        if(entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()).isPresent())
            throw new AppException(
                    messageSource.getMessage("entry.error.receiptNumber.already.registered", null, Locale.getDefault()),
                    HttpStatus.CONFLICT
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
    public EntryResponseDto updateEntry(Long id, EntryCreationDto entryCreationDto) {
        EntryEntity entryEntity = getEntryById(id);
        SupplierEntity supplierEntity = supplierService.getSupplierById(entryCreationDto.getEntrySupplier());
        ReceiptTypeEntity receiptTypeEntity = getReceiptTypeEntity(entryCreationDto.getReceiptType());

        if(entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()).isPresent() && entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()).get() != entryEntity) {
            throw new AppException(
                    messageSource.getMessage("entry.error.receiptNumber.already.registered", null, Locale.getDefault()),
                    HttpStatus.CONFLICT
            );
        }
        entryEntity.setEntrySupplier(supplierEntity);
        entryEntity.setReceiptType(receiptTypeEntity);
        entryEntity.setTax(entryCreationDto.getTax());
        entryEntity.setReceiptNumber(entryCreationDto.getReceiptNumber());
        entryEntity.setReceiptSeries(entryCreationDto.getReceiptSeries());

        totalAmountCalculator(entryEntity, entryCreationDto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        entryEntity.setLastModifiedBy(userService.getUserByEmail(email));

        EntryEntity entryUpdated = entryRepository.save(entryEntity);
        return projectionFactory.createProjection(EntryResponseDto.class, entryUpdated);
    }

    @Override
    public void deleteEntry(Long id) {
        EntryEntity entryEntity = getEntryById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        entryEntity.setLastModifiedBy(userService.getUserByEmail(email));
        entryRepository.save(entryEntity);
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
    public EntryEntity getEntryById(Long id) {
        return entryRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("entry.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

    @Override
    public EntryResponseDto getProjectedEntryById(Long id) {
        return entryRepository.getById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("entry.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

    @Override
    public ReceiptTypeEntity getReceiptTypeEntity(Long id) {
        return receiptTypeRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("receiptType.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

}
