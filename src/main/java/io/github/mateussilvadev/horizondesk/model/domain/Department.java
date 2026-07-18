package io.github.mateussilvadev.horizondesk.model.domain;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.validation.CommonValidation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "departments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, of = "uuid")
@EntityListeners(AuditingEntityListener.class)
public class Department {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    private Department(String name) {
        this.uuid = UUID.randomUUID();
        this.name = checkName(name);
    }

    public static Department create(String name) {
        return new Department(name);
    }

    /**
     * Métodos de alteração de dados (Name, E-mail e Password).
     */

    public void changeDepartmentName(String newName) {
        checkActive();
        String validateName = checkName(newName);
        if (!validateName.equals(this.name))
            this.name = validateName;
    }

    public void activate() {
        if (this.active) return;
        this.active = true;
    }

    public void deactivate() {
        if (!this.active) return;
        this.active = false;
    }

    /**
     * Métodos privados de checagem de dados (Name, E-mail, Password e se é Usuário ativo).
     */

    private void checkActive() {
        if (!this.active) throw new BusinessException(Code.BUSINESS_RULE, "department.error.inactive");
    }

    private static String checkName(String name) {
        String validateName = CommonValidation.requiredText(name, "department.field.name", 2);
        if (!validateName.matches("^[A-Za-zÀ-ÿ.\\s'\\-]+$"))
            throw new BusinessException(Code.BUSINESS_RULE, "department.error.invalid_name", new Object[]{"department.field.name"});
        return validateName;
    }
}
