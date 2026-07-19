package io.github.mateussilvadev.horizondesk.model;

import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusUser;
import io.github.mateussilvadev.horizondesk.support.DomainAssertions;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User entity domain rules tests")
public class UserTest implements DomainAssertions {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));

    private User user;
    private UserBuilder builder;

    @BeforeEach
    void setUp() {
        this.builder = UserBuilder.anUser();
        this.user = builder.build();
    }

    @Nested
    @DisplayName("User creation tests")
    class DepartmentUserTicketCreate {
        @Test
        @DisplayName("Should validate initial entity state match builder inputs")
        void shouldCreateUserWithValidState() {
            assertEntityState(user, Map.of(
                    "name", builder.getName(),
                    "email", builder.getEmail(),
                    "passwordHash", builder.getPasswordHash(),
                    "role", builder.getRole(),
                    "department", builder.getDepartment(),
                    "status", builder.getStatus()
            ));
            assertNotNull(user.getUuid());
        }

        @Test
        @DisplayName("Should throw exception when inputs violate core domain rules")
        void shouldThrowExceptionWhenInputsAreInvalid() {
            assertAll(
                    () -> assertThatException(
                            () -> UserBuilder.anUser().withName("Bob4").build(),
                            BusinessException.class, "user.error.invalid_name"),
                    () -> assertThatException(
                            () -> UserBuilder.anUser().withName("Bo").build(),
                            BusinessException.class, "validation.field_min_length"),
                    () -> assertThatException(
                            () -> UserBuilder.anUser().withName(null).build(),
                            BusinessException.class, "validation.field_min_length"),
                    () -> assertThatException(
                            () -> UserBuilder.anUser().withEmail("invalid-email").build(),
                            BusinessException.class, "user.error.invalid_email"),
                    () -> assertThatException(
                            () -> UserBuilder.anUser().withEmail(null).build(),
                            BusinessException.class, "validation.field_required"),
                    () -> assertThatException(
                            () -> UserBuilder.anUser().withPasswordHash("").build(),
                            BusinessException.class, "validation.field_required"));
        }
    }

    @Test
    @DisplayName("Should block email update when user account is inactive")
    void shouldBlockEmailUpdateWhenUserIsInactive() {
        user.deactivate();
        assertThatException(
                () -> user.changeEmail(faker.internet().emailAddress()),
                BusinessException.class, "user.error.inactive"
        );
    }

    @Nested
    @DisplayName("Update name workflow tests")
    class DepartmentUserUpdateName {
        @Test
        @DisplayName("Should successfully update name and normalize spacing/casing")
        void shouldUpdateNameWithValidInput() {
            String randomName = faker.name().fullName();
            String nameWithSpaces = "   " + randomName + "   ";
            assertUpdateWorkflow(user::changeName, user::getName, nameWithSpaces, randomName);
        }

        @Test
        @DisplayName("Should ignore name update when input is identical to current name")
        void shouldIgnoreUpdateWhenNameIsTheSame() {
            assertNoChange(() -> user.changeName(user.getName()), user::getName);
        }
    }

    @Nested
    @DisplayName("Update email workflow tests")
    class DepartmentUserUpdateEmail {

        @Test
        @DisplayName("Should successfully update email and normalize casing")
        void shouldUpdateEmailWithValidInput() {
            String randomEmail = faker.internet().emailAddress();
            String emailWithSpaces = "   " + randomEmail + "   ";
            assertUpdateWorkflow(user::changeEmail, user::getEmail, emailWithSpaces, randomEmail);
        }

        @Test
        @DisplayName("Should ignore email update when input is identical to current email")
        void shouldIgnoreUpdateWhenEmailIsTheSame() {
            assertNoChange(() -> user.changeEmail(user.getEmail()), user::getEmail);
        }
    }

    @Nested
    @DisplayName("Update password workflow tests")
    class DepartmentUserUpdatePassword {

        String newPasswordHash = faker.credentials().password();

        @Test
        @DisplayName("Should successfully update password hash")
        void shouldUpdatePasswordWithValidInput() {
            assertUpdateWorkflow(user::changePasswordHash, user::getPasswordHash, newPasswordHash, newPasswordHash);
        }

        @Test
        @DisplayName("Should throw exception when password creation input is null")
        void shouldThrowExceptionWhenPasswordHashIsNull() {
            assertAll(
                    () -> assertThatException(
                            () -> UserBuilder.anUser().withPasswordHash(null).build(),
                            BusinessException.class, "validation.field_required")
            );
        }
    }

    @Nested
    @DisplayName("Update department workflow tests")
    class DepartmentUserUpdateDepartment {

        @Test
        @DisplayName("Should successfully update linked department entity")
        void shouldUpdateDepartmentWithValidEntity() {
            Department newDepartmentName = Department.create("Sales Department");
            assertUpdateWorkflow(user::changeDepartment, user::getDepartment, newDepartmentName, newDepartmentName);
        }

        @Test
        @DisplayName("Should ignore department update when entity is identical to current")
        void shouldIgnoreUpdateWhenDepartmentIsTheSame() {
            assertNoChange(() -> user.changeDepartment(user.getDepartment()), user::getDepartment);
        }
    }

    @Nested
    @DisplayName("Update status workflow tests")
    class DepartmentUserUpdateStatus {

        private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 15, 10, 0);
        private User user;

        @BeforeEach
        void setUp() {
            user = UserBuilder.anUser().withStatus(StatusUser.ACTIVE).build();
        }

        @FunctionalInterface
        private interface StatusTransition {
            void execute(User user, LocalDateTime now);
        }

        private void prepareStatus(User user, StatusUser status) {
            ReflectionTestUtils.setField(user, "status", status);
        }

        private void prepareScheduledExclusionAt(User user, LocalDateTime scheduledAt) {
            ReflectionTestUtils.setField(user, "scheduledExclusionAt", scheduledAt);
        }

        private static Stream<Arguments> provideValidTransitions() {
            return Stream.of(
                    Arguments.of(StatusUser.ACTIVE, (StatusTransition) (u, now) -> u.deactivate(), StatusUser.DISABLED),
                    Arguments.of(StatusUser.ACTIVE, (StatusTransition) User::requestAccountExclusion, StatusUser.PENDING_EXCLUSION),
                    Arguments.of(StatusUser.DISABLED, (StatusTransition) (u, now) -> u.activate(), StatusUser.ACTIVE),
                    Arguments.of(StatusUser.DISABLED, (StatusTransition) User::requestAccountExclusion, StatusUser.PENDING_EXCLUSION),
                    Arguments.of(StatusUser.PENDING_EXCLUSION, (StatusTransition) (u, now) -> u.activate(), StatusUser.ACTIVE)
            );
        }

        private static Stream<Arguments> provideInvalidTransitions() {
            return Stream.of(
                    Arguments.of(StatusUser.ACTIVE, (StatusTransition) User::executeDefinitiveAnonymization),
                    Arguments.of(StatusUser.DISABLED, (StatusTransition) User::executeDefinitiveAnonymization),
                    Arguments.of(StatusUser.PENDING_EXCLUSION, (StatusTransition) (u, now) -> u.deactivate()),
                    Arguments.of(StatusUser.EXCLUDED, (StatusTransition) (u, now) -> u.activate())
            );
        }

        private static Stream<Arguments> provideSameStatusTransitions() {
            return Stream.of(
                    Arguments.of(StatusUser.ACTIVE, (StatusTransition) (u, now) -> u.activate()),
                    Arguments.of(StatusUser.DISABLED, (StatusTransition) (u, now) -> u.deactivate()),
                    Arguments.of(StatusUser.PENDING_EXCLUSION, (StatusTransition) User::requestAccountExclusion)
            );
        }

        @ParameterizedTest(name = "Scenario {index}: Transition from {0} should lead to {2}")
        @MethodSource("provideValidTransitions")
        @DisplayName("Should allow valid status transitions")
        void shouldAllowValidTransitions(StatusUser currentStatus, StatusTransition transition, StatusUser expectedStatus) {
            prepareStatus(user, currentStatus);
            transition.execute(user, NOW);
            assertThat(user.getStatus()).isEqualTo(expectedStatus);
        }

        @ParameterizedTest(name = "Scenario {index}: Transition from {0} should be blocked")
        @MethodSource("provideInvalidTransitions")
        @DisplayName("Should block invalid status transitions via User entity")
        void shouldBlockInvalidTransitions(StatusUser currentStatus, StatusTransition transition) {
            prepareStatus(user, currentStatus);

            assertThatException(
                    () -> transition.execute(user, NOW),
                    BusinessException.class, "user.error.invalid_status_transition");
        }

        @Test
        @DisplayName("Should block null status transition directly on Enum")
        void shouldBlockNullStatusTransition() {
            assertThatException(
                    () -> StatusUser.ACTIVE.transitionTo(null),
                    BusinessException.class, "user.error.status_required");
        }

        @ParameterizedTest(name = "Scenario {index}: {0} -> {1} should be blocked")
        @CsvSource({
                "ACTIVE, EXCLUDED",
                "DISABLED, EXCLUDED",
                "PENDING_EXCLUSION, DISABLED",
                "EXCLUDED, ACTIVE"
        })
        @DisplayName("Should throw exception when status transition is not allowed directly on Enum")
        void shouldThrowExceptionOnInvalidStatusTransitionEnum(StatusUser current, StatusUser invalidNext) {
            assertThatException(
                    () -> current.transitionTo(invalidNext),
                    BusinessException.class, "user.error.invalid_status_transition");
        }

        @Test
        @DisplayName("Should block definitive anonymization before scheduled exclusion date")
        void shouldBlockDefinitiveAnonymizationBeforeScheduledDate() {
            user.requestAccountExclusion(NOW);
            LocalDateTime executionDate = NOW.plusDays(6);

            assertThatException(
                    () -> user.executeDefinitiveAnonymization(executionDate),
                    BusinessException.class, "user.error.exclusion_not_due");

            assertThat(user.getStatus()).isEqualTo(StatusUser.PENDING_EXCLUSION);
            assertThat(user.getScheduledExclusionAt()).isEqualTo(NOW.plusDays(7));
        }

        @Test
        @DisplayName("Should block anonymization with date null")
        void shouldBlockAnonymizationWithDateNull() {
            prepareStatus(user, StatusUser.PENDING_EXCLUSION);
            prepareScheduledExclusionAt(user, null);

            assertThatException(
                    () -> user.executeDefinitiveAnonymization(NOW),
                    BusinessException.class, "user.error.exclusion_not_due");

            assertThat(user.getStatus()).isEqualTo(StatusUser.PENDING_EXCLUSION);
        }

        @ParameterizedTest(name = "Scenario {index}: Anonymization from {0} should be blocked")
        @ValueSource(strings = {"ACTIVE", "DISABLED"})
        @DisplayName("Should block definitive anonymization when status is not pending exclusion")
        void shouldBlockDefinitiveAnonymizationWhenStatusIsInvalid(String statusName) {
            StatusUser currentStatus = StatusUser.valueOf(statusName);
            prepareStatus(user, currentStatus);

            assertThatException(
                    () -> user.executeDefinitiveAnonymization(NOW),
                    BusinessException.class, "user.error.invalid_status_transition");

            assertThat(user.getStatus()).isEqualTo(currentStatus);
        }

        @Test
        @DisplayName("Should execute definitive anonymization when scheduled exclusion date has arrived")
        void shouldExecuteDefinitiveAnonymizationWhenScheduledDateHasArrived() {
            user.requestAccountExclusion(NOW);
            user.executeDefinitiveAnonymization(NOW.plusDays(7));

            assertThat(user.getStatus()).isEqualTo(StatusUser.EXCLUDED);
            assertThat(user.getScheduledExclusionAt()).isNull();
            assertThat(user.getName()).isEqualTo("Anonymized User");
            assertThat(user.getEmail()).startsWith("anonymized-");
        }
    }

    @Nested
    @DisplayName("Update role workflow tests")
    class DepartmentUserUpdateRole {

        private void prepareRole(User user, Role role) {
            ReflectionTestUtils.setField(user, "role", role);
        }

        private static Stream<Arguments> provideValidRoleTransitions() {
            return Stream.of(
                    Arguments.of(Role.ROLE_CUSTOMER, Role.ROLE_TECHNICIAN),
                    Arguments.of(Role.ROLE_CUSTOMER, Role.ROLE_ADMIN),
                    Arguments.of(Role.ROLE_TECHNICIAN, Role.ROLE_CUSTOMER),
                    Arguments.of(Role.ROLE_TECHNICIAN, Role.ROLE_ADMIN),
                    Arguments.of(Role.ROLE_ADMIN, Role.ROLE_CUSTOMER),
                    Arguments.of(Role.ROLE_ADMIN, Role.ROLE_TECHNICIAN)
            );
        }

        private static Stream<Role> provideSameRoleTransitions() {
            return Stream.of(Role.ROLE_CUSTOMER, Role.ROLE_TECHNICIAN, Role.ROLE_ADMIN);
        }

        @ParameterizedTest(name = "Scenario {index}: {0} -> {1}")
        @MethodSource("provideValidRoleTransitions")
        @DisplayName("Should allow transitions between different roles")
        void shouldAllowValidRoleTransitions(Role currentRole, Role newRole) {
            prepareRole(user, currentRole);
            user.changeRole(newRole);
            assertThat(user.getRole()).isEqualTo(newRole);
        }

        @ParameterizedTest(name = "Scenario {index}: {0} -> {0} should be blocked")
        @MethodSource("provideSameRoleTransitions")
        @DisplayName("Should throw exception when transitioning to the same role")
        void shouldBlockTransitionToSameRole(Role currentRole) {
            prepareRole(user, currentRole);

            assertThatException(
                    () -> user.changeRole(currentRole),
                    BusinessException.class, "user.error.invalid_role_transition");
        }

        @Test
        @DisplayName("Should block null role transition")
        void shouldBlockNullRoleTransition() {
            prepareRole(user, Role.ROLE_CUSTOMER);

            assertThatException(
                    () -> user.changeRole(null),
                    BusinessException.class, "user.error.role_required");

            assertThat(user.getRole()).isEqualTo(Role.ROLE_CUSTOMER);
        }

        @Test
        @DisplayName("Should throw exception when role transition is not allowed directly on Enum")
        void shouldThrowExceptionOnInvalidRoleTransitionEnum() {
            Role currentRole = Role.ROLE_CUSTOMER;
            assertThatException(
                    () -> currentRole.transitionTo(Role.ROLE_CUSTOMER),
                    BusinessException.class, "user.error.invalid_role_transition");
        }
    }

    @Nested
    @DisplayName("Login permission workflow based on status")
    class LogInIsValid {

        private static void prepareStatus(User user, StatusUser status) {
            try {
                java.lang.reflect.Field field = User.class.getDeclaredField("status");
                field.setAccessible(true);
                field.set(user, status);
            } catch (Exception e) {
                throw new RuntimeException("Failed to prepare initial user status for testing", e);
            }
        }

        private static Stream<Arguments> provideStatusLoginScenarios() {
            return Stream.of(
                    Arguments.of(StatusUser.ACTIVE, true, null),
                    Arguments.of(StatusUser.DISABLED, false, "user.error.inactive"),
                    Arguments.of(StatusUser.PENDING_EXCLUSION, false, "user.error.inactive"),
                    Arguments.of(StatusUser.EXCLUDED, false, "user.error.inactive")
            );
        }

        @ParameterizedTest(name = "Scenario {index}: Status {0} should allow actions? {1}")
        @MethodSource("provideStatusLoginScenarios")
        @DisplayName("Should validate core actions permissions based on account status")
        void shouldValidateStatusPermissions(StatusUser status, boolean shouldSucceed, String expectedMessageKey) {
            prepareStatus(user, status);
            if (shouldSucceed)
                org.assertj.core.api.Assertions.assertThatCode(() -> user.ensureCanLogIn()).doesNotThrowAnyException();
            else
                assertThatException(() -> user.ensureCanLogIn(), BusinessException.class, expectedMessageKey);
        }
    }
}