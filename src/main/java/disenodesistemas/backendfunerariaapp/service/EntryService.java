package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.EntryDto;
import disenodesistemas.backendfunerariaapp.entities.EntryEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.EntryRepository;
import disenodesistemas.backendfunerariaapp.repository.ReceiptTypeRepository;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EntryService {

    @Autowired
    EntryRepository entryRepository;

    @Autowired
    ReceiptTypeRepository receiptTypeRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper mapper;

    public EntryDto createEntry(EntryCreationDto entryCreationDto) {

        if(entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()) != null) {
            throw new RuntimeException("El número de recibo ya se encuentra registrado.");
        }

        EntryEntity entryEntity = new EntryEntity();
        ReceiptTypeEntity receiptTypeEntity = receiptTypeRepository.findById(entryCreationDto.getReceiptType());
        SupplierEntity supplierEntity = supplierRepository.findById(entryCreationDto.getEntrySupplier());
        UserEntity userEntity = userRepository.findByEmail(entryCreationDto.getEntryUser());

        entryEntity.setReceiptNumber(entryCreationDto.getReceiptNumber());
        entryEntity.setReceiptSeries(entryCreationDto.getReceiptSeries());
        entryEntity.setTax(entryCreationDto.getTax());
        entryEntity.setReceiptType(receiptTypeEntity);
        entryEntity.setEntrySupplier(supplierEntity);
        entryEntity.setEntryUser(userEntity);

        EntryEntity entryEntitySaved = entryRepository.save(entryEntity);
        return mapper.map(entryEntitySaved, EntryDto.class);
    }

    public Page<EntryDto> getEntriesPaginated(int page, int limit, String sortBy, String sortDir) {
        if (page > 0) {
            page = page - 1;
        }

        Pageable pageable = PageRequest.of(
                page, limit,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );
        Page<EntryEntity> entriesEntity = entryRepository.findAll(pageable);
        return mapper.map(entriesEntity, Page.class);
    }

}
