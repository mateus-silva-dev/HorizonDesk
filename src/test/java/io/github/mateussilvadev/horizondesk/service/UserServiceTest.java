package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.builder.UserBuilder;
import io.github.mateussilvadev.horizondesk.dto.request.UserRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusUser;
import io.github.mateussilvadev.horizondesk.repository.DepartmentRepository;
import io.github.mateussilvadev.horizondesk.repository.UserRepository;
import io.github.mateussilvadev.horizondesk.support.DomainAssertions;
import net.datafaker.Faker;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static io.github.mateussilvadev.horizondesk.exception.Code.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("User service test")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest implements DomainAssertions {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));
    private final LocalDateTime FIXED_DATE = LocalDateTime.of(2026, Month.JULY, 16, 10, 0);

    @Mock
    private UserRepository repository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService service;

    @InjectMocks
    private UserRequestDTOs.Create createDTO;
    private final UUID uuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                FIXED_DATE.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        createDTO = new UserRequestDTOs.Create(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                faker.credentials().password(8, 20),
                UUID.fromString("b85a4dd5-0866-40d2-9688-c5ba16ce9b5e")
        );

        service = new UserService(repository, passwordEncoder, departmentRepository, fixedClock);
    }

    private User mockUserFound(UUID uuid) {
        var user = UserBuilder.anUser().withUuid(uuid).build();
        given(repository.findByUuid(uuid)).willReturn(Optional.of(user));
        return user;
    }

    @Nested
    @DisplayName("User creation test")
    class Create {

        @Test
        @DisplayName("Create should save user")
        void shouldSaveUser() {
            Department mockDepartment = Department.create("Sales Department");

            given(repository.existsByEmail(createDTO.email())).willReturn(false);
            given(departmentRepository.findByUuid(createDTO.departmentUuid()))
                    .willReturn(Optional.of(mockDepartment));
            given(passwordEncoder.encode(createDTO.password())).willReturn(createDTO.password());
            given(repository.save(any())).willAnswer(i -> i.getArgument(0));

            var answer = service.create(createDTO);

            assertThat(answer).isNotNull();
            assertThat(answer)
                    .extracting("name", "email", "passwordHash", "department")
                    .containsExactly(createDTO.name(), createDTO.email(), createDTO.password(), mockDepartment);

            verify(repository).save(answer);
        }

        @Test
        @DisplayName("An exception should be thrown when the email address is already registered")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            given(repository.existsByEmail(createDTO.email())).willReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> service.create(createDTO));

            assertThat(ex.getCode()).isEqualTo(EMAIL_ALREADY_REGISTERED);
            assertThat(ex.getMessage()).isEqualTo("user_service.error.email_already_registered");

            verify(repository).existsByEmail(createDTO.email());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("An exception should be thrown when the department don't exists")
        void shouldThrowExceptionWhenDepartmentDoesNotExist() {
            given(repository.existsByEmail(createDTO.email())).willReturn(false);
            given(departmentRepository.findByUuid(createDTO.departmentUuid())).willReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.create(createDTO));

            assertThat(ex.getCode()).isEqualTo(ENTITY_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("user_service.error.department_not_found");

            verify(repository).existsByEmail(createDTO.email());
            verify(departmentRepository).findByUuid(createDTO.departmentUuid());
            verify(repository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("Search test for users")
    class Find {

        @Test
        @DisplayName("Search by UUID")
        void byUUID() {
            var user = mockUserFound(uuid);

            var answer = service.findByUuid(uuid);

            assertThat(answer).isNotNull();
            assertThat(answer.getUuid()).isEqualTo(user.getUuid());

            verify(repository).findByUuid(uuid);
        }

        @Test
        @DisplayName("An exception should be thrown when the user is not found")
        void shouldThrowExceptionWhenUserNotFound() {
            UUID uuid = UUID.randomUUID();
            given(repository.findByUuid(uuid)).willReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.findByUuid(uuid));

            assertThat(ex.getCode()).isEqualTo(ENTITY_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("user_service.error.user_not_found");

            verify(repository).findByUuid(uuid);
        }

    }

    @Nested
    @DisplayName("Update test for users")
    class Update {

        @Test
        @DisplayName("User must update successfully")
        void shouldUpdateUser() {
            User user = mockUserFound(uuid);
            UserRequestDTOs.Update updateDTO = new UserRequestDTOs.Update(faker.name().fullName(), faker.internet().emailAddress());

            var answer = service.updateUser(uuid, updateDTO);

            assertThat(answer).isNotNull();
            assertThat(answer.getName()).isEqualTo(updateDTO.name());
            assertThat(answer.getEmail()).isEqualTo(updateDTO.email());
        }

        @Test
        @DisplayName("Should update only the name when other fields are null")
        void shouldUpdateOnlyName() {
            User user = mockUserFound(uuid);
            String originalEmail = user.getEmail();
            UserRequestDTOs.Update updateDTO = new UserRequestDTOs.Update(faker.name().fullName(), null);

            var answer = service.updateUser(uuid, updateDTO);

            assertThat(answer.getName()).isEqualTo(updateDTO.name());
            assertThat(answer.getEmail()).isEqualTo(originalEmail);
        }

        @Test
        @DisplayName("Should update only the email when other fields are null")
        void shouldUpdateOnlyEmail() {
            User user = mockUserFound(uuid);
            String originalName = user.getName();
            UserRequestDTOs.Update updateDTO = new UserRequestDTOs.Update(null, faker.internet().emailAddress());

            var answer = service.updateUser(uuid, updateDTO);

            assertThat(answer.getName()).isEqualTo(originalName);
            assertThat(answer.getEmail()).isEqualTo(updateDTO.email());
        }
    }

    @Nested
    @DisplayName("update password test for users")
    class UpdatePassword {

        private UserRequestDTOs.UpdatePassword userUpdatePasswordDTO;
        private String newPassword;

        @BeforeEach
        void setUpNested() {
            newPassword = faker.credentials().password(8,20);
            userUpdatePasswordDTO = new UserRequestDTOs.UpdatePassword("oldPassword", newPassword, newPassword);
        }

        @Test
        @DisplayName("User must update password successfully")
        void shouldUpdatePassword() {
            User user = mockUserFound(uuid);

            given(passwordEncoder.matches(userUpdatePasswordDTO.currentPassword(), user.getPasswordHash())).willReturn(true);
            given(passwordEncoder.matches(userUpdatePasswordDTO.newPassword(), user.getPasswordHash())).willReturn(false);
            given(passwordEncoder.encode(userUpdatePasswordDTO.newPassword())).willReturn("PasswordEncrypted");

            service.changePassword(uuid, userUpdatePasswordDTO);

            assertEquals("PasswordEncrypted", user.getPasswordHash());
            verify(passwordEncoder, times(1)).encode(userUpdatePasswordDTO.newPassword());
        }

        @Test
        @DisplayName("Should throw BusinessException when current password is incorrect")
        void shouldThrowBusinessExceptionWhenCurrentPasswordIsIncorrect() {
            User user = mockUserFound(uuid);

            given(passwordEncoder.matches(userUpdatePasswordDTO.currentPassword(), user.getPasswordHash())).willReturn(false);

            assertThatException(
                    () -> service.changePassword(uuid, userUpdatePasswordDTO),
                    BusinessException.class, "user_service.error.invalid_current_password");

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when new password is equals current password")
        void shouldThrowBusinessExceptionWhenNewPasswordIsEqualsCurrentPassword() {
            User user = mockUserFound(uuid);

            given(passwordEncoder.matches(userUpdatePasswordDTO.currentPassword(), user.getPasswordHash())).willReturn(true);
            given(passwordEncoder.matches(userUpdatePasswordDTO.newPassword(), user.getPasswordHash())).willReturn(true);

            assertThatException(
                    () -> service.changePassword(uuid, userUpdatePasswordDTO),
                    BusinessException.class, "user_service.error.new_password_same_as_current");

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when passwords don't match")
        void shouldThrowBusinessExceptionWhenPasswordsDontMatch() {
            User user = mockUserFound(uuid);

            String currentPassword = "CurrentPassword123!";
            String newPass = "NewPassword123!";
            String confirmPass = "CompletelyDifferentPassword456!";

            UserRequestDTOs.UpdatePassword dto = new UserRequestDTOs.UpdatePassword(currentPassword, newPass, confirmPass);

            assertThatException(
                    () -> service.changePassword(uuid, dto),
                    BusinessException.class, "user_service.error.passwords_dont_match");

            verify(passwordEncoder, never()).encode(any());
        }
    }

    @Nested
    @DisplayName("Change role test for users")
    class ChangeRole {

        @Test
        @DisplayName("Should update the role successfully")
        void shouldUpdateRole() {
            User user = mockUserFound(uuid);
            service.changeRole(uuid, Role.ROLE_TECHNICIAN);
            assertThat(user.getRole()).isEqualTo(Role.ROLE_TECHNICIAN);
        }

        @Test
        @DisplayName("Should block removing the last admin")
        void shouldBlockRemovingLastAdmin() {
            User adminUser = UserBuilder.anUser().withRole(Role.ROLE_ADMIN).build();

            when(repository.findByUuid(adminUser.getUuid())).thenReturn(Optional.of(adminUser));
            when(repository.countByRole(Role.ROLE_ADMIN)).thenReturn(1L);

            assertThatException(
                    () -> service.changeRole(adminUser.getUuid(), Role.ROLE_CUSTOMER),
                    BusinessException.class, "user_service.error.last_admin"
            );

            verify(repository).countByRole(Role.ROLE_ADMIN);
            assertThat(adminUser.getRole()).isEqualTo(Role.ROLE_ADMIN);
        }
    }

    @Nested
    @DisplayName("Change department test for users")
    class ChangeDepartment {
        @Test
        @DisplayName("Should update the department successfully")
        void shouldUpdateDepartment() {
            Department mockDepartment = Department.create("Sales Department");
            User user = mockUserFound(uuid);

            given(departmentRepository.findByUuid(mockDepartment.getUuid())).willReturn(Optional.of(mockDepartment));

            service.changeDepartment(uuid, mockDepartment.getUuid());

            assertThat(user.getDepartment()).isEqualTo(mockDepartment);
        }
    }

    @Nested
    @DisplayName("Must change user status")
    class ChangeStatus {

        @Test
        @DisplayName("Should activate user successfully")
        void shouldActivateUser() {
            // Cria um usuário DISABLED para podermos ativar
            User user = UserBuilder.anUser().withUuid(uuid).withStatus(StatusUser.DISABLED).build();
            given(repository.findByUuid(uuid)).willReturn(Optional.of(user));

            service.activate(uuid);

            assertThat(user.getStatus()).isEqualTo(StatusUser.ACTIVE);
        }

        @Test
        @DisplayName("Should disable user successfully")
        void shouldSuspendUser() {
            User user = mockUserFound(uuid); // Cria usuário ACTIVE

            service.deactivate(uuid);

            assertThat(user.getStatus()).isEqualTo(StatusUser.DISABLED);
        }

        @Test
        @DisplayName("You must change the status to PENDING_EXCLUSION and change the deletion date to (request date) + 7 days.")
        void shouldRequestExclusion() {
            User user = mockUserFound(uuid);

            service.requestAccountExclusion(uuid);

            assertThat(user.getStatus()).isEqualTo(StatusUser.PENDING_EXCLUSION);
            assertThat(user.getScheduledExclusionAt()).isEqualTo(FIXED_DATE.plusDays(7));
        }
    }

    @Nested
    @DisplayName("Find all technicians")
    class FindAllTechnicians {

        @Test
        @DisplayName("Should return page of active technicians successfully")
        void shouldReturnPageOfActiveTechnicians() {
            Pageable pageable = PageRequest.of(0, 10);
            User technician = UserBuilder.anUser().withRole(Role.ROLE_TECHNICIAN).build();

            Page<User> mockPage = new PageImpl<>(List.of(technician), pageable, 1);

            given(repository.findAllByRoleAndStatus(
                    eq(Role.ROLE_TECHNICIAN),
                    eq(StatusUser.ACTIVE),
                    eq(pageable))).willReturn(mockPage);

            Page<User> result = service.findAllActiveTechnicians(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getRole()).isEqualTo(Role.ROLE_TECHNICIAN);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(StatusUser.ACTIVE);

            verify(repository).findAllByRoleAndStatus(Role.ROLE_TECHNICIAN, StatusUser.ACTIVE, pageable);
        }
    }
}
