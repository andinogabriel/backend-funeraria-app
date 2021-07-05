package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.EntryDetailCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.EntryDetailResponseDto;
import disenodesistemas.backendfunerariaapp.entities.EntryDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.EntryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface IEntryDetail {



    EntryDetailResponseDto createEntryDetail(EntryDetailCreationDto entry);

    EntryDetailResponseDto updateEntryDetail(long id, EntryDetailCreationDto entryDetailCreationDto);

    void deleteEntryDetail(long id);

    EntryDetailEntity getEntryDetailById(long id);

    default void entryTotalAmountAccumulator(EntryDetailCreationDto entry, EntryEntity entryEntity) {
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

    default void setItemStock(EntryDetailCreationDto entry, ItemEntity itemEntity) {
        if (itemEntity.getStock() != null) {
            itemEntity.setStock(Integer.sum(itemEntity.getStock(), entry.getQuantity())); //Incrementamos el stock
        } else {
            itemEntity.setStock(Integer.sum(0, entry.getQuantity())); //Incrementamos el stock
        }
    }


}
