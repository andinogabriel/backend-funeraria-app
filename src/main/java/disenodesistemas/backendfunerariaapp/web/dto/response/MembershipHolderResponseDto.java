package disenodesistemas.backendfunerariaapp.web.dto.response;

public record MembershipHolderResponseDto(
    Long id, Integer dni, String firstName, String lastName, String phone, String email) {}
