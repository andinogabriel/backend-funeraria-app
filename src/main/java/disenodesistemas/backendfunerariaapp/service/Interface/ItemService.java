package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {

    List<ItemResponseDto> getAllItems();

    List<ItemResponseDto> getItemsByCategoryId(Long id);

    ItemResponseDto createItem(ItemRequestDto itemRequestDto);

    ItemResponseDto updateItem(String code, ItemRequestDto itemRequestDto);

    void deleteItem(String code);

    void uploadItemImage(String code, MultipartFile image);
    ItemResponseDto findItemByCode(String code);

}
