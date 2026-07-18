package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.builder.DepartmentBuilder;
import io.github.mateussilvadev.horizondesk.dto.request.DepartmentCreateDTO;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static io.github.mateussilvadev.horizondesk.exception.Code.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Department service test")
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repository;

    @InjectMocks
    private DepartmentService service;
    private DepartmentCreateDTO createDTO;
    private final UUID uuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createDTO = new DepartmentCreateDTO("Technical Support");
    }

    private Department mockDepartmentFound(UUID uuid) {
        var department = DepartmentBuilder.anDepartment().withUuid(uuid).build();
        given(repository.findByUuid(uuid)).willReturn(Optional.of(department));
        return department;
    }

    @Nested
    @DisplayName("Department creation test")
    class Create {

        @Test
        @DisplayName("Create should save department")
        void shouldSaveDepartment() {
            given(repository.existsByName(createDTO.name())).willReturn(false);
            given(repository.save(any())).willAnswer(i -> i.getArgument(0));

            var answer = service.create(createDTO);

            assertThat(answer).isNotNull();
            assertThat(answer).extracting("name").isEqualTo(createDTO.name());

            verify(repository).save(answer);
        }

        @Test
        @DisplayName("An exception should be thrown when the department name is already registered")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            given(repository.existsByName(createDTO.name())).willReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> service.create(createDTO));

            assertThat(ex.getCode()).isEqualTo(DEPARTMENT_ALREADY_REGISTERED);
            assertThat(ex.getMessage()).isEqualTo("department_service.error.department_already_registered");

            verify(repository).existsByName(createDTO.name());
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Search test for department")
    class Find {

        @Test
        @DisplayName("Search by UUID")
        void byUUID() {
            var department = mockDepartmentFound(uuid);

            var answer = service.findByUuid(uuid);

            assertThat(answer).isNotNull();
            assertThat(answer.getUuid()).isEqualTo(department.getUuid());

            verify(repository).findByUuid(uuid);
        }

        @Test
        @DisplayName("An exception should be thrown when the department is not found")
        void shouldThrowExceptionWhenUserNotFound() {
            UUID uuid = UUID.randomUUID();
            given(repository.findByUuid(uuid)).willReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.findByUuid(uuid));

            assertThat(ex.getCode()).isEqualTo(ENTITY_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("department_service.error.department_not_found");

            verify(repository).findByUuid(uuid);
        }

    }

}
