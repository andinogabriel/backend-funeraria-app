package disenodesistemas.backendfunerariaapp.web.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.mapping.UserMapper;
import disenodesistemas.backendfunerariaapp.web.controller.UserController;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserModelAssembler implements RepresentationModelAssembler<UserEntity, EntityModel<UserDto>> {

  private final UserMapper userMapper;

  @Override
  public EntityModel<UserDto> toModel(final UserEntity entity) {
    return EntityModel.of(userMapper.toReferenceDto(entity))
        .add(linkTo(methodOn(UserController.class).getUser()).withRel("me"));
  }
}
