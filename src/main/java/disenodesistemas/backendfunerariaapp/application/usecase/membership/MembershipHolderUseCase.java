package disenodesistemas.backendfunerariaapp.application.usecase.membership;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipHolderPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.mapping.MembershipHolderMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipHolderRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderDetailDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderSummaryDto;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Membership holders ("titulares"): resolution at enrolment time plus the read side (search and
 * detail with the monthly total the holder pays).
 */
@Service
@RequiredArgsConstructor
public class MembershipHolderUseCase {

  static final int SEARCH_LIMIT = 20;

  private final MembershipHolderPersistencePort port;
  private final MembershipHolderMapper holderMapper;
  private final AffiliateMapper affiliateMapper;
  private final Clock clock;

  /**
   * Returns the holder identified by the request DNI, creating it when it does not exist. An
   * existing holder is reused and its name and contact data refreshed with the request (the desk
   * re-types them on every enrolment, so the latest value wins; blank contact fields do not erase
   * stored ones).
   *
   * <p>Only an admin, the system (no actor) or the user linked to the holder may refresh an
   * existing holder's data; any other self-service user reuses the holder as stored, so typing
   * someone else's DNI cannot overwrite their name or contact data. When a self-service ROLE_USER
   * creates a new holder, their account is linked to it: they are enrolling their own family.
   */
  @Transactional
  public MembershipHolderEntity resolveForEnrolment(
      final MembershipHolderRequestDto request, final UserEntity actor) {
    final MembershipHolderEntity holder =
        port.findByDni(request.dni())
            .orElseGet(
                () ->
                    MembershipHolderEntity.builder()
                        .dni(request.dni())
                        .createdAt(Instant.now(clock))
                        .build());
    final boolean isNew = holder.getId() == null;
    if (!isNew && !mayRefresh(holder, actor)) {
      return holder;
    }
    holder.setFirstName(request.firstName().trim());
    holder.setLastName(request.lastName().trim());
    if (hasText(request.phone())) {
      holder.setPhone(request.phone().trim());
    }
    if (hasText(request.email())) {
      holder.setEmail(request.email().trim());
    }
    if (isNew && actor != null && !isAdmin(actor)) {
      holder.setUser(actor);
    }
    return port.save(holder);
  }

  @Transactional(readOnly = true)
  public List<MembershipHolderSummaryDto> search(final String value) {
    if (!hasText(value)) {
      return List.of();
    }
    return port.search(value.trim(), SEARCH_LIMIT).stream()
        .map(
            holder -> {
              final List<AffiliateEntity> affiliates = port.findAffiliatesOf(holder.getId());
              return new MembershipHolderSummaryDto(
                  holderMapper.toDto(holder), countPayable(affiliates), monthlyTotal(affiliates));
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public MembershipHolderDetailDto getByDni(final Integer dni) {
    final MembershipHolderEntity holder =
        port.findByDni(dni).orElseThrow(() -> new NotFoundException("holder.error.not.found"));
    final List<AffiliateEntity> affiliates = port.findAffiliatesOf(holder.getId());
    return new MembershipHolderDetailDto(
        holderMapper.toDto(holder),
        countPayable(affiliates),
        monthlyTotal(affiliates),
        affiliates.stream().map(affiliateMapper::toDto).toList());
  }

  private static boolean mayRefresh(final MembershipHolderEntity holder, final UserEntity actor) {
    if (actor == null || isAdmin(actor)) {
      return true;
    }
    return holder.getUser() != null && Objects.equals(holder.getUser().getId(), actor.getId());
  }

  /** Alive and priced: the affiliates that generate a monthly fee. */
  private static boolean isPayable(final AffiliateEntity affiliate) {
    return !Boolean.TRUE.equals(affiliate.getDeceased()) && affiliate.getMonthlyFee() != null;
  }

  private static int countPayable(final List<AffiliateEntity> affiliates) {
    return (int) affiliates.stream().filter(MembershipHolderUseCase::isPayable).count();
  }

  private static BigDecimal monthlyTotal(final List<AffiliateEntity> affiliates) {
    return affiliates.stream()
        .filter(MembershipHolderUseCase::isPayable)
        .map(AffiliateEntity::getMonthlyFee)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static boolean isAdmin(final UserEntity user) {
    return user.getRoles() != null
        && user.getRoles().stream().anyMatch(role -> role.getName() == Role.ROLE_ADMIN);
  }

  private static boolean hasText(final String value) {
    return value != null && !value.isBlank();
  }
}
