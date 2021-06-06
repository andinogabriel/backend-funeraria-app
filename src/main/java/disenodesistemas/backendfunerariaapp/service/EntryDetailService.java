package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.EntryDetailCreationDto;
import disenodesistemas.backendfunerariaapp.dto.EntryDetailDto;
import disenodesistemas.backendfunerariaapp.entities.EntryDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.EntryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.repository.EntryDetailRepository;
import disenodesistemas.backendfunerariaapp.repository.EntryRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EntryDetailService {

    @Autowired
    EntryDetailRepository entryDetailRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    EntryRepository entryRepository;

    @Autowired
    ModelMapper mapper;

    public EntryDetailDto createEntryDetail(EntryDetailCreationDto entry) {
        EntryDetailEntity entryDetailEntity = new EntryDetailEntity();
        ItemEntity itemEntity = itemRepository.findById(entry.getItem());
        EntryEntity entryEntity = entryRepository.findById(entry.getEntry());

        entryDetailEntity.setQuantity(entry.getQuantity());
        entryDetailEntity.setPurchasePrice(entry.getPurchasePrice());
        entryDetailEntity.setSalePrice(entry.getSalePrice());
        entryDetailEntity.setItem(itemEntity);
        entryDetailEntity.setEntry(entryEntity);

        try {
            itemEntity.setPrice(entry.getSalePrice()); //Seteamos el precio al articulo
            if (itemEntity.getStock() != null) {
                itemEntity.setStock(Integer.sum(itemEntity.getStock(), entry.getQuantity())); //Incrementamos el stock
            } else {
                itemEntity.setStock(Integer.sum(0, entry.getQuantity())); //Incrementamos el stock
            }
            itemRepository.save(itemEntity);

            BigDecimal subTotal;
            BigDecimal addTax;
            //Si el total del ingreso es distinto de null entonces significa que hay detalles de ingreso en el ingreso
            if(entryEntity.getTotalAmount() != null) {
                subTotal = entryEntity.getTotalAmount().add(BigDecimal.valueOf(entry.getQuantity()).multiply(entry.getPurchasePrice()));
                addTax = (entryEntity.getTax().divide(BigDecimal.valueOf(100))).multiply(BigDecimal.valueOf(entry.getQuantity()).multiply(entry.getPurchasePrice()));
                BigDecimal total = subTotal.add(addTax); //Al subtotal se le suma el impuesto del detalle de ingreso creado
                entryEntity.setTotalAmount(new BigDecimal(total.toPlainString()).setScale(2, RoundingMode.FLOOR)); //Seteamos al ingreso el monto total
            } else {
                subTotal = BigDecimal.valueOf(entry.getQuantity()).multiply(entry.getPurchasePrice());
                addTax = (entryEntity.getTax().divide(BigDecimal.valueOf(100))).multiply(subTotal);
                BigDecimal total = subTotal.add(addTax);
                entryEntity.setTotalAmount(new BigDecimal(total.toPlainString()).setScale(2, RoundingMode.FLOOR));
            }
            entryRepository.save(entryEntity);

        } catch (IllegalArgumentException e) {
            throw  new IllegalArgumentException(e);

        }

        entryDetailRepository.save(entryDetailEntity);
        return mapper.map(entryDetailEntity, EntryDetailDto.class);
    }

}
