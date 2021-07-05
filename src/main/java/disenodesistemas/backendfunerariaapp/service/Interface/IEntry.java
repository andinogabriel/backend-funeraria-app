package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.EntryResponseDto;
import disenodesistemas.backendfunerariaapp.entities.EntryEntity;
import disenodesistemas.backendfunerariaapp.entities.ReceiptTypeEntity;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public interface IEntry {

    EntryResponseDto createEntry(EntryCreationDto entryCreationDto);

    List<EntryResponseDto> getAllEntries();

    EntryResponseDto updateEntry(long id, EntryCreationDto entryCreationDto);

    void deleteEntry(long id);

    Page<EntryResponseDto> getEntriesPaginated(int page, int limit, String sortBy, String sortDir);

    EntryEntity getEntryById(long id);

    EntryResponseDto getProjectedEntryById(long id);

    ReceiptTypeEntity getReceiptTypeEntity(long id);

    default void totalAmountCalculator(EntryEntity entryEntity, EntryCreationDto entryCreationDto) {
        entryEntity.setTotalAmount(BigDecimal.valueOf(0));
        //Cantidad × precio de compra de todos los detalles de ingreso y luego le sumamos el impuesto para obtener el monto total
        BigDecimal subTotal = entryEntity.getEntryDetails().stream()
                .map(e -> e.getPurchasePrice().multiply(BigDecimal.valueOf(e.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subTotal.add(subTotal.multiply(entryCreationDto.getTax().divide(BigDecimal.valueOf(100))));
        entryEntity.setTotalAmount(new BigDecimal(total.toPlainString()).setScale(2, RoundingMode.FLOOR));
    }

}
