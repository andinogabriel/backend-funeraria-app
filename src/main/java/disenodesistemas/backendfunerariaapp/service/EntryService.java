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

import java.util.ArrayList;
import java.util.List;

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
        SupplierEntity supplierEntity = supplierRepository.findById(entryCreationDto.getEntrySupplier());
        if (supplierEntity == null) throw new RuntimeException("No existe proveedor con el id especificado.");
        ReceiptTypeEntity receiptTypeEntity = receiptTypeRepository.findById(entryCreationDto.getReceiptType());
        if (receiptTypeEntity == null) throw new RuntimeException("No existe un tipo de recibo con el id especificado.");
        UserEntity userEntity = userRepository.findByEmail(entryCreationDto.getEntryUser());

        if(entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()) != null) throw new RuntimeException("El número de recibo ya se encuentra registrado.");

        EntryEntity entryEntity = new EntryEntity();

        entryEntity.setReceiptNumber(entryCreationDto.getReceiptNumber());
        entryEntity.setReceiptSeries(entryCreationDto.getReceiptSeries());
        entryEntity.setTax(entryCreationDto.getTax());
        entryEntity.setReceiptType(receiptTypeEntity);
        entryEntity.setEntrySupplier(supplierEntity);
        entryEntity.setEntryUser(userEntity);

        EntryEntity entryEntitySaved = entryRepository.save(entryEntity);
        return mapper.map(entryEntitySaved, EntryDto.class);
    }

    public List<EntryDto> getAllEntries() {
        List<EntryEntity> entriesEntity = entryRepository.findAllByOrderByIdDesc();
        List<EntryDto> entriesDto = new ArrayList<>();
        entriesEntity.forEach(e -> entriesDto.add(mapper.map(e, EntryDto.class)));
        return entriesDto;
    }

    public EntryDto updateEntry(long id, EntryCreationDto entryCreationDto) {
        EntryEntity entryEntity = entryRepository.findById(id);
        if (entryEntity == null) throw new RuntimeException("No existe ingreso con el ID especeficado.");
        SupplierEntity supplierEntity = supplierRepository.findById(entryCreationDto.getEntrySupplier());
        if (supplierEntity == null) throw new RuntimeException("No existe proveedor con el id especificado.");
        ReceiptTypeEntity receiptTypeEntity = receiptTypeRepository.findById(entryCreationDto.getReceiptType());
        if (receiptTypeEntity == null) throw new RuntimeException("No existe un tipo de recibo con el id especificado.");

        if(entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()) != null) throw new RuntimeException("El número de recibo ya se encuentra registrado.");

        entryEntity.setEntrySupplier(supplierEntity);
        entryEntity.setTax(entryCreationDto.getTax());
        entryEntity.setReceiptNumber(entryCreationDto.getReceiptNumber());
        entryEntity.setReceiptSeries(entryCreationDto.getReceiptSeries());

        return mapper.map(entryEntity, EntryDto.class);
    }

    public void deleteEntry(long id) {
        EntryEntity entryEntity = entryRepository.findById(id);
        if (entryEntity == null) throw new RuntimeException("No existe ingreso con el ID especeficado.");
        entryRepository.delete(entryEntity);
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
