package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.ItemResponseDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ItemService extends CommonService<ItemResponseDto, ItemRequestDto, String> {
  List<ItemResponseDto> getItemsByCategoryId(Long id);

  void uploadItemImage(String code, MultipartFile image);
}
