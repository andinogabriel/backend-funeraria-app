package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.ItemCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IItem {

    List<ItemResponseDto> getAllItems();

    List<ItemResponseDto> getItemsByCategoryId(Long id);

    ItemResponseDto createItem(ItemCreationDto itemCreationDto);

    ItemResponseDto updateItem(Long id, ItemCreationDto itemCreationDto);

    void deleteItem(Long id);

    ItemEntity getItemById(Long id);

    void uploadItemImage(Long id, MultipartFile image);

}
