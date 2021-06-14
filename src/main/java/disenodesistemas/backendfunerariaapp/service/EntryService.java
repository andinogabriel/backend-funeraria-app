package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.EntryDto;
import disenodesistemas.backendfunerariaapp.entities.*;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
        SupplierEntity supplierEntity = getSupplierEntity(entryCreationDto.getEntrySupplier());
        ReceiptTypeEntity receiptTypeEntity = getReceiptTypeEntity(entryCreationDto.getReceiptType());
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
        EntryEntity entryEntity = getEntryById(id);
        SupplierEntity supplierEntity = getSupplierEntity(entryCreationDto.getEntrySupplier());
        ReceiptTypeEntity receiptTypeEntity = getReceiptTypeEntity(entryCreationDto.getReceiptType());

        if(entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()) != null && entryRepository.findByReceiptNumber(entryCreationDto.getReceiptNumber()) != entryEntity) {
            throw new RuntimeException("El número de recibo ya se encuentra registrado.");
        }
        entryEntity.setEntrySupplier(supplierEntity);
        entryEntity.setReceiptType(receiptTypeEntity);
        entryEntity.setTax(entryCreationDto.getTax());
        entryEntity.setReceiptNumber(entryCreationDto.getReceiptNumber());
        entryEntity.setReceiptSeries(entryCreationDto.getReceiptSeries());

        //Cantidad × precio de compra de todos los detalles de ingreso y luego le sumamos el impuesto para obtener el monto total
        BigDecimal subTotal = entryEntity.getEntryDetails().stream()
                .map(e -> e.getPurchasePrice().multiply(BigDecimal.valueOf(e.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subTotal.add(subTotal.multiply(entryEntity.getTax().divide(BigDecimal.valueOf(100))));
        entryEntity.setTotalAmount(new BigDecimal(total.toPlainString()).setScale(2, RoundingMode.FLOOR));

        EntryEntity entryUpdated = entryRepository.save(entryEntity);
        return mapper.map(entryUpdated, EntryDto.class);
    }

    public void deleteEntry(long id) {
        EntryEntity entryEntity = getEntryById(id);
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

    public EntryEntity getEntryById(long id) {
        EntryEntity entryEntity = entryRepository.findById(id);
        if (entryEntity == null) throw new RuntimeException("No existe ingreso con el ID especeficado.");
        return entryEntity;
    }

    private ReceiptTypeEntity getReceiptTypeEntity(long id) {
        ReceiptTypeEntity receiptTypeEntity = receiptTypeRepository.findById(id);
        if (receiptTypeEntity == null) throw new RuntimeException("No existe un tipo de recibo con el id especificado.");
        return receiptTypeEntity;
    }


    private SupplierEntity getSupplierEntity(long id) {
        SupplierEntity supplierEntity = supplierRepository.findById(id);
        if (supplierEntity == null) throw new RuntimeException("No existe proveedor con el id especificado.");
        return supplierEntity;
    }


}
