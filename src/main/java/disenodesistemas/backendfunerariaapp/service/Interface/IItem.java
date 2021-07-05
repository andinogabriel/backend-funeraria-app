package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.ItemCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IItem {

    List<ItemResponseDto> getAllItems();

    List<ItemResponseDto> getItemsByCategoryId(long id);

    ItemResponseDto createItem(ItemCreationDto itemCreationDto);

    ItemResponseDto updateItem(long id, ItemCreationDto itemCreationDto);

    void deleteItem(long id);

    ItemEntity getItemById(long id);

    void uploadItemImage(long id, MultipartFile file);

}
