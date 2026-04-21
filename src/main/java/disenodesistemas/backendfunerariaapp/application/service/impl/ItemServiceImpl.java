package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.application.service.ItemService;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

  private final ItemCommandUseCase itemCommandUseCase;
  private final ItemQueryUseCase itemQueryUseCase;

  @Override
  public List<ItemResponseDto> findAll() {
    return itemQueryUseCase.findAll();
  }

  @Override
  public List<ItemResponseDto> getItemsByCategoryId(final Long id) {
    return itemQueryUseCase.getItemsByCategoryId(id);
  }

  @Override
  public ItemResponseDto create(final ItemRequestDto itemRequestDto) {
    return itemCommandUseCase.create(itemRequestDto);
  }

  @Override
  public ItemResponseDto update(final String code, final ItemRequestDto itemRequestDto) {
    return itemCommandUseCase.update(code, itemRequestDto);
  }

  @Override
  public void delete(final String code) {
    itemCommandUseCase.delete(code);
  }

  @Override
  public void uploadItemImage(final String code, final FilePayload image) {
    itemCommandUseCase.uploadItemImage(code, image);
  }

  @Override
  public ItemResponseDto findById(final String code) {
    return itemQueryUseCase.findById(code);
  }
}

