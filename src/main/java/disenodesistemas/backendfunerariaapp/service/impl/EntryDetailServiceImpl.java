package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.EntryDetailCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.EntryDetailResponseDto;
import disenodesistemas.backendfunerariaapp.entities.EntryDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.EntryEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.EntryDetailRepository;
import disenodesistemas.backendfunerariaapp.repository.EntryRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.IEntry;
import disenodesistemas.backendfunerariaapp.service.Interface.IEntryDetail;
import disenodesistemas.backendfunerariaapp.service.Interface.IItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class EntryDetailServiceImpl implements IEntryDetail {

    private final EntryDetailRepository entryDetailRepository;
    private final ItemRepository itemRepository;
    private final IItem itemService;
    private final IEntry entryService;
    private final EntryRepository entryRepository;
    private final ProjectionFactory projectionFactory;
    private final MessageSource messageSource;

    @Autowired
    public EntryDetailServiceImpl(EntryDetailRepository entryDetailRepository, ItemRepository itemRepository, IItem itemService, IEntry entryService, EntryRepository entryRepository, ProjectionFactory projectionFactory, MessageSource messageSource) {
        this.entryDetailRepository = entryDetailRepository;
        this.itemRepository = itemRepository;
        this.itemService = itemService;
        this.entryService = entryService;
        this.entryRepository = entryRepository;
        this.projectionFactory = projectionFactory;
        this.messageSource = messageSource;
    }


    @Override
    public EntryDetailResponseDto createEntryDetail(EntryDetailCreationDto entry) {
        ItemEntity itemEntity = itemService.getItemById(entry.getItem());
        EntryEntity entryEntity = entryService.getEntryById(entry.getEntry());

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

        return projectionFactory.createProjection(EntryDetailResponseDto.class, entryDetailRepository.save(entryDetailEntity));
    }

    @Override
    public EntryDetailResponseDto updateEntryDetail(Long id, EntryDetailCreationDto entryDetailCreationDto) {
        EntryDetailEntity entryDetailEntity = getEntryDetailById(id);
        ItemEntity itemEntity = itemService.getItemById(entryDetailCreationDto.getItem());

        entryDetailEntity.setPurchasePrice(entryDetailCreationDto.getPurchasePrice());
        entryDetailEntity.setSalePrice(entryDetailCreationDto.getSalePrice());

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
        itemRepository.save(itemEntity);
        EntryDetailEntity entryDetailEntityUpdated = entryDetailRepository.save(entryDetailEntity);

        return projectionFactory.createProjection(EntryDetailResponseDto.class, entryDetailEntityUpdated);
    }

    @Override
    public void deleteEntryDetail(Long id) {
        EntryDetailEntity entryDetailEntity = getEntryDetailById(id);
        entryDetailEntity.getItem().setStock(entryDetailEntity.getItem().getStock() - entryDetailEntity.getQuantity());
        itemRepository.save(entryDetailEntity.getItem());
        entryDetailRepository.delete(entryDetailEntity);
    }

    @Override
    public EntryDetailEntity getEntryDetailById(Long id) {
        return entryDetailRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("entryDetail.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

}
