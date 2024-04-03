package disenodesistemas.backendfunerariaapp.cucumberjunit.bdd.steps;

import disenodesistemas.backendfunerariaapp.cucumberjunit.bdd.CucumberSpringConfiguration;
import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignupSteps extends CucumberSpringConfiguration {

    private ResponseEntity<UserResponseDto> response;
    private UserRegisterDto userRegisterDto;

    @Given("an email not registered and a password {string}")
    public void adminUserWantsToLogin(String arg0) {
        userRegisterDto = UserRegisterDto.builder()
                .password(arg0)
                .email("validEmail@gmail.com")
                .addresses(List.of())
                .roles(Set.of("ROLE_USER"))
                .firstName("John")
                .lastName("Doe")
                .mobileNumbers(List.of())
                .matchingPassword(arg0)
                .build();
    }

    @When("the user tries to signup")
    public void theUserTriesToLoginAsAdmin() {
        response = testRestTemplate.postForEntity(
                "/api/v1/users", userRegisterDto,
                UserResponseDto.class);
    }

    @Then("the user registers successfully")
    public void theUserIsAllowedToUseTheApp() {
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        //assertTrue(Objects.requireNonNull(response.getBody()).getRoles()..contains("ROLE_ADMIN"));
    }
}
