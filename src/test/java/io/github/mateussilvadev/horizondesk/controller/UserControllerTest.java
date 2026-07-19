package io.github.mateussilvadev.horizondesk.controller;

import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.dto.request.UserRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.service.UserService;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("userController test")
public class UserControllerTest {

    private static final Faker FAKER = new Faker(Locale.of("pt", "BR"));
    private static final String BASE_URL = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MessageSource messageSource;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID uuid = UUID.randomUUID();
    private final Department mockDepartment = Department.create("TI");


    @Test
    @DisplayName("Should return 500 when an unexpected error occurs")
    void shouldReturn500OnGenericException() throws Exception {
        UUID randomUuid = UUID.randomUUID();

        given(userService.findByUuid(any(UUID.class)))
                .willThrow(new RuntimeException("error.internal_server_error"));

        mockMvc.perform(get(BASE_URL + "/{uuid}", randomUuid))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(Code.INTERNAL_SERVER_ERROR.toString()));
    }

    @Test
    @DisplayName("Should return 400 for general business rules")
    void shouldReturn400ForGeneralBusinessException() throws Exception {
        var request = new UserRequestDTOs.UserCreate("John Doe", "john@email.com", "password123", UUID.randomUUID());

        given(userService.create(any(UserRequestDTOs.UserCreate.class))).willThrow(
                new BusinessException(Code.MALFORMED_JSON, "error.invalid_json"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(Code.MALFORMED_JSON.toString()));
    }

    @Test
    @DisplayName("Should return 422 when any data is invalid.")
    void shouldReturn422WhenAnyDataIsInvalid() throws Exception {
        var request = new UserRequestDTOs.UserCreate("John Doe", "email-invalid", "password123", UUID.randomUUID());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value(Code.VALIDATION_ERROR.toString()));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 404 when UUID does not exist")
    void shouldReturn404WhenEntityNotFound() throws Exception {
        UUID randomUuid = UUID.randomUUID();
        given(userService.findByUuid(randomUuid))
                .willThrow(new EntityNotFoundException("user_service.error.user_not_found"));

        mockMvc.perform(get(BASE_URL + "/{uuid}", randomUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(Code.ENTITY_NOT_FOUND.toString()));
    }

    @Nested
    @DisplayName("Create user")
    class DepartmentUserTicketCreate {
        @Test
        @DisplayName("Should create user and return 201")
        void shouldCreateUser() throws Exception {
            var request = new UserRequestDTOs.UserCreate(
                    FAKER.name().fullName(),
                    FAKER.internet().emailAddress(),
                    FAKER.credentials().password(8, 20),
                    UUID.fromString("b85a4dd5-0866-40d2-9688-c5ba16ce9b5e"));

            var user = UserBuilder.anUser()
                    .withName(request.name())
                    .withEmail(request.email())
                    .withPasswordHash(request.password())
                    .withDepartment(mockDepartment)
                    .build();

            given(userService.create(any())).willReturn(user);

            ResultActions actions = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            assertUserResponse(actions, user);
        }

        @Test
        @DisplayName("Should return 409 when email already registered")
        void shouldReturn409WhenEmailExists() throws Exception {
            var request = new UserRequestDTOs.UserCreate(FAKER.name().fullName(), "email@test.com",
                    FAKER.credentials().password(8, 20), UUID.fromString("b85a4dd5-0866-40d2-9688-c5ba16ce9b5e"));

            String dbMockedMessage = "ERROR: duplicate key value violates unique constraint \"uk6dotkott2kjsp8vw4d0m25fb7\"";

            given(userService.create(any()))
                    .willThrow(new DataIntegrityViolationException(dbMockedMessage));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(Code.EMAIL_ALREADY_REGISTERED.toString()));
        }
    }

    @Test
    @DisplayName("Should get user by UUID and return 200")
    void shouldGetUserByUuid() throws Exception {
        var user = UserBuilder.anUser().withUuid(uuid).build();
        given(userService.findByUuid(uuid)).willReturn(user);

        mockMvc.perform(get(BASE_URL + "/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(user.getUuid().toString()));
    }

    @Test
    @DisplayName("Should update user and return 200")
    void shouldUpdateUser() throws Exception {
        var dto = new UserRequestDTOs.UserUpdate("New Name", null);
        var user = UserBuilder.anUser().withName("New Name").build();

        given(userService.updateUser(eq(uuid), any())).willReturn(user);

        mockMvc.perform(patch(BASE_URL + "/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    @DisplayName("Should update password and return 204")
    void shouldUpdatePassword() throws Exception {
        var dto = new UserRequestDTOs.UpdatePassword("12345678", "senha@123", "senha@123");

        mockMvc.perform(patch(BASE_URL + "/{uuid}/password", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"activate", "deactivate", "exclusion-request"})
    @DisplayName("Should return 204 when changing user status")
    void shouldReturn204WhenChangingStatus(String action) throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{uuid}/" + action, uuid))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should change user role and return 204")
    void shouldChangeRole() throws Exception {
        var dto = new UserRequestDTOs.ChangeRole(Role.ROLE_ADMIN);

        mockMvc.perform(patch(BASE_URL + "/{uuid}/role", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should change user department and return 204")
    void shouldChangeDepartment() throws Exception {
        var dto = new UserRequestDTOs.ChangeDepartment(UUID.randomUUID());

        mockMvc.perform(patch(BASE_URL + "/{uuid}/department", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 200 and paged users")
    void shouldReturnPagedUsers() throws Exception {
        Page<User> page = new PageImpl<>(List.of(UserBuilder.anUser().build()));
        given(userService.findAllActiveTechnicians(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get(BASE_URL + "/technicians")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private void assertUserResponse(ResultActions actions, User user) throws Exception {
        actions
                .andExpect(jsonPath("$.uuid").value(user.getUuid().toString()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.department").value(mockDepartment.getName()))
                .andExpect(jsonPath("$.status").value(user.getStatus().name()));
    }
}
























































