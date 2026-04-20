package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import java.util.List;

public interface ItemService {

  ItemResponseDto create(ItemRequestDto dto);

  ItemResponseDto update(String code, ItemRequestDto dto);

  void delete(String code);

  List<ItemResponseDto> findAll();

  ItemResponseDto findById(String code);

  List<ItemResponseDto> getItemsByCategoryId(Long id);

  void uploadItemImage(String code, FilePayload image);
}
