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
        itemEntity.setPrice(entry.getSalePrice());

        itemEntity.setStock(itemEntity.getStock() + entry.getQuantity());
        entryEntity.setTotalAmount(entryEntity.getTotalAmount().add(BigDecimal.valueOf(entry.getQuantity()).multiply(entry.getSalePrice())));


        EntryDetailEntity entryDetailSaved = entryDetailRepository.save(entryDetailEntity);
        return mapper.map(entryDetailSaved, EntryDetailDto.class);
    }

}
