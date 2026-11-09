package disenodesistemas.backendfunerariaapp.modern.application.usecase.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipHolderPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipHolderUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.mapping.MembershipHolderMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipHolderRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderDetailDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderSummaryDto;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MembershipHolderUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-11-08T12:00:00Z");

  @Mock private MembershipHolderPersistencePort port;
  @Mock private MembershipHolderMapper holderMapper;
  @Mock private AffiliateMapper affiliateMapper;

  private MembershipHolderUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new MembershipHolderUseCase(
            port, holderMapper, affiliateMapper, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private static MembershipHolderRequestDto request(final String phone, final String email) {
    return MembershipHolderRequestDto.builder()
        .dni(20111222)
        .firstName("  Rosa ")
        .lastName("Perez")
        .phone(phone)
        .email(email)
        .build();
  }

  private static UserEntity user(final Role role) {
    final UserEntity user = new UserEntity();
    user.setEmail(role.name().toLowerCase() + "@funeraria.local");
    final RoleEntity roleEntity = new RoleEntity();
    roleEntity.setName(role);
    user.setRoles(Set.of(roleEntity));
    return user;
  }

  private static AffiliateEntity affiliate(final String fee, final boolean deceased) {
    return AffiliateEntity.builder()
        .monthlyFee(fee == null ? null : new BigDecimal(fee))
        .deceased(deceased)
        .build();
  }

  @Test
  @DisplayName("Given an unknown DNI when resolving the holder then it creates one, trims names and stamps createdAt")
  void createsNewHolder() {
    when(port.findByDni(20111222)).thenReturn(Optional.empty());
    when(port.save(any())).thenAnswer(inv -> inv.getArgument(0));

    final MembershipHolderEntity holder =
        useCase.resolveForEnrolment(request("221-555", "rosa@mail.com"), user(Role.ROLE_ADMIN));

    assertThat(holder.getDni()).isEqualTo(20111222);
    assertThat(holder.getFirstName()).isEqualTo("Rosa");
    assertThat(holder.getPhone()).isEqualTo("221-555");
    assertThat(holder.getEmail()).isEqualTo("rosa@mail.com");
    assertThat(holder.getCreatedAt()).isEqualTo(NOW);
    assertThat(holder.getUser()).as("admins enrol at the desk, no login is linked").isNull();
  }

  @Test
  @DisplayName("Given an existing DNI when resolving the holder then it is reused, names refreshed and blank contact data does not erase stored values")
  void reusesExistingHolder() {
    final Instant created = Instant.parse("2025-01-01T00:00:00Z");
    final MembershipHolderEntity existing =
        MembershipHolderEntity.builder()
            .id(5L)
            .dni(20111222)
            .firstName("Rosa M.")
            .lastName("Perez")
            .phone("221-000")
            .email("old@mail.com")
            .createdAt(created)
            .build();
    when(port.findByDni(20111222)).thenReturn(Optional.of(existing));
    when(port.save(existing)).thenReturn(existing);

    final MembershipHolderEntity holder =
        useCase.resolveForEnrolment(request(" ", null), user(Role.ROLE_ADMIN));

    assertThat(holder).isSameAs(existing);
    assertThat(holder.getFirstName()).isEqualTo("Rosa");
    assertThat(holder.getPhone()).isEqualTo("221-000");
    assertThat(holder.getEmail()).isEqualTo("old@mail.com");
    assertThat(holder.getCreatedAt()).isEqualTo(created);
  }

  @Test
  @DisplayName("Given a self-service user when resolving a holder without a login then the user's account is linked")
  void linksSelfServiceUser() {
    final UserEntity self = user(Role.ROLE_USER);
    when(port.findByDni(20111222)).thenReturn(Optional.empty());
    when(port.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertThat(useCase.resolveForEnrolment(request(null, null), self).getUser()).isSameAs(self);
  }

  private static UserEntity user(final Role role, final long id) {
    final UserEntity user = user(role);
    user.setId(id);
    return user;
  }

  @Test
  @DisplayName("Given another holder's DNI when a self-service user enrols then the holder is reused as stored, neither refreshed nor relinked")
  void selfServiceCannotOverwriteSomeoneElsesHolder() {
    final UserEntity owner = user(Role.ROLE_USER, 1L);
    final MembershipHolderEntity linked =
        MembershipHolderEntity.builder().id(5L).dni(20111222).firstName("Rosa M.").user(owner).build();
    final MembershipHolderEntity unlinked =
        MembershipHolderEntity.builder().id(6L).dni(20111222).firstName("Rosa M.").build();
    when(port.findByDni(20111222)).thenReturn(Optional.of(linked), Optional.of(unlinked));

    final UserEntity stranger = user(Role.ROLE_USER, 2L);
    final MembershipHolderEntity first = useCase.resolveForEnrolment(request("1", "x@mail.com"), stranger);
    final MembershipHolderEntity second = useCase.resolveForEnrolment(request("1", "x@mail.com"), stranger);

    assertThat(first.getUser()).isSameAs(owner);
    assertThat(first.getFirstName()).isEqualTo("Rosa M.");
    assertThat(second.getUser()).as("an existing unlinked holder is not claimed").isNull();
    assertThat(second.getFirstName()).isEqualTo("Rosa M.");
    verify(port, never()).save(any());
  }

  @Test
  @DisplayName("Given the self-service user linked to the holder when they enrol again then the holder data is refreshed")
  void linkedUserRefreshesOwnHolder() {
    final UserEntity owner = user(Role.ROLE_USER, 1L);
    final MembershipHolderEntity existing =
        MembershipHolderEntity.builder().id(5L).dni(20111222).firstName("Rosa M.").user(owner).build();
    when(port.findByDni(20111222)).thenReturn(Optional.of(existing));
    when(port.save(existing)).thenReturn(existing);

    final MembershipHolderEntity holder =
        useCase.resolveForEnrolment(request(null, null), user(Role.ROLE_USER, 1L));

    assertThat(holder.getFirstName()).isEqualTo("Rosa");
    assertThat(holder.getUser()).isSameAs(owner);
  }

  @Test
  @DisplayName("Given a blank search when searching holders then it returns nothing without touching storage")
  void blankSearch() {
    assertThat(useCase.search("  ")).isEmpty();
    assertThat(useCase.search(null)).isEmpty();
    verify(port, never()).search(anyString(), anyInt());
  }

  @Test
  @DisplayName("Given holders with mixed affiliates when searching then totals only count alive, priced affiliates")
  void searchTotals() {
    final MembershipHolderEntity holder = MembershipHolderEntity.builder().id(5L).build();
    final MembershipHolderResponseDto holderDto =
        new MembershipHolderResponseDto(5L, 20111222, "Rosa", "Perez", null, null);
    when(port.search("perez", 20))
        .thenReturn(List.of(holder));
    when(port.findAffiliatesOf(5L))
        .thenReturn(
            List.of(
                affiliate("2000.00", false),
                affiliate("3600.00", false),
                affiliate("5000.00", true), // deceased: no longer pays
                affiliate(null, false))); // legacy: no fee yet
    when(holderMapper.toDto(holder)).thenReturn(holderDto);

    final List<MembershipHolderSummaryDto> result = useCase.search(" perez ");

    assertThat(result).singleElement().satisfies(
        summary -> {
          assertThat(summary.holder()).isEqualTo(holderDto);
          assertThat(summary.activeAffiliates()).isEqualTo(2);
          assertThat(summary.monthlyTotal()).isEqualByComparingTo("5600.00");
        });
  }

  @Test
  @DisplayName("Given a known DNI when reading the holder detail then it lists every affiliate and the monthly total")
  void detail() {
    final MembershipHolderEntity holder = MembershipHolderEntity.builder().id(5L).build();
    final AffiliateEntity alive = affiliate("2000.00", false);
    final AffiliateEntity deceased = affiliate("3600.00", true);
    when(port.findByDni(20111222)).thenReturn(Optional.of(holder));
    when(port.findAffiliatesOf(5L)).thenReturn(List.of(alive, deceased));

    final MembershipHolderDetailDto detail = useCase.getByDni(20111222);

    assertThat(detail.affiliates()).hasSize(2);
    assertThat(detail.activeAffiliates()).isEqualTo(1);
    assertThat(detail.monthlyTotal()).isEqualByComparingTo("2000.00");
  }

  @Test
  @DisplayName("Given an unknown DNI when reading the holder detail then it is a 404")
  void detailNotFound() {
    when(port.findByDni(1)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> useCase.getByDni(1))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("holder.error.not.found");
  }
}
