package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.UserMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryUseCase {

  private static final String ASC = "asc";

  private final UserPersistencePort userPersistencePort;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public UserEntity getUserById(final Long id) {
    return userPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("user.error.id.not.found"));
  }

  @Transactional(readOnly = true)
  public UserEntity getUserByEmail(final String email) {
    return userPersistencePort
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException("user.error.email.not.registered"));
  }

  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
    final UserEntity user =
        userPersistencePort
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("user.error.load.user.by.username"));
    return User.withUsername(user.getEmail())
        .password(user.getEncryptedPassword())
        .authorities(user.getRoles().stream().map(role -> role.getName().name()).toArray(String[]::new))
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }

  @Transactional(readOnly = true)
  public Page<UserEntity> getAllUsers(
      int page, final int limit, final String sortBy, final String sortDir) {
    page = page > 0 ? page - 1 : page;
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            Strings.CI.equals(sortDir, ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());
    return userPersistencePort.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public List<UserResponseDto> findAll() {
    return userPersistencePort.findAllByOrderByStartDateDesc().stream()
        .map(userMapper::toDto)
        .toList();
  }
}
