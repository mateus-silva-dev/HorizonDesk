package io.github.mateussilvadev.horizondesk.model.domain;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusUser;
import io.github.mateussilvadev.horizondesk.validation.CommonValidation;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.validator.routines.EmailValidator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, of = "uuid")
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {

    private static final EmailValidator VALIDATOR = EmailValidator.getInstance();

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID uuid;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatusUser status = StatusUser.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @Column(name = "scheduled_exclusion_at")
    private LocalDateTime scheduledExclusionAt;

    private User(String name, String email, String passwordHash, Department department) {
        this.uuid = UUID.randomUUID();
        this.name = checkName(name);
        this.email = checkEmail(email);
        this.passwordHash = checkPasswordHash(passwordHash);
        this.role = Role.ROLE_CUSTOMER;
        this.department = CommonValidation.required(department, "user.field.department");
    }

    public static User create(String name, String email, String passwordHash, Department department) {
        return new User(name, email, passwordHash, department);
    }

    public void changeName(String newName) {
        ensureEditable();
        String validateName = checkName(newName);
        if (!validateName.equals(this.name))
            this.name = validateName;
    }

    public void changeEmail(String newEmail) {
        ensureEditable();
        String validateEmail = checkEmail(newEmail);
        if (!validateEmail.equals(this.email))
            this.email = validateEmail;
    }

    public void changePasswordHash(String newPasswordHash) {
        ensureEditable();
        String validatedHash = checkPasswordHash(newPasswordHash);
        if (!validatedHash.equals(this.passwordHash))
            this.passwordHash = validatedHash;
    }

    public void changeDepartment(Department newDepartment) {
        ensureEditable();
        Department validateDepartment = CommonValidation.required(newDepartment, "user.field.department");
        if (!Objects.equals(this.department, validateDepartment))
            this.department = newDepartment;
    }

    public void changeRole(Role newRole) {
        ensureEditable();
        this.role.transitionTo(newRole);
        this.role = newRole;
    }

    public void activate() {
        this.status.transitionTo(StatusUser.ACTIVE);
        this.status = StatusUser.ACTIVE;
        this.scheduledExclusionAt = null;
    }

    public void deactivate() {
        this.status.transitionTo(StatusUser.DISABLED);
        this.status = StatusUser.DISABLED;
    }

    public void requestAccountExclusion(LocalDateTime now) {
        this.status.transitionTo(StatusUser.PENDING_EXCLUSION);
        this.status = StatusUser.PENDING_EXCLUSION;
        this.scheduledExclusionAt = now.plusDays(7);
    }

    public void executeDefinitiveAnonymization(LocalDateTime now) {
        LocalDateTime validatedNow = CommonValidation.required(now, "user.error.now_required");
        this.status.transitionTo(StatusUser.EXCLUDED);
        if (this.scheduledExclusionAt == null || validatedNow.isBefore(this.scheduledExclusionAt))
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.exclusion_not_due");

        this.name = "Anonymized User";
        this.email = "anonymized-" + this.uuid + "@horizondesk.internal";
        this.passwordHash = "EXCLUDED_ACCOUNT_" + UUID.randomUUID();
        this.department = null;
        this.scheduledExclusionAt = null;
        this.status = StatusUser.EXCLUDED;
    }

    private static String checkName(String name) {
        String validateName = CommonValidation.requiredText(name, "user.field.name", 3);
        if (!validateName.matches("^[A-Za-zÀ-ÿ.\\s'\\-]+$"))
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.invalid_name");
        return validateName;
    }

    private static String checkEmail(String value) {
        String email = CommonValidation.required(value, "user.field.email").trim();
        if (!VALIDATOR.isValid(email))
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.invalid_email");
        return email.toLowerCase(Locale.ROOT);
    }

    private static String checkPasswordHash(String value) {
        return CommonValidation.required(value, "user.field.password_hash").trim();
    }

    private void ensureEditable() {
        if (this.status != StatusUser.ACTIVE)
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.inactive");
    }

    public void ensureCanLogIn() {
        if (!this.status.canLogIn())
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.inactive");
    }

    @Override
    public String toString() {
        return "User[" +
                "id=" + id +
                ", uuid=" + uuid +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ']';
    }

}