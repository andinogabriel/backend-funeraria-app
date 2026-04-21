package disenodesistemas.backendfunerariaapp.application.usecase.item;

import static java.util.Objects.nonNull;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.application.port.out.FileStoragePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.mapping.ItemMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemCommandUseCase {

  private final ItemPersistencePort itemPersistencePort;
  private final ItemMapper itemMapper;
  private final FileStoragePort fileStoragePort;
  private final ItemQueryUseCase itemQueryUseCase;

  @Transactional
  public ItemResponseDto create(final ItemRequestDto itemRequestDto) {
    logItemCreateStarted(itemRequestDto);
    final ItemEntity itemEntity = itemMapper.toEntity(itemRequestDto);
    itemEntity.setCode(UUID.randomUUID().toString());
    final ItemResponseDto createdItem = itemMapper.toDto(itemPersistencePort.save(itemEntity));
    logItemCompleted("item.create.completed", createdItem);
    return createdItem;
  }

  @Transactional
  public ItemResponseDto update(final String code, final ItemRequestDto itemRequestDto) {
    logItemStarted("item.update.started", code);
    final ItemEntity itemEntity = itemQueryUseCase.getItemByCode(code);
    itemMapper.updateEntity(itemRequestDto, itemEntity);
    final ItemResponseDto updatedItem = itemMapper.toDto(itemPersistencePort.save(itemEntity));
    logItemCompleted("item.update.completed", updatedItem);
    return updatedItem;
  }

  @Transactional
  public void delete(final String code) {
    final ItemEntity itemEntity = itemQueryUseCase.getItemByCode(code);
    logItemDeleteStarted(code, itemEntity);
    if (nonNull(itemEntity.getItemImageLink())) {
      fileStoragePort.deleteFiles(itemEntity);
    }
    itemPersistencePort.delete(itemEntity);
    logItemStarted("item.delete.completed", code);
  }

  @Transactional
  public void uploadItemImage(final String code, final FilePayload image) {
    final ItemEntity itemEntity = itemQueryUseCase.getItemByCode(code);
    Optional.ofNullable(image)
        .ifPresentOrElse(
            imageToUpload -> {
              logItemImageUploadStarted(code, imageToUpload);
              itemEntity.setItemImageLink(fileStoragePort.store(itemEntity, imageToUpload));
              itemPersistencePort.save(itemEntity);
              logItemStarted("item.image.upload.completed", code);
            },
            () -> logItemRejected(code, "item.image.upload.rejected", "null_payload"));
  }

  private void logItemCreateStarted(final ItemRequestDto itemRequestDto) {
    log.atInfo()
        .addKeyValue("event", "item.create.started")
        .addKeyValue("name", itemRequestDto.name())
        .log("item.create.started");
  }

  private void logItemStarted(final String event, final String code) {
    log.atInfo().addKeyValue("event", event).addKeyValue("code", code).log(event);
  }

  private void logItemCompleted(final String event, final ItemResponseDto itemResponseDto) {
    log.atInfo()
        .addKeyValue("event", event)
        .addKeyValue("code", itemResponseDto.code())
        .addKeyValue("name", itemResponseDto.name())
        .log(event);
  }

  private void logItemDeleteStarted(final String code, final ItemEntity itemEntity) {
    log.atInfo()
        .addKeyValue("event", "item.delete.started")
        .addKeyValue("code", code)
        .addKeyValue("hasImage", nonNull(itemEntity.getItemImageLink()))
        .log("item.delete.started");
  }

  private void logItemImageUploadStarted(final String code, final FilePayload imageToUpload) {
    log.atInfo()
        .addKeyValue("event", "item.image.upload.started")
        .addKeyValue("code", code)
        .addKeyValue("filename", imageToUpload.originalFilename())
        .addKeyValue("contentType", imageToUpload.contentType())
        .addKeyValue("size", imageToUpload.size())
        .log("item.image.upload.started");
  }

  private void logItemRejected(final String code, final String event, final String reason) {
    log.atWarn()
        .addKeyValue("event", event)
        .addKeyValue("code", code)
        .addKeyValue("reason", reason)
        .log(event);
  }
}
