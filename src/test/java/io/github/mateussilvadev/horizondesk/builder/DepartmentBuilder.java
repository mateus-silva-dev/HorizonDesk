package io.github.mateussilvadev.horizondesk.builder;

import io.github.mateussilvadev.horizondesk.model.domain.Department;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DepartmentBuilder {

    private Long id;
    private UUID uuid;
    private String name;
    private boolean active;

    private DepartmentBuilder() {
        this.uuid = UUID.randomUUID();
        this.name = "Technology Department";
        this.active = true;
    }

    public static DepartmentBuilder anDepartment() {
        return new DepartmentBuilder();
    }

    public DepartmentBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public DepartmentBuilder withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public DepartmentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public DepartmentBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }

    public Department build() {
        return Department.create(name);
    }
}
