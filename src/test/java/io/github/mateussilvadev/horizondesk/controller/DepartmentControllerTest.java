package io.github.mateussilvadev.horizondesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateussilvadev.horizondesk.builder.DepartmentBuilder;
import io.github.mateussilvadev.horizondesk.dto.request.DepartmentRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.service.DepartmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("DepartmentController test")
@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTest {

    private static final String BASE_URL = "/api/v1/departments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageSource messageSource;

    @MockitoBean
    private DepartmentService service;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID uuid = UUID.randomUUID();
    private final Department mockDepartment = Department.create("TI");

    @Test
    @DisplayName("Should return 500 when an unexpected error occurs")
    void shouldReturn500OnGenericException() throws Exception {
        UUID randomUuid = UUID.randomUUID();

        given(service.findByUuid(any(UUID.class)))
                .willThrow(new RuntimeException("error.internal_server_error"));

        mockMvc.perform(get(BASE_URL + "/{uuid}", randomUuid))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(Code.INTERNAL_SERVER_ERROR.toString()));
    }

    @Test
    @DisplayName("Should return 400 for general business rules")
    void shouldReturn400ForGeneralBusinessException() throws Exception {
        var request = new DepartmentRequestDTOs.DepartmentCreate("Compliance");

        given(service.create(any(DepartmentRequestDTOs.DepartmentCreate.class))).willThrow(
                new BusinessException(Code.MALFORMED_JSON, "error.invalid_json"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(Code.MALFORMED_JSON.toString()));
    }

    @Test
    @DisplayName("Should return 422 when any name is invalid.")
    void shouldReturn422WhenAnyNameIsInvalid() throws Exception {
        var request = new DepartmentRequestDTOs.DepartmentCreate("A");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value(Code.VALIDATION_ERROR.toString()));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Should return 404 when department does not exist")
    void shouldReturn404WhenDepartmentDoesNotExist() throws Exception {
        UUID randomUuid = UUID.randomUUID();
        given(service.findByUuid(randomUuid))
                .willThrow(new EntityNotFoundException("user_service.error.user_not_found"));

        mockMvc.perform(get(BASE_URL + "/{uuid}", randomUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(Code.ENTITY_NOT_FOUND.toString()));
    }

    @Nested
    @DisplayName("Create department")
    class DepartmentCreate {
        @Test
        @DisplayName("Should create department and return 201")
        void shouldCreateUser() throws Exception {
            var request = new DepartmentRequestDTOs.DepartmentCreate("Compliance");

            var department = DepartmentBuilder.anDepartment()
                    .withName(request.name())
                    .build();

            given(service.create(any())).willReturn(department);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.uuid").value(department.getUuid().toString()))
                    .andExpect(jsonPath("$.name").value(department.getName()))
                    .andExpect(jsonPath("$.active").value(department.isActive()));

        }

        @Test
        @DisplayName("Should return 409 when name already registered")
        void shouldReturn409WhenNameExists() throws Exception {
            var request = new DepartmentRequestDTOs.DepartmentCreate("Compliance");

            String dbMockedMessage = "ERROR: duplicate key value violates unique constraint \"ukj6cwks7xecs5jov19ro8ge3qk\"";

            given(service.create(any()))
                    .willThrow(new DataIntegrityViolationException(dbMockedMessage));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(Code.DEPARTMENT_ALREADY_REGISTERED.toString()));
        }
    }

    @Test
    @DisplayName("Should get department by UUID and return 200")
    void shouldGetDepartmentByUuid() throws Exception {
        var department = DepartmentBuilder.anDepartment().withUuid(uuid).build();
        given(service.findByUuid(uuid)).willReturn(department);

        mockMvc.perform(get(BASE_URL + "/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(department.getUuid().toString()));
    }

    @Test
    @DisplayName("Should update department and return 200")
    void shouldUpdateDepartment() throws Exception {
        var dto = new DepartmentRequestDTOs.DepartmentUpdate("New Name");
        var department = DepartmentBuilder.anDepartment().withName("New Name").build();

        given(service.update(eq(uuid), any())).willReturn(department);

        mockMvc.perform(patch(BASE_URL + "/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"activate", "deactivate"})
    @DisplayName("Should return 204 when changing department status")
    void shouldReturn204WhenChangingStatus(String action) throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{uuid}/" + action, uuid))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 200 and paged departments")
    void shouldReturnPagedDepartments() throws Exception {
        Page<Department> page = new PageImpl<>(List.of(DepartmentBuilder.anDepartment().build()));
        given(service.findAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should return 200 and options departments")
    void shouldReturnOptionsDepartments() throws Exception {
        List<Department> list = List.of(DepartmentBuilder.anDepartment().build());
        given(service.findAllActiveOptions()).willReturn(list);

        mockMvc.perform(get(BASE_URL + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").exists());
    }

}
