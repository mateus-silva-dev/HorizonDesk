package io.github.mateussilvadev.horizondesk.model;

import io.github.mateussilvadev.horizondesk.builder.DepartmentBuilder;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.support.DomainAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Department entity domain rules tests")
public class DepartmentTest implements DomainAssertions {

    private Department department;
    private DepartmentBuilder builder;

    @BeforeEach
    void setUp() {
        this.builder = DepartmentBuilder.anDepartment();
        this.department = builder.build();
    }

    @Nested
    @DisplayName("Department creation tests")
    class Create {

        @Test
        @DisplayName("Should validate initial entity state match builder inputs")
        void shouldCreateDepartmentWithValidState() {
            assertEntityState(department, Map.of(
                    "name", builder.getName(),
                    "active", builder.isActive()
            ));
            assertNotNull(department.getUuid());
        }

        @Test
        @DisplayName("Should throw exception when inputs violate core domain rules")
        void shouldThrowExceptionWhenInputsAreInvalid() {
            assertAll(
                    () -> assertThatException(
                            () -> DepartmentBuilder.anDepartment().withName("TI4").build(),
                            BusinessException.class, "department.error.invalid_name"),
                    () -> assertThatException(
                            () -> DepartmentBuilder.anDepartment().withName("T").build(),
                            BusinessException.class, "validation.field_min_length"),
                    () -> assertThatException(
                            () -> DepartmentBuilder.anDepartment().withName(null).build(),
                            BusinessException.class, "validation.field_min_length")
            );
        }
    }

    @Nested
    @DisplayName("Update name workflow tests")
    class UpdateName {

        @Test
        @DisplayName("Should successfully update name and normalize spacing/casing")
        void shouldUpdateNameWithValidInput() {
            String randomName = "Sales Department";
            String nameWithSpaces = "   Sales   Department   ";
            assertUpdateWorkflow(department::changeDepartmentName, department::getName, nameWithSpaces, randomName);
        }

        @Test
        @DisplayName("Should ignore name update when input is identical to current name")
        void shouldIgnoreUpdateWhenNameIsTheSame() {
            assertNoChange(() -> department.changeDepartmentName(department.getName()), department::getName);
        }

        @Test
        @DisplayName("Should block name update when department is inactive")
        void shouldBlockNameUpdateWhenDepartmentIsInactive() {
            department.deactivate();
            assertThatException(
                    () -> department.changeDepartmentName("New Department Name"),
                    BusinessException.class, "department.error.inactive"
            );
        }
    }

    @Nested
    @DisplayName("Department activation and deactivation tests")
    class ActivationLifecycle {

        @Test
        @DisplayName("Should successfully activate an inactive department")
        void shouldActivateDepartment() {
            department.deactivate();
            department.activate();
            assertTrue(department.isActive());
        }

        @Test
        @DisplayName("Should maintain idempotency when activating an already active department")
        void shouldMaintainIdempotencyOnActivation() {
            department.deactivate();
            department.activate();
            assertIdempotent(department::activate, department::isActive);
        }

        @Test
        @DisplayName("Should successfully deactivate an active department")
        void shouldDeactivateDepartment() {
            department.deactivate();
            assertFalse(department.isActive());
        }

        @Test
        @DisplayName("Should maintain idempotency when deactivating an already inactive department")
        void shouldMaintainIdempotencyOnDeactivation() {
            department.deactivate();
            assertIdempotent(department::deactivate, department::isActive);
        }
    }
}
