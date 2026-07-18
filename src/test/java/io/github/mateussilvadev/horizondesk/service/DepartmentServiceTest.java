package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.builder.DepartmentBuilder;
import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.dto.request.DepartmentRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusUser;
import io.github.mateussilvadev.horizondesk.repository.DepartmentRepository;
import io.github.mateussilvadev.horizondesk.support.DomainAssertions;
import org.awaitility.core.DeadlockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.mateussilvadev.horizondesk.exception.Code.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("Department service test")
@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest implements DomainAssertions {

    @Mock
    private DepartmentRepository repository;

    @InjectMocks
    private DepartmentService service;
    private DepartmentRequestDTOs.Create createDTO;
    private final UUID uuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createDTO = new DepartmentRequestDTOs.Create("Technical Support");
        service = new DepartmentService(repository);
    }

    private Department mockDepartmentFound(UUID uuid) {
        var department = DepartmentBuilder.anDepartment().withName("Technical Support").withUuid(uuid).build();
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
            assertThat(answer)
                    .extracting("name")
                    .isEqualTo(createDTO.name());

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
    @DisplayName("Department must update successfully")
    class Update {
        @Test
        @DisplayName("Department must update successfully")
        void shouldUpdateDepartment() {
            Department department = mockDepartmentFound(uuid);
            DepartmentRequestDTOs.Update updateDTO = new DepartmentRequestDTOs.Update("Sales Department");

            var answer = service.update(uuid, updateDTO.name());

            assertThat(answer).isNotNull();
            assertThat(answer.getUuid()).isEqualTo(department.getUuid());
            assertThat(answer.getName()).isEqualTo(updateDTO.name());
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

        @Test
        @DisplayName("Should return page of departments successfully")
        void shouldReturnPageOfDepartments() {
            Pageable pageable = PageRequest.of(0, 10);
            Department department = DepartmentBuilder.anDepartment().build();

            Page<Department> mockPage = new PageImpl<>(List.of(department), pageable, 1);

            when(repository.findAll(any(Pageable.class))).thenReturn(mockPage);

            Page<Department> result = service.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().isActive()).isEqualTo(true);

            verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return list of active departments successfully")
        void shouldReturnListOfActiveDepartments() {
            Department department = DepartmentBuilder.anDepartment().build();
            List<Department> departments = List.of(department);

            when(repository.findAllByActiveTrueOrderByNameAsc()).thenReturn(departments);

            List<Department> result = service.findAllActiveOptions();

            assertThat(result).isNotEmpty();
            assertThat(departments.getFirst().isActive()).isTrue();
        }

    }

    @Nested
    @DisplayName("Must change department status")
    class ChangeStatus {

        @Test
        @DisplayName("Should activate department successfully")
        void shouldActivateDepartment() {
            Department department = DepartmentBuilder.anDepartment().withUuid(uuid).withActive(false).build();
            given(repository.findByUuid(uuid)).willReturn(Optional.of(department));

            service.activate(uuid);

            assertThat(department.isActive()).isEqualTo(true);
        }


    }

}
