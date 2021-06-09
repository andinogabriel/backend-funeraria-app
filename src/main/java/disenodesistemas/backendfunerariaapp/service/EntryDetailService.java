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
        ItemEntity itemEntity = getItemEntity(entry.getItem());
        EntryEntity entryEntity = getEntryEntity(entry.getEntry());

        EntryDetailEntity entryDetailEntity = new EntryDetailEntity();
        entryDetailEntity.setQuantity(entry.getQuantity());
        entryDetailEntity.setPurchasePrice(entry.getPurchasePrice());
        entryDetailEntity.setSalePrice(entry.getSalePrice());
        entryDetailEntity.setItem(itemEntity);
        entryDetailEntity.setEntry(entryEntity);

        itemEntity.setPrice(entry.getSalePrice());
        setItemStock(entry, itemEntity);
        itemRepository.save(itemEntity);

        entryTotalAmountAccumulator(entry, entryEntity);
        entryRepository.save(entryEntity);

        entryDetailRepository.save(entryDetailEntity);
        return mapper.map(entryDetailEntity, EntryDetailDto.class);
    }

    public EntryDetailDto updateEntryDetail(long id, EntryDetailCreationDto entryDetailCreationDto) {
        EntryDetailEntity entryDetailEntity = getEntryDetailById(id);
        ItemEntity itemEntity = getItemEntity(entryDetailCreationDto.getItem());
        EntryEntity entryEntity = getEntryEntity(entryDetailCreationDto.getEntry());

        //Antes de actualizar el total amount del ingreso, restamos el subtotal de este entryDetail desactualizado para que no se acumule.
        BigDecimal outdatedEntryDetail = BigDecimal.valueOf(entryDetailEntity.getQuantity()).multiply(entryDetailEntity.getPurchasePrice());
        BigDecimal tax = entryEntity.getTax().divide(BigDecimal.valueOf(100)).multiply(outdatedEntryDetail);
        BigDecimal outdatedEntryDetailSubTotal = outdatedEntryDetail.add(tax);
        //Le restamos al total amount del ingreso el subtotal de este detalle de ingreso desactualizado para que no haya una acumulacion extra.
        entryEntity.setTotalAmount(new BigDecimal((entryEntity.getTotalAmount().subtract(outdatedEntryDetailSubTotal)).toPlainString()).setScale(2, RoundingMode.FLOOR)); //Seteamos al ingreso el monto total

        //Si el item del detalle de ingreso es el mismo que el de la actualizacion, restamos para que no haya una acumulacion extra con la cantidad anterior (desactualizada)
        if(itemEntity == entryDetailEntity.getItem()) {
            itemEntity.setStock(itemEntity.getStock() - entryDetailEntity.getQuantity());
        } else {
            //Si son distintos los items de la actualizacion con el anterior, entonces al stock del item anterior le restamos la cantidad anterior que habiamos ingresado
            entryDetailEntity.getItem().setStock(entryDetailEntity.getItem().getStock() - entryDetailEntity.getQuantity());
        }

        entryDetailEntity.setQuantity(entryDetailCreationDto.getQuantity());
        entryDetailEntity.setItem(itemEntity);
        setItemStock(entryDetailCreationDto, itemEntity);
        itemEntity.setPrice(entryDetailCreationDto.getSalePrice());
        entryDetailEntity.setPurchasePrice(entryDetailCreationDto.getPurchasePrice());
        entryDetailEntity.setSalePrice(entryDetailCreationDto.getSalePrice());

        itemRepository.save(itemEntity);
        entryTotalAmountAccumulator(entryDetailCreationDto, entryEntity); //Metodo que acumula en el total amount de ingreso el precio por cantidad del item del detalle de ingreso
        entryRepository.save(entryEntity);
        EntryDetailEntity entryDetailEntityUpdated = entryDetailRepository.save(entryDetailEntity);

        return mapper.map(entryDetailEntityUpdated, EntryDetailDto.class);
    }

    public void deleteEntryDetail(long id) {
        EntryDetailEntity entryDetailEntity = getEntryDetailById(id);
        entryDetailRepository.delete(entryDetailEntity);
    }

    private EntryDetailEntity getEntryDetailById(long id) {
        EntryDetailEntity entryDetailEntity = entryDetailRepository.findById(id);
        if(entryDetailEntity == null) throw new RuntimeException("No existe detalle de ingreso con el id especificado.");
        return entryDetailEntity;
    }


    private EntryEntity getEntryEntity(long id) {
        EntryEntity entryEntity = entryRepository.findById(id);
        if (entryEntity == null) throw new RuntimeException("No existe ingreso con el ID especifcado.");
        return entryEntity;
    }

    private ItemEntity getItemEntity(long id) {
        ItemEntity itemEntity = itemRepository.findById(id);
        if (itemEntity == null) throw new RuntimeException("No existe artículo con el ID especifcado.");
        return itemEntity;
    }


    private void entryTotalAmountAccumulator(EntryDetailCreationDto entry, EntryEntity entryEntity) {
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
    }

    private void setItemStock(EntryDetailCreationDto entry, ItemEntity itemEntity) {
        if (itemEntity.getStock() != null) {
            itemEntity.setStock(Integer.sum(itemEntity.getStock(), entry.getQuantity())); //Incrementamos el stock
        } else {
            itemEntity.setStock(Integer.sum(0, entry.getQuantity())); //Incrementamos el stock
        }
    }

}
