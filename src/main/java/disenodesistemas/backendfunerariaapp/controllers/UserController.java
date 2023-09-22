package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.LogOutRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetByEmailDto;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.dto.request.RolRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.dto.response.MobileNumberResponseDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserAddressAndPhoneDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.service.EmailService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final ProjectionFactory projectionFactory;

    @GetMapping(path = "/me")
    public UserResponseDto getUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return projectionFactory.createProjection(UserResponseDto.class, userService.getUserByEmail(email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid final UserRegisterDto userRegisterDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRegisterDto));
    }

    @PostMapping(path = "/login")
    public ResponseEntity<JwtDto> login(@Valid @RequestBody final UserLoginDto userLoginDto) {
        return ResponseEntity.ok(userService.login(userLoginDto));
    }

    @GetMapping(path = "/activation")
    public String confirmation(@RequestParam("token") final String token) {
        return userService.confirmationUser(token);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping("/logout")
    public ResponseEntity<OperationStatusModel> logoutUser(@Valid @RequestBody final LogOutRequestDto logOutRequest) {
        return ResponseEntity.ok(userService.logoutUser(logOutRequest));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refreshJwtToken(@Valid @RequestBody final TokenRefreshRequestDto tokenRefreshRequest) {
        return ResponseEntity.ok(userService.refreshJwtToken(tokenRefreshRequest));
    }

    @PostMapping(path = "/forgot-password")
    public String forgotUserPassword(@RequestParam(value = "email") final String email) {
        return emailService.sendForgotPassword(email);
    }

    @PostMapping(path = "/reset-password-by-email")
    public String resetUserPasswordByEmail(@Valid @RequestBody final PasswordResetByEmailDto passwordResetDto,
                                    @RequestParam("token") final String token) {
        return userService.resetUserPasswordByEmail(passwordResetDto, token);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(path = "/reset-password")
    public ResponseEntity<Map<String, String>> resetUserPassword(@Valid @RequestBody final PasswordResetDto passwordResetDto) {
        return ResponseEntity.ok(userService.changeOldPassword(passwordResetDto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(path = "/addresses-and-phones")
    public ResponseEntity<UserAddressAndPhoneDto> getUserAddressesAndMobileNumbers() {
        return ResponseEntity.ok(userService.getUserAddressesAndMobileNumbers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/rol/{email}")
    public Set<RolRequestDto> updateUserRol(@PathVariable final String email, @Valid @RequestBody final RolRequestDto rolRequestDto) {
        return userService.updateUserRol(email, rolRequestDto);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(path = "/address")
    public List<AddressResponseDto> addUserAddresses(@RequestBody final List<@Valid AddressRequestDto> addressesRequest) {
        return userService.addAddressesUser(addressesRequest);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(path = "/mobile-numbers")
    public ResponseEntity<List<MobileNumberResponseDto>> addUserMobileNumbers(@RequestBody final List<@Valid MobileNumberRequestDto> mobileNumbersRequest) {
        return ResponseEntity.ok(userService.addMobileNumbersUser(mobileNumbersRequest));
    }
}
