package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.dto.request.UserRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.mapper.UserMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusUser;
import io.github.mateussilvadev.horizondesk.repository.DepartmentRepository;
import io.github.mateussilvadev.horizondesk.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final Clock clock;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, DepartmentRepository departmentRepository, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
        this.clock = clock;
    }

    /**
     * Métodos de negócios.
     */
    @Transactional
    public User create(UserRequestDTOs.UserCreate dto) {
        checkEmailExists(dto.email());
        Department department = departmentRepository.findByUuid(dto.departmentUuid())
            .orElseThrow(() -> new EntityNotFoundException("user_service.error.department_not_found"));
        String encryptedPassword = passwordEncoder.encode(dto.password());
        User user = UserMapper.toUser(dto, department, encryptedPassword);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByUuid(UUID uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("user_service.error.user_not_found"));
    }

    @Transactional
    public User updateUser(UUID uuid, UserRequestDTOs.UserUpdate dto) {
        User user = findByUuid(uuid);
        if (dto.name() != null)
            user.changeName(dto.name());
        if (dto.email() != null) {
            checkEmailExists(dto.email());
            user.changeEmail(dto.email());
        }
        return user;
    }

    @Transactional
    public void changeRole(UUID uuid, Role newRole) {
        User user = findByUuid(uuid);

        boolean removingAdmin = user.getRole() == Role.ROLE_ADMIN && newRole != Role.ROLE_ADMIN;

        if (removingAdmin && userRepository.countByRole(Role.ROLE_ADMIN) <= 1)
            throw new BusinessException(Code.BUSINESS_RULE, "user_service.error.last_admin");

        user.changeRole(newRole);
    }

    @Transactional
    public void changeDepartment(UUID uuid, UUID newDepartmentUuid) {
        User user = findByUuid(uuid);
        Department department = departmentRepository.findByUuid(newDepartmentUuid)
                .orElseThrow(() -> new BusinessException(Code.ENTITY_NOT_FOUND, "user_service.error.department_not_found"));
        user.changeDepartment(department);
    }

    @Transactional
    public void changePassword(UUID uuid, UserRequestDTOs.UpdatePassword dto) {
        User user = findByUuid(uuid);

        if (!Objects.equals(dto.newPassword(), dto.confirmPassword()))
            throw new BusinessException(Code.BUSINESS_RULE, "user_service.error.passwords_dont_match");

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPasswordHash()))
            throw new BusinessException(Code.BUSINESS_RULE, "user_service.error.invalid_current_password");

        if (passwordEncoder.matches(dto.newPassword(), user.getPasswordHash()))
            throw new BusinessException(Code.BUSINESS_RULE, "user_service.error.new_password_same_as_current");

        String encryptedPassword = passwordEncoder.encode(dto.newPassword());
        user.changePasswordHash(encryptedPassword);
    }

    @Transactional
    public void activate(UUID uuid) {
        User user = findByUuid(uuid);
        user.activate();
    }

    @Transactional
    public void deactivate(UUID uuid) {
        User user = findByUuid(uuid);
        user.deactivate();
    }

    @Transactional
    public void requestAccountExclusion(UUID uuid) {
        User user = findByUuid(uuid);
        user.requestAccountExclusion(LocalDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public Page<User> findAllActiveTechnicians(Pageable pageable) {
        return userRepository.findAllByRoleAndStatus(Role.ROLE_TECHNICIAN, StatusUser.ACTIVE, pageable);
    }

    private void checkEmailExists(String email) {
        if (userRepository.existsByEmail(email))
            throw new BusinessException(Code.EMAIL_ALREADY_REGISTERED, "user_service.error.email_already_registered");
    }
}
