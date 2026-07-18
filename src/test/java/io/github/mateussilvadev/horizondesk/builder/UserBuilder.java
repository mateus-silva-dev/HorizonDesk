package io.github.mateussilvadev.horizondesk.builder;

import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.model.enums.Role;
import io.github.mateussilvadev.horizondesk.model.enums.StatusUser;
import jakarta.persistence.*;
import lombok.Getter;
import net.datafaker.Faker;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Getter
public class UserBuilder {

    private static final Faker faker = new Faker(Locale.of("pt-BR"));

    private Long id;
    private UUID uuid;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private Department department = Department.create("Technology Department");
    private StatusUser status;

    private UserBuilder() {
        this.uuid = UUID.randomUUID();
        this.name = faker.name().fullName();
        this.email = faker.internet().emailAddress();
        this.passwordHash = faker.credentials().password();
        this.role = Role.ROLE_CUSTOMER;
        this.status = StatusUser.ACTIVE;
    }

    public static UserBuilder anUser() {
        return new UserBuilder();
    }

    public UserBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public UserBuilder withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public UserBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public UserBuilder withRole(Role role) {
        this.role = role;
        return this;
    }

    public UserBuilder withDepartment(Department department) {
        this.department = department;
        return this;
    }

    public UserBuilder withStatus(StatusUser status) {
        this.status = status;
        return this;
    }

    public User build() {
        User user = User.create(name, email, passwordHash, department);

        ReflectionTestUtils.setField(user, "uuid", this.uuid);
        ReflectionTestUtils.setField(user, "role", this.role);
        ReflectionTestUtils.setField(user, "status", this.status);

        return user;
    }

}
