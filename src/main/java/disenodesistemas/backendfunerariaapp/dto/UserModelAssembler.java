package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.controllers.UserController;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class UserModelAssembler extends RepresentationModelAssemblerSupport<UserEntity, UserModel> {

    private final ModelMapper modelMapper;

    public UserModelAssembler(final ModelMapper modelMapper) {
        super(UserController.class, UserModel.class);
        this.modelMapper = modelMapper;
    }

    @Override
    public UserModel toModel(final UserEntity entity) {
        return modelMapper.map(entity, UserModel.class);
    }
}
